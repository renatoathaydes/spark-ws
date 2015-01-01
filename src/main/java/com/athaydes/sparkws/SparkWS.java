package com.athaydes.sparkws;

import org.glassfish.tyrus.core.TyrusSession;

import javax.websocket.Endpoint;
import javax.websocket.Session;

/**
 * SparkWS is, basically, a namespace for all "functions", or static methods, that can
 * be used to configure a Websocket server.
 */
public class SparkWS {

    static final ServerInstance serverInstance = new ServerInstance();

    public static synchronized void stopServer() {
        if ( serverInstance != null ) {
            serverInstance.stop();
            System.out.println( "Server STOPPED!" );
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

    public static void wsEndpoint( String path, OnStart onStart, OnMessage onMessage, OnError onError ) {
        serverInstance.getHandlers().put( path, new EndpointWithOnMessage( onStart, onMessage, onError ) );
        serverInstance.start();
    }

    public static void wsEndpoint( String path, OnStart onStart, OnMessage onMessage, OnError onError, OnClose onClose ) {
        serverInstance.getHandlers().put( path, new EndpointWithOnMessage( onStart, onMessage, onError, onClose ) );
        serverInstance.start();
    }

    public static void wsEndpoint( String path, OnMessage onMessage, Endpoint endpoint ) {
        serverInstance.getHandlers().put( path, new EndpointWithOnMessage( onMessage, endpoint ) );
        serverInstance.start();
    }

    public static void broadcast( Session session, String message ) {
        ( ( TyrusSession ) session ).broadcast( message );
    }

}
