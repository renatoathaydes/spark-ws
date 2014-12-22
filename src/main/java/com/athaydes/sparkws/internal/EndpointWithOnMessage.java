package com.athaydes.sparkws.internal;

import com.athaydes.sparkws.remote.OnMessage;
import com.athaydes.sparkws.remote.OnStart;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;

/**
 *
 */
public class EndpointWithOnMessage extends Endpoint implements OnMessage {

    private final OnMessage onMessage;
    private OnStart onStart;
    private Endpoint delegateEndpoint;

    public EndpointWithOnMessage( OnMessage onMessage ) {
        this.onMessage = onMessage;
    }

    public EndpointWithOnMessage( OnStart onStart, OnMessage onMessage ) {
        this.onStart = onStart;
        this.onMessage = onMessage;
    }

    public EndpointWithOnMessage( OnMessage onMessage, Endpoint endpoint ) {
        this.onMessage = onMessage;
        this.delegateEndpoint = endpoint;
    }

    @Override
    public void onOpen( Session session, EndpointConfig config ) {
        if ( delegateEndpoint != null ) {
            delegateEndpoint.onOpen( session, config );
        } else try {
            onStart.accept( session, config );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void acceptWhole( Session session, String message ) throws IOException {
        onMessage.acceptWhole( session, message );
    }

}
