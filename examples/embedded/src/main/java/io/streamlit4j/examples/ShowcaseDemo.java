package io.streamlit4j.examples;

import io.streamlit4j.core.api.St;
import io.streamlit4j.server.Streamlit4jServer;
import java.util.List;

/**
 * Sidebar-driven hub that lets the visitor pick any of the bundled demos and
 * see it rendered in place. Each navigation entry delegates to the matching
 * demo's own {@code run()}, so the hub stays in sync with whatever those
 * demos render when launched standalone. Runnable as
 * {@code java -cp <classpath> io.streamlit4j.examples.ShowcaseDemo [port]}.
 */
public final class ShowcaseDemo {

    private static final int DEFAULT_PORT = 8501;

    private static final String NAV_HOME = "Home";
    private static final String NAV_HELLO = "Hello";
    private static final String NAV_WIDGETS = "Widgets";
    private static final String NAV_LAYOUT = "Layout";
    private static final String NAV_DATA = "Data";
    private static final String NAV_CHAT = "Chat (echo bot)";
    private static final String NAV_COMPONENT = "Custom component";
    private static final String NAV_ABOUT = "About";

    private static final List<String> PAGES =
            List.of(NAV_HOME, NAV_HELLO, NAV_WIDGETS, NAV_LAYOUT, NAV_DATA, NAV_CHAT, NAV_COMPONENT, NAV_ABOUT);

    private ShowcaseDemo() {}

    /** Renders the demo. Invoked once per session by the runtime. */
    public static void run() {
        String[] selected = {PAGES.get(0)};
        St.sidebar(() -> {
            St.title("streamlit4j");
            St.markdown("**An interactive data-app framework for the JVM.**");
            St.divider();
            selected[0] = St.radio("Demo", PAGES);
            St.divider();
            St.markdown("[GitHub](https://github.com/t-izuno/streamlit4j)");
        });

        switch (selected[0]) {
            case NAV_HELLO -> Hello.run();
            case NAV_WIDGETS -> WidgetsDemo.run();
            case NAV_LAYOUT -> LayoutDemo.run();
            case NAV_DATA -> DataDemo.run();
            case NAV_CHAT -> ChatDemo.run();
            case NAV_COMPONENT -> ComponentDemo.run();
            case NAV_ABOUT -> renderAbout();
            default -> renderHome();
        }
    }

    private static void renderHome() {
        St.title("streamlit4j showcase");
        St.markdown(
                """
                Pick a demo from the sidebar to see it rendered in place. Each entry
                in the sidebar delegates to the same Java class you can run by itself
                from the command line:

                - **Hello** — `io.streamlit4j.examples.Hello`
                - **Widgets** — `io.streamlit4j.examples.WidgetsDemo`
                - **Layout** — `io.streamlit4j.examples.LayoutDemo`
                - **Data** — `io.streamlit4j.examples.DataDemo`
                - **Chat (echo bot)** — `io.streamlit4j.examples.ChatDemo`
                - **Custom component** — `io.streamlit4j.examples.ComponentDemo`

                Open `examples/embedded/` or `examples/spring-boot/` in the repository
                to see how each demo is launched.
                """);
    }

    private static void renderAbout() {
        St.title("About streamlit4j");
        St.markdown(
                """
                **streamlit4j** is an independent community open-source project
                (MIT License). It is not affiliated with Snowflake, Inc. or the
                Streamlit project; the name "Streamlit" appears within
                "streamlit4j" solely as nominative fair use.

                ## Source code

                <https://github.com/t-izuno/streamlit4j>
                """);

        St.header("Runtime");
        St.columns(2, index -> {
            if (index == 0) {
                St.metric("Java", System.getProperty("java.version"));
            } else {
                St.metric("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
            }
        });
    }

    /**
     * Boots an embedded server that serves this demo on the given port.
     *
     * @param args optional single positional argument: the listen port (default {@value #DEFAULT_PORT})
     * @throws Exception when the server fails to start
     */
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        try (Streamlit4jServer server = new Streamlit4jServer(port, () -> ShowcaseDemo::run)) {
            server.start();
            Thread.currentThread().join();
        }
    }
}
