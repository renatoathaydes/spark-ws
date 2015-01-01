package com.athaydes.sparkws;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;

class EndpointWithOnMessage extends Endpoint implements OnMessage {

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
        } else if ( onStart != null ) try {
            onStart.accept( session, config );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose( Session session, CloseReason closeReason ) {
        if ( delegateEndpoint != null ) {
            delegateEndpoint.onClose( session, closeReason );
        }
    }

    @Override
    public void onError( Session session, Throwable error ) {
        if ( delegateEndpoint != null ) {
            delegateEndpoint.onError( session, error );
        }
    }

    @Override
    public void acceptWhole( Session session, String message ) throws IOException {
        onMessage.acceptWhole( session, message );
    }

}
