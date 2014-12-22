package com.athaydes.sparkws;

import com.athaydes.sparkws.internal.EndpointWithOnMessage;
import org.glassfish.tyrus.core.TyrusServerEndpointConfig;
import org.glassfish.tyrus.spi.ServerContainer;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

class ServerInstance {

    private State state = new State();
    private volatile ServerContainer server;
    private Thread serverThread;
    private Thread shutdownHookThread;
    private boolean started = false;
    private final Map<String, EndpointWithOnMessage> handlers = new ConcurrentHashMap<>();
    private final CountDownLatch serverLatch = new CountDownLatch( 1 );

    ServerInstance( ServerContainer serverContainer ) {
        Objects.requireNonNull( serverContainer );
        this.server = serverContainer;

        this.shutdownHookThread = new Thread() {
            @Override
            public void run() {
                try {
                    server.stop();
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        };

        this.serverThread = new Thread( "SparkWS-Server" ) {
            @Override
            public void run() {
                try {
                    server.addEndpoint( TyrusServerEndpointConfig.Builder
                            .create( SparkWS.InternalSparkWSEndpoint.class, "/{param1}" ).build() );
                    server.start( state.rootPath.get(), state.port.get() );
                    System.out.println( "SparkWS Server started!" );
                    serverLatch.await();
                } catch ( Exception e ) {
                    e.printStackTrace();
                } finally {
                    ServerInstance.this.stop();
                }
            }
        };
    }

    void start() {
        Runtime.getRuntime().addShutdownHook( shutdownHookThread );
        serverThread.start();
        started = true;
    }

    void stop() {
        state = new State();
        handlers.clear();
        started = false;
        serverLatch.countDown();
        Runtime.getRuntime().removeShutdownHook( shutdownHookThread );
        serverThread = null;
        shutdownHookThread = null;
        server = null;
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
