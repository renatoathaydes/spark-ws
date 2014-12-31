package com.athaydes.sparkws;

import com.athaydes.sparkws.internal.EndpointWithOnMessage;
import com.athaydes.sparkws.remote.OnMessage;
import com.athaydes.sparkws.remote.OnStart;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.io.IOException;

/**
 * SparkWS is, basically, a namespace for all "functions", or static methods, that can
 * be used to configure a Websocket server.
 */
public class SparkWS {

    private static volatile ServerInstance serverInstance = new ServerInstance();

    public static synchronized void runServer() {
        serverInstance.start();
    }

    public static synchronized void stopServer() {
        if ( serverInstance != null ) {
            serverInstance.stop();
        }
    }

    public static void wsRootPath( String rootPath ) {
        ensureServerNotStarted();
        serverInstance.getState().rootPath.set( rootPath );
    }

    public static void wsEndpoint( String path, OnMessage onMessage ) {
        ensureServerNotStarted();
        serverInstance.getHandlers().put( path, new EndpointWithOnMessage( onMessage ) );
    }

    public static void wsEndpoint( String path, OnStart onStart, OnMessage onMessage ) {
        ensureServerNotStarted();
        serverInstance.getHandlers().put( path, new EndpointWithOnMessage( onStart, onMessage ) );
    }

    public static void wsEndpoint( String path, OnMessage onMessage, Endpoint endpoint ) {
        ensureServerNotStarted();
        serverInstance.getHandlers().put( path, new EndpointWithOnMessage( onMessage, endpoint ) );
    }

    private static void ensureServerNotStarted() {
        if ( serverInstance.isStarted() ) {
            throw new IllegalStateException( "Server already started" );
        }
    }

    public static class InternalSparkWSEndpoint extends Endpoint {

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
            return serverInstance.getHandlers().get( session.getRequestURI().getPath().substring( 1 ) );
        }
    }


}
