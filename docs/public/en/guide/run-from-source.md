# Run from source

The quickest path to clone the repository and launch one of the bundled
examples. Time required: 5–10 minutes.

> **Who this is for**: developers who want to run streamlit4j locally for
> evaluation, sanity checks, or to contribute.

## Prerequisites

| Tool | Version | Verification command |
| --- | --- | --- |
| JDK | 21 LTS (22 / 23 / 24 also work. **25 is not supported**) | `java -version` |
| Git | Any | `git --version` |
| Network | HTTPS outbound to Maven Central | — |

A standalone Maven installation is not required. The repository ships with
`./mvnw` (Maven Wrapper), which fetches the required Maven version
automatically.

### If you do not have JDK 21

The fastest route is via [SDKMAN!](https://sdkman.io/):

```sh
sdk install java 21.0.9-librca
sdk use java 21.0.9-librca
```

The repository root includes a `.sdkmanrc`, so if you have SDKMAN!'s
`auto_env` setting enabled, the JDK is switched automatically on `cd`.

## Step 1: Clone

```sh
git clone https://github.com/t-izuno/streamlit4j.git
cd streamlit4j
```

## Step 2: Build

```sh
./mvnw -DskipTests install
```

| Option | Effect |
| --- | --- |
| `-DskipTests` | Skip unit tests for faster turnaround when you only need to launch the app (a full `mvn verify` takes several minutes) |
| `install` | Places each module jar into the local `~/.m2/repository` so the examples module can resolve its dependencies |

The first run includes the frontend build (React + Vite) and the Maven
dependency download, taking 2–5 minutes. Subsequent runs are incremental and
finish in tens of seconds.

## Step 3: Launch an example

The examples module ships both Library-form (own `main`) and Spring Boot Starter-form
launchers for every demo. Both forms render the same UI.

### Path A (Library / own `main`)

By default `ShowcaseDemo` (a sidebar-driven hub linking to every other demo) starts on
port 8501:

```sh
./mvnw -pl examples/embedded -q exec:java
```

To launch a single demo directly, pass `-Dexec.mainClass` and optionally
`-Dexec.args=<port>`:

```sh
./mvnw -pl examples/embedded -q exec:java \
    -Dexec.mainClass=io.streamlit4j.examples.Hello \
    -Dexec.args=8501
```

Available `mainClass` values:

| Main class | Elements covered |
| --- | --- |
| `io.streamlit4j.examples.Hello` | title / markdown / slider / metric / button |
| `io.streamlit4j.examples.WidgetsDemo` | text / number / select / radio / checkbox / date / time / colorPicker |
| `io.streamlit4j.examples.LayoutDemo` | columns / container / expander / tabs / form (sidebar is showcased by ShowcaseDemo's own navigation) |
| `io.streamlit4j.examples.DataDemo` | dataframe / line / bar / area / scatter / metric / cache |
| `io.streamlit4j.examples.ChatDemo` | textInput / form / state for an echo bot |
| `io.streamlit4j.examples.ComponentDemo` | Custom components (star-rating) |
| `io.streamlit4j.examples.ShowcaseDemo` | Sidebar hub linking to every other demo (the default) |

On startup you will see:

```text
Local URL: http://localhost:8501
SSE: http://localhost:8501/events
WebSocket fallback: ws://localhost:8501/ws
```

### Path B (Spring Boot Starter)

By default `SpringBootShowcaseApp` (the Spring Boot variant of the sidebar hub)
starts:

```sh
./mvnw -pl examples/spring-boot -q exec:java
```

Each demo lives in its own sub-package
(`io.streamlit4j.examples.spring.{hello,widgets,layout,data,chat,component,showcase}`).
To launch a single demo directly, pass `-Dexec.mainClass`:

```sh
./mvnw -pl examples/spring-boot -q exec:java \
    -Dexec.mainClass=io.streamlit4j.examples.spring.hello.SpringBootHelloApp
```

The app mounts under `${streamlit4j.base-path}` (default `/streamlit4j`), so open
`http://localhost:8080/streamlit4j`.

## Step 4: Open in a browser

For Path A open <http://localhost:8501>; for Path B open <http://localhost:8080/streamlit4j>.

Sanity checks (for `Hello`):

- Moving the slider updates the metric in real time
- Pressing the **Greet** button triggers a toast notification
- The browser DevTools **Network** tab shows the JSON envelopes flowing
  through `ws://...`

Stop the server with `Ctrl+C` in the launching shell.

## Troubleshooting

| Symptom | Cause / Fix |
| --- | --- |
| Fails with `enforcer requires JDK 21 LTS` | JDK is out of range (e.g. 25). Switch with `sdk use java 21.0.9-librca` |
| `Address already in use` | Another process is holding 8501 / 8080. Switch with e.g. `-Dexec.args=8502` |
| `ClassNotFoundException: io.streamlit4j...` | Step 2's `install` was skipped, so dependencies are not in place. Run `./mvnw -DskipTests install` first |
| Server starts but the page is blank | The frontend may not be bundled. Rebuild with `./mvnw -pl frontend-assets clean install` |

## Next steps

- [Getting Started](./getting-started) — write your own scripts / embed in Spring Boot
- [Spring Boot Integration](./spring-boot) — mount on an existing web application
- [Reference](../reference/overview) — full API listing
