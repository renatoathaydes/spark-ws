# SparkWS

> A Java Websockets library inspired by [Spark](sparkjava.com).

## Getting started

Add a dependency to Spark-WS:

> This project will be hosted on JCenter when the first release is out.
  For now, please clone this repo then build from source using `./gradlew publishToMavenLocal`.

### Gradle

```groovy
compile 'com.athaydes.spark-ws:spark-ws:0.1'
```

### Maven

```xml
<dependency>
    <group>com.athaydes.spark-ws</group>
    <artifact>spark-ws</artifact>
    <version>0.1</version>
</dependency>
```

Write a *Hello World* Websocket server that will run at `localhost:8025/hello` with a single endpoint:

```java
public class HelloSparkWS {
    public static void main(String[] args) {
        wsEndpoint( "hello", ( session, message ) ->
            session.getBasicRemote().sendText( "Hello " + message ) );
    }
}
```

That's it! The server is started automatically and you can immediately test it using
[this page](http://www.websocket.org/echo.html), for example.
Set `Location` to `ws://192.168.1.16:8025/hello`.


## Broadcasting

You can broadcast a message to all connected sessions using the `SparkWS.broadcast()` method:

```java
wsEndpoint( "chat",
    ( session, config ) -> broadcast( session, name( session ) + " joined this conversation" ),
    ( session, message ) -> broadcast( session, name( session ) + " says: " + message ) );
```

In the above example, the first closure is a `onStart` handler, and the second a `onMessage` handler.

