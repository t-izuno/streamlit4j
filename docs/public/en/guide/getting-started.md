# Getting Started

A walkthrough for running streamlit4j and evaluating it firsthand. Time required: 10–20 minutes.

## Prerequisites

| Tool | Version | Purpose |
| --- | --- | --- |
| JDK | 21 LTS (24 or below also works) | Required at runtime. JDK 25 is unsupported due to formatter internal API incompatibility |
| Maven Wrapper | `./mvnw` bundled with the repository | No additional install needed as long as `JAVA_HOME` points at JDK 21 |
| Node.js | 22+ | Only when you want to modify the frontend |

> 0.1.0 has not yet been published to Maven Central. For now, place it in your local
> repository from source via `./mvnw -DskipTests install`. If you just want to launch
> the bundled examples from a clone, see [Run from source](./run-from-source).

## Choosing an evaluation track

| Track | Time required | What you can verify |
| --- | --- | --- |
| **A**: Pull it in as a library and write your own script | About 10 min | Feel of the API / ease of integration with your own logic |
| **B**: Mount on an existing app via the Spring Boot Starter | About 15 min | Spring Security / Session integration |
| **C**: Skim the feature catalog | About 5 min | Coverage of the provided widgets |

## A. Pull it in as a library

`pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>io.streamlit4j</groupId>
    <artifactId>streamlit4j-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </dependency>
  <dependency>
    <groupId>io.streamlit4j</groupId>
    <artifactId>streamlit4j-server</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </dependency>
</dependencies>
```

`src/main/java/com/example/MyApp.java`:

```java
package com.example;

import io.streamlit4j.core.api.St;
import io.streamlit4j.server.Streamlit4jServer;

public final class MyApp {

  public static void main(String[] args) throws Exception {
    try (var server = new Streamlit4jServer(8501, () -> MyApp::render)) {
      server.start();
      System.out.println("Open http://localhost:" + server.port());
      Thread.currentThread().join();
    }
  }

  static void render() {
    St.title("Sales dashboard");
    St.markdown("Pick a target month:");
    int month = St.slider("Month", 1, 12, 6);
    St.metric("Selected month", month);
    if (St.button("Submit")) {
      St.toast("Submitted month " + month);
    }
  }
}
```

Run it:

```sh
./mvnw -q exec:java -Dexec.mainClass=com.example.MyApp
```

Verification points:

- After editing `render()`, does the UI change with just a restart?
- When you open a separate tab, does the session carry independent state (i.e. the sliders are not locked to the same position)?

## B. Try the Spring Boot Starter

See [Spring Boot Integration](./spring-boot) for details. The essentials:

```xml
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-spring-boot-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

```yaml
streamlit4j:
  base-path: /streamlit4j
```

Declaring a single `@Bean EntrypointSource` is enough to run it under `${streamlit4j.base-path}`.

Verification points:

- Does access control via your existing Spring Security `SecurityFilterChain` take effect?
- When combined with Spring Session (Redis, etc.), does destroying the HTTP session
  cascade into the streamlit4j session disappearing as well?

## C. Skim the feature catalog

The `examples/` directory contains samples for the major features. Each demo can be
launched in either Path A form (`io.streamlit4j.examples.<Name>` under
`examples/embedded`) or Path B form
(`io.streamlit4j.examples.spring.<name>.SpringBoot<Name>App` under
`examples/spring-boot`).

| Demo | Elements you can verify |
| --- | --- |
| `Hello` | title / markdown / slider / metric / button / toast |
| `WidgetsDemo` | text / number / select / radio / checkbox / button / slider / date / time / colorPicker |
| `LayoutDemo` | columns / container / expander / tabs / sidebar / form |
| `DataDemo` | dataframe / line / bar / area / scatter / metric / cache |
| `ComponentDemo` | Custom components (star-rating) |
| `ShowcaseDemo` | All categories in one sidebar-driven showcase |

For a clone-and-launch walkthrough, see [Run from source](./run-from-source).

## Understanding the rerun model

For each WebSocket session, `render()` runs on a **virtual thread**. When a widget changes:

1. Update the session state with the new value
2. Rerun `render()` (serialized within the same session)
3. Diff against the previous render tree (keyed) and send a `render_delta`
4. The frontend applies the patch

This is the same model as Streamlit, where rewriting the script alone is enough to handle state management.

## Evaluation checklist

Points to consider when deciding on adoption.

- [ ] Are the widgets you need available? (See [Reference](../reference/overview) for coverage)
- [ ] Are the charts sufficient for your use case? (v1 only ships placeholder rendering)
- [ ] Does performance meet your requirements? (assumes virtual threads; one thread per session)
- [ ] Is the security model consistent with your internal policies?
      (Spring Security integration supported; custom components are in-house only)
- [ ] Is the license (MIT) acceptable under your internal standards?
- [ ] Are the constraints (README §"Constraints") within an acceptable range?

## Next steps

- [Reference](../reference/overview) — Full API listing
- [Custom Components Guide](./custom-components) — Adding your own React parts
- [Spring Boot Integration](./spring-boot) — auto-config and Session / Security integration
