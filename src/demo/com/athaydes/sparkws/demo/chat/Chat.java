package com.athaydes.sparkws.demo.chat;

import javax.websocket.Session;

import static com.athaydes.sparkws.SparkWS.broadcast;
import static com.athaydes.sparkws.SparkWS.wsEndpoint;

/**
 * Simple sample application showing how to create a simple Chat using SparkWS.
 */
public class Chat {

    public static void main( String[] args ) {
        wsEndpoint( "chat",
                ( session, config ) -> broadcast( session, name( session ) + " joined this conversation" ),
                ( session, message ) -> broadcast( session, name( session ) + " says: " + message ) );
    }

    private static String name( Session session ) {
        return session.getId();
    }

}
