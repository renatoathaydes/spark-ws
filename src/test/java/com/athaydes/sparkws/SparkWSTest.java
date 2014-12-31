package com.athaydes.sparkws;

import com.google.common.util.concurrent.SettableFuture;
import org.glassfish.tyrus.client.ClientManager;
import org.junit.After;
import org.junit.Test;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static com.athaydes.sparkws.SparkWS.runServer;
import static com.athaydes.sparkws.SparkWS.stopServer;
import static com.athaydes.sparkws.SparkWS.wsEndpoint;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class SparkWSTest {

    private final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
    private final ClientManager client = ClientManager.createClient();

    @After
    public void cleanup() {
        stopServer();
    }

    @Test
    public void simplestStart() throws Exception {
        wsEndpoint( "hello", ( session, message ) -> {
            session.getBasicRemote().sendText( "Hello " + message );
        } );
        runServer();

        assertMessageReceived( "hello", "Hello SparkWS" );
    }

    @Test
    public void simplestStartAgainToEnsureServerCanBeRestarted() throws Exception {
        wsEndpoint( "ola", ( session, message ) -> {
            session.getBasicRemote().sendText( "Ola " + message );
        } );
        runServer();

        assertMessageReceived( "ola", "Ola SparkWS" );
    }

    @Test
    public void twoDifferentSimpleEndpoints() throws Exception {
        wsEndpoint( "ep1", ( session, message ) -> {
            session.getBasicRemote().sendText( "EP1" );
        } );
        wsEndpoint( "ep2", ( session, message ) -> {
            session.getBasicRemote().sendText( "EP2" );
        } );
        runServer();

        assertMessageReceived( "ep1", "EP1" );
        assertMessageReceived( "ep2", "EP2" );
    }

    @Test
    public void multiPathEndpoints() throws Exception {
        wsEndpoint( "part1/part2", ( session, message ) -> {
            session.getBasicRemote().sendText( "P1P2" );
        } );
        wsEndpoint( "part1/part3", ( session, message ) -> {
            session.getBasicRemote().sendText( "P1P3" );
        } );
        wsEndpoint( "part2/part3", ( session, message ) -> {
            session.getBasicRemote().sendText( "P2P3" );
        } );
        runServer();

        assertMessageReceived( "part1/part2", "P1P2" );
        assertMessageReceived( "part1/part3", "P1P3" );
        assertMessageReceived( "part2/part3", "P2P3" );
    }

    @Test
    public void mostSpecificPathIsSelected() throws Exception {
        wsEndpoint( "part1/part2/part3", ( session, message ) -> {
            session.getBasicRemote().sendText( "P1P2P3" );
        } );
        wsEndpoint( "part1", ( session, message ) -> {
            session.getBasicRemote().sendText( "P1" );
        } );
        wsEndpoint( "part1/part2", ( session, message ) -> {
            session.getBasicRemote().sendText( "P1P2" );
        } );
        wsEndpoint( "part1/part2/part3/part4", ( session, message ) -> {
            session.getBasicRemote().sendText( "P1P2P3P4" );
        } );
        runServer();

        assertMessageReceived( "part1", "P1" );
        assertMessageReceived( "part1/part2", "P1P2" );
        assertMessageReceived( "part1/part2/part3", "P1P2P3" );
        assertMessageReceived( "part1/part2/part3/part4", "P1P2P3P4" );
    }

    private void assertMessageReceived( String endpoint, String expectedMessage ) throws Exception {
        final SettableFuture<String> futureMessage = SettableFuture.create();

        client.connectToServer( new Endpoint() {

            @Override
            public void onOpen( Session session, EndpointConfig config ) {
                try {
                    session.addMessageHandler( new MessageHandler.Whole<String>() {

                        @Override
                        public void onMessage( String message ) {
                            System.out.println( "Received message: " + message );
                            futureMessage.set( message );
                        }
                    } );
                    session.getBasicRemote().sendText( "SparkWS" );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }, cec, new URI( "ws://localhost:8025/" + endpoint ) );

        assertEquals( expectedMessage, futureMessage.get( 2, TimeUnit.SECONDS ) );
    }

}
