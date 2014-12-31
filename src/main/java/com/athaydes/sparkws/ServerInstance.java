package com.athaydes.sparkws;

import org.glassfish.tyrus.core.TyrusServerEndpointConfig;
import org.glassfish.tyrus.core.TyrusWebSocketEngine;
import org.glassfish.tyrus.spi.ServerContainer;
import org.glassfish.tyrus.spi.ServerContainerFactory;

import java.net.BindException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class ServerInstance {

    static final int MAX_PATH_PARTS = 16;

    private volatile State state;
    private volatile Thread serverThread;
    private volatile boolean started = false;
    private volatile CountDownLatch serverLatch;
    private volatile CountDownLatch startupLatch;

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
        final ServerContainer server = ServerContainerFactory.createServerContainer( serverProperties );
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
                    started = true;
                    startupLatch.countDown();
                    serverLatch.await();
                } catch ( BindException be ) {
                    System.out.println( "Cannot start server: " + be.getLocalizedMessage() );
                } catch ( Exception e ) {
                    e.printStackTrace();
                } finally {
                    server.stop();
                    started = false;
                    ServerInstance.this.reset();
                    startupLatch.countDown();
                }
            }
        };
    }

    synchronized void start() {
        if ( started ) {
            return;
        }
        startupLatch = new CountDownLatch( 1 );
        serverThread.start();

        try {
            boolean ok = startupLatch.await( 10, TimeUnit.SECONDS );
            if ( !ok ) {
                throw new RuntimeException( "Server failed to start up within the timeout limit" );
            }
        } catch ( InterruptedException e ) {
            throw new RuntimeException( "Server startup latch interrupted" );
        }
    }

    synchronized void stop() {
        if ( !started ) {
            return;
        }
        serverLatch.countDown();
        try {
            serverThread.join();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        } finally {
            System.out.println( "SparkWS Server stopped." );
        }
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
