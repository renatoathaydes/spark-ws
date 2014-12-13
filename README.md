# SparkWS

> A Java Websockets library inspired by [Spark](sparkjava.com).

# === Work in progress ====


## Getting started

To run a *Hello World* Websocket server at `localhost:8025/hello` with a single endpoint:

```java
wsEndpoint( "hello", ( session, message ) -> {
    session.getBasicRemote().sendText( "Hello " + message );
} );
runServer();
```
