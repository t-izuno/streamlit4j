# Spring Boot Integration

streamlit4j ships with a Spring Boot starter that mounts the WebSocket endpoint
and the bundled SPA under a configurable base path. Auto-configuration handles
session management, Spring Security delegation, and Spring Session integration
without any explicit wiring.

## 1. Add the starter

```xml
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-spring-boot-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The starter declares optional dependencies on Spring WebMVC / WebSocket. Your
application must bring its own servlet stack:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

## 2. Provide an `EntrypointSource`

streamlit4j needs a callable that produces a `Runnable` per session — that
`Runnable` is what gets re-executed on each widget event.

```java
@SpringBootApplication
public class MyApp {

  @Bean
  public EntrypointSource streamlit4jEntrypointSource() {
    return () -> MyDashboard::render;  // factory called once per session
  }

  public static void main(String[] args) {
    SpringApplication.run(MyApp.class, args);
  }
}
```

If you omit this bean, the starter falls back to a no-op entrypoint, which is
useful only for context-load smoke tests.

## 3. Configure the base path (optional)

```properties
# application.properties
streamlit4j.base-path=/apps/dashboard
```

| Path | Mapped to |
| --- | --- |
| `${streamlit4j.base-path}/ws` | WebSocket endpoint |
| `${streamlit4j.base-path}/**` | Bundled SPA static assets |

Default base path is `/streamlit`. Setting it to `/` or empty skips the static
handler registration to avoid clobbering Spring's default handlers.

## 4. Authentication via Spring Security

When `spring-security-core` is on the classpath, the starter auto-registers
`Streamlit4jPrincipalHandshakeInterceptor`, which copies the current
`Authentication` into the WebSocket session attributes during handshake.

```java
@Configuration
@EnableWebSecurity
class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/apps/dashboard/**").authenticated()
            .anyRequest().permitAll())
        .oauth2Login(Customizer.withDefaults());
    return http.build();
  }
}
```

Inside your `render()` you can read the authenticated user from the WebSocket
session attributes via
`Streamlit4jPrincipalHandshakeInterceptor.currentAuthentication(session)`.

> **Note**: streamlit4j does **not** enforce authentication on your behalf.
> Configure your `SecurityFilterChain` to match the base path you chose.

## 5. Session lifecycle and Spring Session

The starter binds streamlit4j internal sessions to the hosting HTTP session via
`Streamlit4jHttpSessionRegistry` and registers a Servlet `HttpSessionListener`.
When the HTTP session is destroyed (logout, expiry, or Spring Session backend
eviction), the bound streamlit4j sessions are terminated automatically.

This works transparently whether you use:

- vanilla servlet sessions (default Tomcat / Jetty / Undertow), or
- Spring Session backed by Redis, JDBC, MongoDB, etc.

No additional configuration is required on the streamlit4j side — just add
`spring-session-*` to your project as usual.

## 6. End-to-end example

The repository ships a minimal sample at
`examples/src/main/java/io/streamlit4j/examples/spring/SpringBootHelloApp.java`:

```java
@SpringBootApplication
public class SpringBootHelloApp {

  public static void main(String[] args) {
    SpringApplication.run(SpringBootHelloApp.class, args);
  }

  @Bean
  public EntrypointSource streamlit4jEntrypointSource() {
    return () -> Hello::run;
  }
}
```

Run it locally with:

```sh
mvn -pl examples -am package
java -cp examples/target/streamlit4j-examples-0.1.0-SNAPSHOT.jar:examples/target/dependency/* \
     io.streamlit4j.examples.spring.SpringBootHelloApp
```

(Once a Spring Boot Maven plugin profile lands in TASK-122 you'll be able to
use `mvn -pl examples spring-boot:run` directly.)

## What's auto-configured

| Bean | Purpose | Conditional on |
| --- | --- | --- |
| `Streamlit4jApplication` | Composition root (sessions, downloads, renderer) | `Bootstrap` on classpath |
| `Streamlit4jWebSocketHandler` | Spring WebSocket adapter to core use cases | always |
| `Streamlit4jPrincipalHandshakeInterceptor` | Copies Spring Security `Authentication` | `SecurityContextHolder` on classpath |
| `Streamlit4jHttpSessionRegistry` + `Binder` + `Listener` | HTTP session ↔ streamlit4j session binding | servlet web environment |
| `ResourceRegistration` | Serves bundled SPA at `${base-path}/**` | `WebMvcConfigurer` on classpath |
| `WebSocketRegistration` | Registers WebSocket handler at `${base-path}/ws` | `@EnableWebSocket` enabled |

## See also

- [Getting Started](./getting-started) — standalone CLI / Maven workflow
- [Reference](../reference/overview) — full element catalog
