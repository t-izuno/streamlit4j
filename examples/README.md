# streamlit4j examples

Sample applications for the two streamlit4j adoption paths.

| Sub-project | Adoption path | Use case |
| --- | --- | --- |
| [`embedded/`](embedded) | A. Library + embedded server | Tiny standalone `main` that boots `Streamlit4jServer` (Jetty) directly |
| [`spring-boot/`](spring-boot) | B. Spring Boot Starter | `@SpringBootApplication` launchers that mount the same demos under `${streamlit4j.base-path}` |

Both sub-projects ship the **same seven demos** —
[`Hello`](embedded/src/main/java/io/streamlit4j/examples/Hello.java),
[`WidgetsDemo`](embedded/src/main/java/io/streamlit4j/examples/WidgetsDemo.java),
[`LayoutDemo`](embedded/src/main/java/io/streamlit4j/examples/LayoutDemo.java),
[`DataDemo`](embedded/src/main/java/io/streamlit4j/examples/DataDemo.java),
[`ChatDemo`](embedded/src/main/java/io/streamlit4j/examples/ChatDemo.java) (echo bot),
[`ComponentDemo`](embedded/src/main/java/io/streamlit4j/examples/ComponentDemo.java),
[`ShowcaseDemo`](embedded/src/main/java/io/streamlit4j/examples/ShowcaseDemo.java)
(sidebar hub linking to the rest). Only the launcher differs, so you can compare the
two integration shapes side by side. The render scripts (`<Demo>::run`) live in
`embedded/` and are reused by `spring-boot/` via a Maven dependency.

Neither sub-project is published to Maven Central; they exist for local evaluation and
as the source of truth for the launcher shapes shown in the project README.

## Quickstart

```sh
# from the repository root
./mvnw -DskipTests install

# Path A (embedded server) — defaults to ShowcaseDemo (sidebar-driven hub)
./mvnw -pl examples/embedded -q exec:java

# Path B (Spring Boot) — defaults to SpringBootShowcaseApp, mounted at /streamlit4j
./mvnw -pl examples/spring-boot -q exec:java
```

Full prerequisites and troubleshooting:
[Run from source](../docs/public/guide/run-from-source.md).
