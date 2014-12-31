package com.athaydes.sparkws;

import com.athaydes.sparkws.internal.EndpointWithOnMessage;
import com.athaydes.sparkws.remote.OnMessage;
import com.athaydes.sparkws.remote.OnStart;

import javax.websocket.Endpoint;

/**
 * SparkWS is, basically, a namespace for all "functions", or static methods, that can
 * be used to configure a Websocket server.
 */
public class SparkWS {

    static final ServerInstance serverInstance = new ServerInstance();

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

}
