# streamlit4j examples (Spring Boot)

Spring Boot launchers that mount the same six demos under `${streamlit4j.base-path}`
(default `/streamlit`) using the streamlit4j Spring Boot Starter. Use this form when you
want streamlit4j to coexist with the rest of a Spring Boot app — the canonical reference
for **adoption path B** in the project README.

The render scripts (`Hello::run`, `WidgetsDemo::run`, …) live in
[`../embedded`](../embedded) and are pulled in via the
`streamlit4j-examples-embedded` Maven dependency, so the two sub-projects always render
the same content.

## What is in here

Each launcher lives in its own sub-package so that `@SpringBootApplication`'s component
scan finds exactly one entrypoint bean per app.

| Class | Wraps render script |
| --- | --- |
| `io.streamlit4j.examples.spring.hello.SpringBootHelloApp` | `io.streamlit4j.examples.Hello` |
| `io.streamlit4j.examples.spring.widgets.SpringBootWidgetsApp` | `io.streamlit4j.examples.WidgetsDemo` |
| `io.streamlit4j.examples.spring.layout.SpringBootLayoutApp` | `io.streamlit4j.examples.LayoutDemo` |
| `io.streamlit4j.examples.spring.data.SpringBootDataApp` | `io.streamlit4j.examples.DataDemo` |
| `io.streamlit4j.examples.spring.component.SpringBootComponentApp` | `io.streamlit4j.examples.ComponentDemo` |
| `io.streamlit4j.examples.spring.showcase.SpringBootShowcaseApp` | `io.streamlit4j.examples.ShowcaseDemo` |

## Run

From the repository root, after `./mvnw -DskipTests install`:

```sh
./mvnw -pl examples/spring-boot -q exec:java \
    -Dexec.mainClass=io.streamlit4j.examples.spring.hello.SpringBootHelloApp
```

Then open <http://localhost:8080/streamlit>.

Swap `SpringBootHelloApp` for any of the other classes in the table to launch the
corresponding demo.

## Launcher shape

Each launcher is a tiny `@SpringBootApplication` that exposes a single
`EntrypointSource` bean pointing at one of the render scripts:

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

The Starter wires the WebSocket endpoint, applies `streamlit4j.base-path`, and lets the
rest of the Spring Boot app keep its existing Security / Session configuration. See
[Spring Boot Integration](../../docs/public/guide/spring-boot.md) for the full
configuration surface.

## Where to go next

- Embedded-server variant of the same demos: [`../embedded`](../embedded)
- Walkthrough including build prerequisites: [Run from source](../../docs/public/guide/run-from-source.md)
- Spring Boot Integration guide: [`spring-boot.md`](../../docs/public/guide/spring-boot.md)
