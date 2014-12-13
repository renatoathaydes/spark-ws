package com.athaydes.sparkws;

import com.google.common.util.concurrent.SettableFuture;
import org.glassfish.tyrus.client.ClientManager;
import org.junit.Test;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static com.athaydes.sparkws.SparkWS.runServer;
import static com.athaydes.sparkws.SparkWS.wsEndpoint;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class SparkWSTest {

    private final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
    private final ClientManager client = ClientManager.createClient();

    @Test
    public void simplestStart() throws Exception {
        wsEndpoint( "hello", ( session, message ) -> {
            session.getBasicRemote().sendText( "Hello " + message );
        } );
        runServer();

        assertMessageReceived( "hello", "Hello SparkWS" );
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
