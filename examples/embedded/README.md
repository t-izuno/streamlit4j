# streamlit4j examples (embedded)

Standalone demos that boot the embedded `Streamlit4jServer` (Jetty) directly from a
plain Java `main`. Use this form when you want the smallest possible launcher with no
framework around it — the canonical reference for **adoption path A** in the project
README.

## What is in here

Each row links the launcher class to its source and the demonstrated `St.*` elements to
the matching [Reference](../../docs/public/reference/overview.md) page.

| Class | Demonstrates |
| --- | --- |
| [`Hello`](src/main/java/io/streamlit4j/examples/Hello.java) | [text](../../docs/public/reference/text.md) (title, markdown), [inputs](../../docs/public/reference/inputs.md) (slider, button), [status](../../docs/public/reference/status.md) (metric, toast) |
| [`WidgetsDemo`](src/main/java/io/streamlit4j/examples/WidgetsDemo.java) | [inputs](../../docs/public/reference/inputs.md) — text / number / select / radio / checkbox / slider / date / time / colorPicker / button |
| [`LayoutDemo`](src/main/java/io/streamlit4j/examples/LayoutDemo.java) | [layout](../../docs/public/reference/layout.md) (columns / container / expander / tabs / sidebar), [forms](../../docs/public/reference/forms.md) |
| [`DataDemo`](src/main/java/io/streamlit4j/examples/DataDemo.java) | [data](../../docs/public/reference/data.md) (dataframe), [charts](../../docs/public/reference/charts.md) (line / bar / area / scatter), [status](../../docs/public/reference/status.md) (metric), [cache](../../docs/public/reference/cache.md) |
| [`ChatDemo`](src/main/java/io/streamlit4j/examples/ChatDemo.java) | [text](../../docs/public/reference/text.md) (markdown), [inputs](../../docs/public/reference/inputs.md) (textInput, button), [forms](../../docs/public/reference/forms.md), [control](../../docs/public/reference/control.md) (state) |
| [`ComponentDemo`](src/main/java/io/streamlit4j/examples/ComponentDemo.java) | [components](../../docs/public/reference/components.md) — custom in-process React renderer (star-rating) |
| [`ShowcaseDemo`](src/main/java/io/streamlit4j/examples/ShowcaseDemo.java) | All of the above behind a sidebar selector. Recommended quick-look entry point. |

## Run

From the repository root, after `./mvnw -DskipTests install`:

```sh
# 8501 is the listen port (optional positional argument, default 8501)
./mvnw -pl examples/embedded -q exec:java \
    -Dexec.mainClass=io.streamlit4j.examples.ShowcaseDemo \
    -Dexec.args=8501
```

Then open <http://localhost:8501>. The startup banner printed by
[`Streamlit4jServer.start()`](../../server/src/main/java/io/streamlit4j/server/Streamlit4jServer.java)
also prints the URL.

Swap `ShowcaseDemo` for any other class in the table to launch that single demo
directly without the navigation sidebar.

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
