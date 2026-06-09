# Run from source

The quickest path to clone the repository and launch the bundled demo without
installing JBang. Time required: 5–10 minutes.

> **Who this is for**: developers who want to run streamlit4j locally for
> evaluation, sanity checks, or to contribute.
> For one-liner startup via JBang, see [Installation](./installation#jbang).

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
| `install` | Places each module jar into the local `~/.m2/repository` so the `cli` module can resolve its dependencies |

The first run includes the frontend build (React + Vite) and the Maven
dependency download, taking 2–5 minutes. Subsequent runs are incremental and
finish in tens of seconds.

## Step 3: Launch the demo

```sh
# 8501 is the listen port (change to any free port you like)
java -jar cli/target/streamlit4j-cli-0.1.0-SNAPSHOT.jar 8501
```

The `cli` module is packaged as an executable jar (fat jar) with all
dependencies bundled via `maven-shade-plugin`, so no additional classpath
configuration is needed.

On startup you will see:

```text
streamlit4j listening on ws://localhost:8501/ws
```

## Step 4: Open in a browser

Open <http://localhost:8501> to see the contents of `examples/Hello.java`
(title / markdown / slider / metric / button).

Sanity checks:

- Moving the slider updates the metric in real time
- Pressing the **Greet** button triggers a toast notification
- The browser DevTools **Network** tab shows the JSON envelopes flowing
  through `ws://localhost:8501/ws`

Stop the server with `Ctrl+C` in the launching shell.

## Edit cycle (optional)

To push auto-reload notifications to the frontend when you edit a script,
add `--watch`:

```sh
java -jar cli/target/streamlit4j-cli-0.1.0-SNAPSHOT.jar 8501 --watch examples/src/main/java
```

When a file under `examples/src/main/java` changes, a `source_change:<path>`
notification is broadcast to every connected client and the frontend
reloads. (Class recompilation still has to happen separately.)

## Troubleshooting

| Symptom | Cause / Fix |
| --- | --- |
| Fails with `enforcer requires JDK 21 LTS` | JDK is out of range (e.g. 25). Switch with `sdk use java 21.0.9-librca` |
| `Address already in use` | Another process is holding 8501. Change to e.g. `-Dexec.args=8502` |
| `no main manifest attribute` / `ClassNotFoundException` | Step 2's `install` was skipped, so the fat jar has not been produced. Run `./mvnw -DskipTests install` first |
| Server starts but the page is blank | The frontend may not be bundled. Rebuild with `./mvnw -pl frontend-assets clean install` |
| Restart needed on every edit | `--watch` is not set, or the watched directory is wrong |

## Next steps

- [Getting Started](./getting-started) — write your own scripts / embed in Spring Boot
- [Spring Boot Integration](./spring-boot) — mount on an existing web application
- [Reference](../reference/overview) — full API listing
