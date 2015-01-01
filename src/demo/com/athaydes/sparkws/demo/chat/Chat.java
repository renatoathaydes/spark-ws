package com.athaydes.sparkws.demo.chat;

import javax.websocket.Session;

import static com.athaydes.sparkws.SparkWS.broadcast;
import static com.athaydes.sparkws.SparkWS.wsEndpoint;
import static spark.Spark.get;
import static spark.SparkBase.port;
import static spark.SparkBase.staticFileLocation;

/**
 * Simple sample application showing how to create a simple Chat using SparkWS.
 *
 * Run Chat.java, then access
 *
 * http://localhost:8026/chat.html
 *
 * The WS endpoint can be accessed directly at
 *
 * ws://localhost:8025/chat
 */
public class Chat {

    public static void main( String[] args ) {
        // Spark-WS config
        wsEndpoint( "chat",
                ( session, config ) -> broadcast( session, name( session ) + " joined this conversation" ),
                ( session, message ) -> broadcast( session, name( session ) + " says: " + message ) );

        // Spark config
        staticFileLocation( "com/athaydes/sparkws/demo/chat/resources" );
        port( 8026 );
        get("chat-app", (req, res) -> "Chat-App");
    }

    private static String name( Session session ) {
        return session.getId();
    }

}
