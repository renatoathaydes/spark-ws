package com.athaydes.sparkws;

import com.athaydes.sparkws.internal.EndpointWithOnMessage;
import com.athaydes.sparkws.internal.StringPathUtil;
import org.glassfish.tyrus.core.TyrusServerEndpointConfig;
import org.glassfish.tyrus.core.TyrusWebSocketEngine;
import org.glassfish.tyrus.spi.ServerContainer;
import org.glassfish.tyrus.spi.ServerContainerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

class ServerInstance {

    static final int MAX_PATH_PARTS = 16;

    private volatile State state;
    private volatile ServerContainer server;
    private volatile Thread serverThread;
    private volatile CountDownLatch serverLatch;

    private volatile boolean started = false;
    private final Map<String, EndpointWithOnMessage> handlers = new ConcurrentHashMap<>();
    private final Map<String, Object> serverProperties = new HashMap<>();

    ServerInstance() {
        serverProperties.put( TyrusWebSocketEngine.WSADL_SUPPORT, "true" );
        Runtime.getRuntime().addShutdownHook( createShutdownHookThread() );
        reset();
    }

    private Thread createShutdownHookThread() {
        return new Thread() {
            @Override
            public void run() {
                try {
                    ServerInstance.this.stop();
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void reset() {
        state = new State();
        handlers.clear();
        server = ServerContainerFactory.createServerContainer( serverProperties );
        serverLatch = new CountDownLatch( 1 );

        serverThread = new Thread( "SparkWS-Server" ) {
            @Override
            public void run() {
                try {
                    for ( String parameters : StringPathUtil.parametersPaths( MAX_PATH_PARTS ) ) {
                        server.addEndpoint( TyrusServerEndpointConfig.Builder
                                .create( InternalSparkWSEndpoint.class, parameters ).build() );
                    }
                    server.start( state.rootPath.get(), state.port.get() );
                    System.out.println( "SparkWS Server started!" );
                    serverLatch.await();
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        };
    }

    void start() {
        serverThread.start();
        started = true;
    }

    void stop() {
        if ( !started ) {
            return;
        }
        serverLatch.countDown();
        started = false;
        reset();
    }

    public State getState() {
        return state;
    }

    public boolean isStarted() {
        return started;
    }

    public Map<String, EndpointWithOnMessage> getHandlers() {
        return handlers;
    }

}
