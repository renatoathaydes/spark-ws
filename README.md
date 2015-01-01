# SparkWS

> A Java Websockets library inspired by [Spark](http://sparkjava.com/).

## Quick Start

```java
import static com.athaydes.sparkws.SparkWS.wsEndpoint;

public class HelloSparkWS {
    public static void main(String[] args) {
        wsEndpoint( "hello", ( session, message ) ->
            session.getBasicRemote().sendText( "Hello " + message ) );
    }
}
```

You can immediately test it using [this web page](http://www.websocket.org/echo.html).
Set `Location` to `ws://localhost:8025/hello`.

## Depending on Spark-WS

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

## Documentation

[Click here](wiki/Spark-WS-Documentation) to visit the wiki.

## Demos

* A very simple Spark-WS-based [Chat application](src/demo/com/athaydes/sparkws/demo), complete with
  a HTML/JavaScript client.