package com.athaydes.sparkws;


import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;

/**
 *
 */
@FunctionalInterface
public interface OnClose {

    void accept( Session session, CloseReason closeReason )
            throws IOException;


}
