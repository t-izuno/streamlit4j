# streamlit4j examples (Spring Boot)

Spring Boot launchers that mount the same demos under `${streamlit4j.base-path}`
(default `/streamlit4j`) using the streamlit4j Spring Boot Starter. Use this form when
you want streamlit4j to coexist with the rest of a Spring Boot app — the canonical
reference for **adoption path B** in the project README.

The render scripts (`Hello::run`, `WidgetsDemo::run`, …) live in
[`../embedded`](../embedded) and are pulled in via the `streamlit4j-examples-embedded`
Maven dependency, so the two sub-projects always render the same content.

## What is in here

Each launcher lives in its own sub-package so that `@SpringBootApplication`'s component
scan finds exactly one entrypoint bean per app. The "Demonstrates" column links the
matching [Reference](../../docs/public/reference/overview.md) page for each
demonstrated `St.*` element.

| Class | Wraps | Demonstrates |
| --- | --- | --- |
| [`SpringBootHelloApp`](src/main/java/io/streamlit4j/examples/spring/hello/SpringBootHelloApp.java) | [`Hello`](../embedded/src/main/java/io/streamlit4j/examples/Hello.java) | [text](../../docs/public/reference/text.md), [inputs](../../docs/public/reference/inputs.md), [status](../../docs/public/reference/status.md) |
| [`SpringBootWidgetsApp`](src/main/java/io/streamlit4j/examples/spring/widgets/SpringBootWidgetsApp.java) | [`WidgetsDemo`](../embedded/src/main/java/io/streamlit4j/examples/WidgetsDemo.java) | [inputs](../../docs/public/reference/inputs.md) |
| [`SpringBootLayoutApp`](src/main/java/io/streamlit4j/examples/spring/layout/SpringBootLayoutApp.java) | [`LayoutDemo`](../embedded/src/main/java/io/streamlit4j/examples/LayoutDemo.java) | [layout](../../docs/public/reference/layout.md), [forms](../../docs/public/reference/forms.md) |
| [`SpringBootDataApp`](src/main/java/io/streamlit4j/examples/spring/data/SpringBootDataApp.java) | [`DataDemo`](../embedded/src/main/java/io/streamlit4j/examples/DataDemo.java) | [data](../../docs/public/reference/data.md), [charts](../../docs/public/reference/charts.md), [cache](../../docs/public/reference/cache.md) |
| [`SpringBootChatApp`](src/main/java/io/streamlit4j/examples/spring/chat/SpringBootChatApp.java) | [`ChatDemo`](../embedded/src/main/java/io/streamlit4j/examples/ChatDemo.java) | [forms](../../docs/public/reference/forms.md), [control](../../docs/public/reference/control.md) (state) |
| [`SpringBootComponentApp`](src/main/java/io/streamlit4j/examples/spring/component/SpringBootComponentApp.java) | [`ComponentDemo`](../embedded/src/main/java/io/streamlit4j/examples/ComponentDemo.java) | [components](../../docs/public/reference/components.md) |
| [`SpringBootShowcaseApp`](src/main/java/io/streamlit4j/examples/spring/showcase/SpringBootShowcaseApp.java) | [`ShowcaseDemo`](../embedded/src/main/java/io/streamlit4j/examples/ShowcaseDemo.java) | All of the above behind a sidebar selector. Recommended quick-look entry point. |

## Run

From the repository root, after `./mvnw -DskipTests install`:

```sh
./mvnw -pl examples/spring-boot -q exec:java \
    -Dexec.mainClass=io.streamlit4j.examples.spring.showcase.SpringBootShowcaseApp
```

Then open <http://localhost:8080/streamlit4j>. The streamlit4j-specific startup banner
printed by
[`Streamlit4jStartupBanner`](../../spring-boot-starter/src/main/java/io/streamlit4j/springboot/Streamlit4jStartupBanner.java)
also prints the URL after Spring Boot is ready.

Swap `SpringBootShowcaseApp` for any other class in the table to launch that single
demo directly without the navigation sidebar.

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
