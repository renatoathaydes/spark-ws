package com.athaydes.sparkws;

import com.athaydes.sparkws.remote.OnMessage;
import com.athaydes.sparkws.remote.OnStart;
import org.glassfish.tyrus.core.TyrusServerEndpointConfig;
import org.glassfish.tyrus.core.TyrusWebSocketEngine;
import org.glassfish.tyrus.spi.ServerContainer;
import org.glassfish.tyrus.spi.ServerContainerFactory;
import sun.plugin.dom.exception.InvalidStateException;

import javax.websocket.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class SparkWS {

    private static final State state = new State();
    private static final Map<String, OnMessage> handlers = new ConcurrentHashMap<>();
    private static volatile ServerContainer server;

    public static synchronized void runServer() {
        if ( server != null ) {
            throw new InvalidStateException( "Server already running" );
        }

        Map<String, Object> props = new HashMap<>();
        props.put( TyrusWebSocketEngine.WSADL_SUPPORT, "true" );

        server = ServerContainerFactory.createServerContainer( props );

        new Thread( "SparkWS-Server" ) {
            @Override
            public void run() {
                try {
                    server.addEndpoint( TyrusServerEndpointConfig.Builder
                            .create( InternalSparkWSEndpoint.class, "/{param1}" ).build() );
                    server.start( state.rootPath.get(), state.port.get() );
                    System.out.println( "Server started. Press any key to stop" );
                    System.in.read();
                } catch ( Exception e ) {
                    e.printStackTrace();
                } finally {
                    server.stop();
                }
            }
        }.start();

    }

    public static void wsRootPath( String rootPath ) {
        state.rootPath.set( rootPath );
    }

    public static void wsEndpoint( String path, OnMessage onMessage ) {
        handlers.put( path, onMessage );
    }

    public static void wsEndpoint( String path, OnStart onStart, OnMessage onMessage ) {
        //TODO
    }

    public static class InternalSparkWSEndpoint extends Endpoint {

        @Override
        public void onOpen( final Session session, EndpointConfig config ) {
            System.out.println( "Session started on path: " + session.getRequestURI().getPath() );
            final OnMessage handler = handlers.get( session.getRequestURI().getPath().substring( 1 ) );
            if ( handler == null ) {
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
                        handler.accept( session, message );
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
            } );
        }

        @Override
        public void onClose( Session session, CloseReason closeReason ) {
            //TODO
            super.onClose( session, closeReason );
        }

        @Override
        public void onError( Session session, Throwable thr ) {
            //TODO
            super.onError( session, thr );
        }
    }


}
