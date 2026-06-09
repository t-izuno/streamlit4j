# streamlit4j examples (embedded)

Standalone demos that boot the embedded `Streamlit4jServer` (Jetty) directly from a
plain Java `main`. Use this form when you want the smallest possible launcher with no
framework around it — the canonical reference for **adoption path A** in the project
README.

## What is in here

| Class | Demonstrates |
| --- | --- |
| `io.streamlit4j.examples.Hello` | title / markdown / slider / metric / button / toast |
| `io.streamlit4j.examples.WidgetsDemo` | text / number / select / radio / checkbox / button / slider / date / time / colorPicker |
| `io.streamlit4j.examples.LayoutDemo` | columns / container / expander / tabs / sidebar / form |
| `io.streamlit4j.examples.DataDemo` | dataframe / line / bar / area / scatter / metric / cache |
| `io.streamlit4j.examples.ComponentDemo` | Custom components (star-rating) |
| `io.streamlit4j.examples.ShowcaseDemo` | All categories in one sidebar-driven showcase |

## Run

From the repository root, after `./mvnw -DskipTests install`:

```sh
# 8501 is the listen port (optional positional argument, default 8501)
./mvnw -pl examples/embedded -q exec:java \
    -Dexec.mainClass=io.streamlit4j.examples.Hello \
    -Dexec.args=8501
```

Then open <http://localhost:8501>.

Swap `Hello` for any of the other classes in the table to launch the corresponding demo.

## Launcher shape

Each demo is a `final class` with two static methods:

```java
public static void run() {
    St.title("...");
    // ... St.* calls
}

public static void main(String[] args) throws Exception {
    int port = args.length > 0 ? Integer.parseInt(args[0]) : 8501;
    try (Streamlit4jServer server = new Streamlit4jServer(port, () -> Hello::run)) {
        server.start();
        Thread.currentThread().join();
    }
}
```

`run()` is the render script; `main()` is the launcher. Copy this shape into your own
project to adopt streamlit4j as a library — see the project README option A for the
matching Maven dependencies.

## Where to go next

- Spring Boot variant of the same demos: [`../spring-boot`](../spring-boot)
- Walkthrough including build prerequisites: [Run from source](../../docs/public/guide/run-from-source.md)
- API reference: [Reference overview](../../docs/public/reference/overview.md)
