# Installation

streamlit4j is distributed via **Maven Central** under the `io.streamlit4j`
group ID. Pick the dependencies that match your run mode.

> **Status (0.1.0-SNAPSHOT)**: not yet published to Maven Central. For now,
> build locally with `mvn -DskipTests install` from a clone of the repository.
> Once 0.1.0 ships, the coordinates below will resolve directly.

## Maven coordinates

| Module | Coordinate | Use when |
| --- | --- | --- |
| `streamlit4j-core` | `io.streamlit4j:streamlit4j-core` | Always (public API & runtime) |
| `streamlit4j-server` | `io.streamlit4j:streamlit4j-server` | Standalone (embedded HTTP/WS server) |
| `streamlit4j-frontend-assets` | `io.streamlit4j:streamlit4j-frontend-assets` | Always — bundles the SPA jar |
| `streamlit4j-spring-boot-starter` | `io.streamlit4j:streamlit4j-spring-boot-starter` | Spring Boot host |
| `streamlit4j-cli` | `io.streamlit4j:streamlit4j-cli` | Running scripts via CLI / JBang |

All artifacts share the same version, so define it once via a BOM-style
property or import the parent `dependencyManagement`.

## Standalone (embedded server)

```xml
<properties>
  <streamlit4j.version>0.1.0</streamlit4j.version>
</properties>

<dependencies>
  <dependency>
    <groupId>io.streamlit4j</groupId>
    <artifactId>streamlit4j-server</artifactId>
    <version>${streamlit4j.version}</version>
  </dependency>
  <!-- Optional: write your render() in your own module -->
  <dependency>
    <groupId>io.streamlit4j</groupId>
    <artifactId>streamlit4j-core</artifactId>
    <version>${streamlit4j.version}</version>
  </dependency>
</dependencies>
```

`streamlit4j-server` transitively pulls `streamlit4j-core` and
`streamlit4j-frontend-assets` — declaring `core` separately is purely for
readability.

## Spring Boot

```xml
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-spring-boot-starter</artifactId>
  <version>${streamlit4j.version}</version>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

See the [Spring Boot Integration guide](./spring-boot) for wiring and
configuration properties.

## Gradle (Kotlin DSL)

```kotlin
dependencies {
  implementation("io.streamlit4j:streamlit4j-server:0.1.0")
  // または:
  // implementation("io.streamlit4j:streamlit4j-spring-boot-starter:0.1.0")
}
```

## JBang

For one-file experimentation without a full Maven project, install the CLI via
JBang once 0.1.0 lands:

```sh
jbang app install streamlit4j@t-izuno/streamlit4j
streamlit4j 8501
```

The catalog definition lives at the repository root (`jbang-catalog.json`).

## JDK requirements

| Component | JDK |
| --- | --- |
| Runtime | 21 LTS (24 以下も可) |
| Build (developers / contributors) | 21 LTS — JDK 25 は formatter 内部 API 非互換のため未サポート |

streamlit4j depends on virtual threads (JEP 444 / JDK 21) and pattern matching
for sealed types, so older JDKs are not supported and will not be.

## Snapshots (developers only)

`0.1.0-SNAPSHOT` builds are not published. To depend on the current `main`
branch:

```sh
git clone https://github.com/t-izuno/streamlit4j.git
cd streamlit4j
mvn -DskipTests install
# 後はあなたの pom.xml で version を 0.1.0-SNAPSHOT に
```

## Verifying integrity

Once 0.1.0 is published, each artifact ships with a detached GPG signature
(`*.asc`). To verify:

```sh
gpg --verify streamlit4j-core-0.1.0.jar.asc streamlit4j-core-0.1.0.jar
```
