package com.athaydes.sparkws;

import com.google.common.util.concurrent.SettableFuture;
import org.glassfish.tyrus.client.ClientManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.athaydes.sparkws.SparkWS.serverInstance;
import static com.athaydes.sparkws.SparkWS.stopServer;
import static com.athaydes.sparkws.SparkWS.wsEndpoint;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class SparkWSTest {

    private final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
    private final ClientManager client = ClientManager.createClient();
    private Session clientSession;
    private static final ReentrantLock lock = new ReentrantLock();

    @Before
    public void setup() {
        lock.lock();
    }

    @After
    public void cleanup() {
        stopServer();
        lock.unlock();
    }

    @Test
    public void startStopTest() {
        for ( int i = 0; i < 10; i++ ) {
            serverInstance.start();
            assertTrue( "Server not started on run " + i, serverInstance.isStarted() );
            serverInstance.stop();
            assertFalse( "Server not stopped on run " + i, serverInstance.isStarted() );
        }
    }

    @Test
    public void simplestStart() throws Exception {
        wsEndpoint( "hello", ( session, message ) ->
                session.getBasicRemote().sendText( "Hello " + message ) );

        assertMessageReceived( "hello", "Hello SparkWS" );
    }

    @Test
    public void simplestStartAgainToEnsureServerCanBeRestarted() throws Exception {
        wsEndpoint( "ola", ( session, message ) ->
                session.getBasicRemote().sendText( "Ola " + message ) );

        assertMessageReceived( "ola", "Ola SparkWS" );
    }

    @Test
    public void twoDifferentSimpleEndpoints() throws Exception {
        wsEndpoint( "ep1", ( session, message ) ->
                session.getBasicRemote().sendText( "EP1" ) );
        wsEndpoint( "ep2", ( session, message ) ->
                session.getBasicRemote().sendText( "EP2" ) );

        assertMessageReceived( "ep1", "EP1" );
        assertMessageReceived( "ep2", "EP2" );
    }

    @Test
    public void multiPathEndpoints() throws Exception {
        wsEndpoint( "part1/part2", ( session, message ) ->
                session.getBasicRemote().sendText( "P1P2" ) );
        wsEndpoint( "part1/part3", ( session, message ) ->
                session.getBasicRemote().sendText( "P1P3" ) );
        wsEndpoint( "part2/part3", ( session, message ) ->
                session.getBasicRemote().sendText( "P2P3" ) );

        assertMessageReceived( "part1/part2", "P1P2" );
        assertMessageReceived( "part1/part3", "P1P3" );
        assertMessageReceived( "part2/part3", "P2P3" );
    }

    @Test
    public void mostSpecificPathIsSelected() throws Exception {
        wsEndpoint( "part1/part2/part3", ( session, message ) ->
                session.getBasicRemote().sendText( "P1P2P3" ) );
        wsEndpoint( "part1", ( session, message ) ->
                session.getBasicRemote().sendText( "P1" ) );
        wsEndpoint( "part1/part2", ( session, message ) ->
                session.getBasicRemote().sendText( "P1P2" ) );
        wsEndpoint( "part1/part2/part3/part4", ( session, message ) ->
                session.getBasicRemote().sendText( "P1P2P3P4" ) );

        assertMessageReceived( "part1", "P1" );
        assertMessageReceived( "part1/part2", "P1P2" );
        assertMessageReceived( "part1/part2/part3", "P1P2P3" );
        assertMessageReceived( "part1/part2/part3/part4", "P1P2P3P4" );
    }

    @Test
    public void onStartHandlerGetsCalled() throws Exception {
        List<Session> interactions = new ArrayList<>();
        wsEndpoint( "test",
                ( session, config ) -> interactions.add( session ),
                ( session, message ) -> session.getBasicRemote().sendText( "A" ) );

        assertMessageReceived( "test", "A" );
        assertEquals( 1, interactions.size() );
    }

    @Test
    public void onOpenOnErrorHandlersGetCalled() throws Exception {
        List<String> handlerCalls = new ArrayList<>();

        createEndpointUsingOnOpenOnError( handlerCalls );

        assertMessageReceived( "test", "B" );

        clientSession.getBasicRemote().sendText( "ERROR" );
        clientSession.close();

        assertEquals( Arrays.asList( "onOpen", "onError:RuntimeException" ), handlerCalls );
    }

    @Test
    public void onOpenOnErrorOnCloseHandlersGetCalled() throws Exception {
        List<String> handlerCalls = new ArrayList<>();
        CountDownLatch onCloseLatch = new CountDownLatch( 1 );

        createEndpointUsingOnOpenOnErrorOnClose( handlerCalls, onCloseLatch );

        assertMessageReceived( "test", "B" );

        clientSession.getBasicRemote().sendText( "ERROR" );
        clientSession.close();
        onCloseLatch.await( 2, TimeUnit.SECONDS );

        assertEquals( Arrays.asList( "onOpen", "onError:RuntimeException", "onClose" ), handlerCalls );
    }

    @Test
    public void customEndpointHandlersGetCalled() throws Exception {
        List<String> handlerCalls = new ArrayList<>();
        CountDownLatch onCloseLatch = new CountDownLatch( 1 );

        createEndpointUsingCustomEndpoint( handlerCalls, onCloseLatch );

        assertMessageReceived( "test", "B" );

        clientSession.getBasicRemote().sendText( "ERROR" );
        clientSession.close();
        onCloseLatch.await( 2, TimeUnit.SECONDS );

        assertEquals( Arrays.asList( "onOpen", "onError:RuntimeException", "onClose" ), handlerCalls );
    }

    private void createEndpointUsingOnOpenOnError( List<String> handlerCalls ) {
        wsEndpoint( "test",
                ( session, config ) -> handlerCalls.add( "onOpen" ),
                ( session, message ) -> {
                    if ( message.equals( "ERROR" ) ) {
                        throw new RuntimeException();
                    }
                    session.getBasicRemote().sendText( "B" );
                }, ( session, error ) -> {
                    handlerCalls.add( "onError:" + error.getClass().getSimpleName() );
                    try {
                        session.getBasicRemote().sendText( "ERROR" );
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }

                } );
    }

    private void createEndpointUsingOnOpenOnErrorOnClose( List<String> handlerCalls, CountDownLatch onCloseLatch ) {
        wsEndpoint( "test",
                ( session, config ) -> handlerCalls.add( "onOpen" ),
                ( session, message ) -> {
                    if ( message.equals( "ERROR" ) ) {
                        throw new RuntimeException();
                    }
                    session.getBasicRemote().sendText( "B" );
                }, ( session, error ) -> {
                    handlerCalls.add( "onError:" + error.getClass().getSimpleName() );
                    try {
                        session.getBasicRemote().sendText( "ERROR" );
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }

                }, ( session, closeReason ) -> {
                    handlerCalls.add( "onClose" );
                    onCloseLatch.countDown();
                } );
    }

    private void createEndpointUsingCustomEndpoint( List<String> handlerCalls, CountDownLatch onCloseLatch ) {
        wsEndpoint( "test",
                ( session, message ) -> {
                    if ( message.equals( "ERROR" ) ) {
                        throw new RuntimeException();
                    }
                    session.getBasicRemote().sendText( "B" );
                }, new Endpoint() {
                    @Override
                    public void onOpen( Session session, EndpointConfig config ) {
                        handlerCalls.add( "onOpen" );
                    }

                    @Override
                    public void onClose( Session session, CloseReason closeReason ) {
                        handlerCalls.add( "onClose" );
                        onCloseLatch.countDown();
                    }

                    @Override
                    public void onError( Session session, Throwable thr ) {
                        handlerCalls.add( "onError:" + thr.getClass().getSimpleName() );
                        try {
                            session.getBasicRemote().sendText( "ERROR" );
                        } catch ( IOException e ) {
                            e.printStackTrace();
                        }
                    }
                } );
    }

    private void assertMessageReceived( String endpoint, String expectedMessage ) throws Exception {
        assertMessageReceived( endpoint, expectedMessage, "SparkWS" );
    }

    private void assertMessageReceived( String endpoint, String expectedMessage, String messageToSend ) throws Exception {
        final SettableFuture<String> futureMessage = SettableFuture.create();

        client.connectToServer( new Endpoint() {

            @Override
            public void onOpen( Session session, EndpointConfig config ) {
                clientSession = session;
                try {
                    session.addMessageHandler( new MessageHandler.Whole<String>() {

                        @Override
                        public void onMessage( String message ) {
                            System.out.println( "Received message: " + message );
                            futureMessage.set( message );
                        }
                    } );
                    session.getBasicRemote().sendText( messageToSend );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }, cec, new URI( "ws://localhost:8025/" + endpoint ) );

        assertEquals( expectedMessage, futureMessage.get( 2, TimeUnit.SECONDS ) );
    }

}
