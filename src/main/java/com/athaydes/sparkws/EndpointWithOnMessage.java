package com.athaydes.sparkws;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;

class EndpointWithOnMessage extends Endpoint implements OnMessage {

    private final OnMessage onMessage;
    private final Endpoint delegateEndpoint;

    private static void noOp() {
    }

    public EndpointWithOnMessage( OnMessage onMessage ) {
        this( ( a, b ) -> noOp(), onMessage, ( a, b ) -> noOp(), ( a, b ) -> noOp() );
    }

    public EndpointWithOnMessage( OnStart onStart, OnMessage onMessage ) {
        this( onStart, onMessage, ( a, b ) -> noOp(), ( a, b ) -> noOp() );
    }

    public EndpointWithOnMessage( OnStart onStart, OnMessage onMessage, OnError onError ) {
        this( onStart, onMessage, onError, ( a, b ) -> noOp() );
    }

    public EndpointWithOnMessage( OnStart onStart, OnMessage onMessage, OnError onError, OnClose onClose ) {
        this( onMessage, new Endpoint() {
            @Override
            public void onOpen( Session session, EndpointConfig config ) {
                try {
                    onStart.accept( session, config );
                } catch ( Throwable throwable ) {
                    onError( session, throwable );
                }
            }

            @Override
            public void onClose( Session session, CloseReason closeReason ) {
                try {
                    onClose.accept( session, closeReason );
                } catch ( Throwable throwable ) {
                    onError( session, throwable );
                }
            }

            @Override
            public void onError( Session session, Throwable error ) {
                try {
                    onError.accept( session, error );
                } catch ( Throwable throwable ) {
                    throwable.printStackTrace();
                }
            }
        } );
    }

    public EndpointWithOnMessage( OnMessage onMessage, Endpoint endpoint ) {
        this.onMessage = onMessage;
        this.delegateEndpoint = endpoint;
    }

    @Override
    public void onOpen( Session session, EndpointConfig config ) {
        delegateEndpoint.onOpen( session, config );
    }

    @Override
    public void onClose( Session session, CloseReason closeReason ) {
        delegateEndpoint.onClose( session, closeReason );
    }

    @Override
    public void onError( Session session, Throwable error ) {
        delegateEndpoint.onError( session, error );
    }

    @Override
    public void acceptWhole( Session session, String message ) throws IOException {
        onMessage.acceptWhole( session, message );
    }

}
