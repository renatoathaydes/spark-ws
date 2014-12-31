package com.athaydes.sparkws.demo;

import static com.athaydes.sparkws.SparkWS.broadcast;
import static com.athaydes.sparkws.SparkWS.wsEndpoint;

/**
 * Simple sample application showing how to create a simple Chat using SparkWS.
 */
public class Chat {

    public static void main( String[] args ) {
        wsEndpoint( "chat",
                ( session, config ) -> broadcast( session, session.getId() + " joined this conversation" ),
                ( session, message ) -> broadcast( session, session.getId() + " says: " + message ) );
    }

}
