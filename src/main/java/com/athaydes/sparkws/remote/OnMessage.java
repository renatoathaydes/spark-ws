package com.athaydes.sparkws.remote;

import javax.websocket.Session;
import java.io.IOException;

/**
 *
 */
@FunctionalInterface
public interface OnMessage {

    void acceptWhole( Session session, String message )
            throws IOException;

//    default void sendText( String text ) {
//        SparkWS.sessionFor(this);
//    }

}
