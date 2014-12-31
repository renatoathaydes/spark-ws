package com.athaydes.sparkws;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.io.IOException;

class InternalSparkWSEndpoint extends Endpoint {

    @Override
    public void onOpen( final Session session, EndpointConfig config ) {
        System.out.println( "Session started on path: " + session.getRequestURI().getPath() );
        final EndpointWithOnMessage handler = getHandler( session );
        if ( handler == null ) {
            System.out.println( "No handler found for path " + session.getRequestURI().getPath() );
            try {
                session.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
            return;
        }
        session.addMessageHandler( new MessageHandler.Whole<String>() {
            @Override
            public void onMessage( String message ) {
                System.out.println( "Server got message " + message );
                try {
                    handler.acceptWhole( session, message );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        } );
    }

    @Override
    public void onClose( Session session, CloseReason closeReason ) {
        final EndpointWithOnMessage handler = getHandler( session );
        if ( handler != null ) {
            handler.onClose( session, closeReason );
        }
    }

    @Override
    public void onError( Session session, Throwable thr ) {
        final EndpointWithOnMessage handler = getHandler( session );
        if ( handler != null ) {
            handler.onError( session, thr );
        }
    }

    private EndpointWithOnMessage getHandler( Session session ) {
        return SparkWS.serverInstance.getHandlers().get( session.getRequestURI().getPath().substring( 1 ) );
    }
}

