package com.athaydes.sparkws;

import javax.websocket.Session;
import java.io.IOException;

/**
 * Handler for receiving messages.
 */
@FunctionalInterface
public interface OnMessage {

    void acceptWhole( Session session, String message )
            throws IOException;

}
