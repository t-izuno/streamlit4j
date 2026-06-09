# streamlit4j

> 日本語: [README.ja.md](./README.ja.md)

An interactive data-app and dashboard framework for Java. streamlit4j brings the same
"script re-run + automatic re-render" model that Streamlit Python is known for to the JVM.

> **Independent community open-source software.** streamlit4j is not affiliated with,
> endorsed by, or sponsored by Snowflake, Inc. or the Streamlit project. The name
> "Streamlit" appears within "streamlit4j" solely as nominative fair use to describe
> this project's design lineage; "Streamlit" is a trademark of its respective owner
> and no trademark claim is asserted by this project.

## What it does

Compose static calls on `St.*` and a WebSocket + React UI is produced for you.

```java
import io.streamlit4j.core.api.St;

public final class SalesDashboard {
  public static void run() {
    St.title("Sales dashboard");
    int year = St.slider("Year", 2020, 2030, 2026);
    St.metric("Selected", year);
    St.lineChart(loadSales(year));
  }
}
```

Categories provided (full list: [Reference overview](docs/public/reference/overview.md)):

| Category | Main elements |
| --- | --- |
| Text | title / header / markdown / write / code / latex / html / divider |
| Status | metric / toast / progress / spinner / status |
| Tables & charts | dataframe / table / data_editor / line / bar / area / scatter |
| Inputs | slider / textInput / selectbox / button / date / time / colorPicker and 14 more |
| Files | fileUploader / downloadButton (bytes) / downloadCsv / downloadJson |
| Layout | columns / container / expander / tabs / sidebar / empty |
| Other | form / cache / pages / custom components / rerun / state |

## How to adopt (two options)

streamlit4j is a library — you write your own Java code and the framework drives the UI. Pick the form that fits your scenario.

| Form | Use case | Entry point |
| --- | --- | --- |
| **A. Library (core + server)** | Embed in an existing Java project / launch from your own `main` | Add `streamlit4j-core` + `streamlit4j-server` as dependencies |
| **B. Spring Boot Starter** | Mount as one feature of a Spring Boot app | Add `streamlit4j-spring-boot-starter` as a dependency |

> Want to see it running first? Clone the repo and pick an example —
> [`examples/embedded`](examples/embedded) ships standalone `main` launchers (Library form)
> and [`examples/spring-boot`](examples/spring-boot) ships matching `SpringBoot<Name>App`
> launchers (Spring Boot form) for the same six demos. See
> [Run from source](docs/public/guide/run-from-source.md).

### A. Library (for your own script)

`pom.xml`:

```xml
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-core</artifactId>
  <version>0.1.0</version>
</dependency>
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-server</artifactId>
  <version>0.1.0</version>
</dependency>
```

`main`:

```java
import io.streamlit4j.core.api.St;
import io.streamlit4j.server.Streamlit4jServer;

public final class App {
  public static void main(String[] args) throws Exception {
    try (var server = new Streamlit4jServer(8501, () -> App::render)) {
      server.start();
      Thread.currentThread().join();
    }
  }
  static void render() {
    St.title("Hello");
    St.write("Hello, " + St.textInput("Name", "world"));
  }
}
```

### B. Spring Boot Starter (mount inside an existing web app)

```xml
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-spring-boot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

`application.yml`:

```yaml
streamlit4j:
  base-path: /streamlit4j       # default /streamlit4j
```

Declare one `@Bean EntrypointSource` and your script is mounted with full Spring Security /
Spring Session integration. See [Spring Boot Integration](docs/public/guide/spring-boot.md) for details.

## Modules

| Maven coordinate | Role |
| --- | --- |
| `io.streamlit4j:streamlit4j-core` | Framework-agnostic execution engine |
| `io.streamlit4j:streamlit4j-server` | Embedded Jetty + WebSocket |
| `io.streamlit4j:streamlit4j-frontend-assets` | Pre-built frontend bundled in classpath |
| `io.streamlit4j:streamlit4j-spring-boot-starter` | Spring Boot auto-configuration |
| `io.streamlit4j:streamlit4j-examples-embedded` | Standalone demos launched via own `main` + embedded `Streamlit4jServer` |
| `io.streamlit4j:streamlit4j-examples-spring-boot` | Spring Boot launchers that mount the embedded demos |

## Constraints

Things you should know before adopting.

- **Java 21 LTS or newer is required.** Virtual threads are mandatory; JDK 17 and below are not supported.
- **Protocol is fixed to JSON.** MessagePack and similar are not supported (a v1.x consideration).
- **Charts render placeholders in v1.** A real charting library is on the backlog.
- **`dataEditor` is one-way.** Edited values are not propagated back to the server yet.
- **Multi-page is explicit registration only.** No `pages/` directory convention.
- **Custom components are in-process only.** Iframe isolation is not supported.
- **GraalVM native is deferred to v1.x.**

## Documentation

- [Getting Started](docs/public/guide/getting-started.md) — library and Spring Boot adoption paths
- [Run from source](docs/public/guide/run-from-source.md) — clone the repo and launch the bundled showcase demo
- [Reference](docs/public/reference/overview.md) — Java signature, protocol envelope,
  and frontend rendering for every `St.*` element
- [Custom Components Guide](docs/public/guide/custom-components.md) — adding your own React renderers
- [Spring Boot Integration](docs/public/guide/spring-boot.md) — auto-config, Security, and Session interop

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for build, test, and pull-request guidelines.

## License

[MIT License](LICENSE)
