# streamlit4j examples

Sample applications for the two streamlit4j adoption paths.

| Sub-project | Adoption path | Use case |
| --- | --- | --- |
| [`embedded/`](embedded) | A. Library + embedded server | Tiny standalone `main` that boots `Streamlit4jServer` (Jetty) directly |
| [`spring-boot/`](spring-boot) | B. Spring Boot Starter | `@SpringBootApplication` launchers that mount the same demos under `${streamlit4j.base-path}` |

Both sub-projects ship the **same six demos** — `Hello`, `WidgetsDemo`, `LayoutDemo`,
`DataDemo`, `ComponentDemo`, `ShowcaseDemo`. Only the launcher differs, so you can
compare the two integration shapes side by side. The render scripts (`<Demo>::run`)
live in `embedded/` and are reused by `spring-boot/` via a Maven dependency.

Neither sub-project is published to Maven Central; they exist for local evaluation and
as the source of truth for the launcher shapes shown in the project README.

## Quickstart

```sh
# from the repository root
./mvnw -DskipTests install

# Path A (embedded server)
./mvnw -pl examples/embedded -q exec:java \
    -Dexec.mainClass=io.streamlit4j.examples.Hello

# Path B (Spring Boot)
./mvnw -pl examples/spring-boot -q exec:java \
    -Dexec.mainClass=io.streamlit4j.examples.spring.hello.SpringBootHelloApp
```

Full prerequisites and troubleshooting:
[Run from source](../docs/public/guide/run-from-source.md).
