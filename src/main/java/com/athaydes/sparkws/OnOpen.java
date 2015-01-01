package com.athaydes.sparkws;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;

/**
 * Action to run when a websocket connection is opened.
 */
@FunctionalInterface
public interface OnOpen {

    void accept( Session session, EndpointConfig config )
            throws IOException;

}
