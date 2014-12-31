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

    public static synchronized void stopServer() {
        if ( serverInstance != null ) {
            serverInstance.stop();
        }
    }

    public static void wsRootPath( String rootPath ) {
        serverInstance.getState().rootPath.set( rootPath );
    }

    public static void wsEndpoint( String path, OnMessage onMessage ) {
        serverInstance.getHandlers().put( path, new EndpointWithOnMessage( onMessage ) );
        serverInstance.start();
    }

    public static void wsEndpoint( String path, OnStart onStart, OnMessage onMessage ) {
        serverInstance.getHandlers().put( path, new EndpointWithOnMessage( onStart, onMessage ) );
        serverInstance.start();
    }

    public static void wsEndpoint( String path, OnMessage onMessage, Endpoint endpoint ) {
        serverInstance.getHandlers().put( path, new EndpointWithOnMessage( onMessage, endpoint ) );
        serverInstance.start();
    }

}
