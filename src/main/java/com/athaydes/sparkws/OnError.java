package com.athaydes.sparkws;

import javax.websocket.Session;

/**
 *
 */
@FunctionalInterface
public interface OnError {

    void accept( Session session, Throwable error )
            throws Throwable;

}
