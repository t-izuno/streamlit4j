package io.streamlit4j.examples;

import io.streamlit4j.core.api.St;
import io.streamlit4j.core.domain.SessionState;
import io.streamlit4j.server.Streamlit4jServer;

/**
 * Sidebar-driven hub that lets the visitor pick any of the bundled demos and see it rendered in place. Each navigation
 * entry delegates to the matching demo's own {@code run()}, so the hub stays in sync with whatever those demos render
 * when launched standalone. Runnable as {@code java -cp <classpath> io.streamlit4j.examples.ShowcaseDemo [port]}.
 */
public final class ShowcaseDemo {

    private static final int DEFAULT_PORT = 8501;
    private static final String STATE_KEY = "showcase_nav";

    private static final String NAV_HELLO = "hello";
    private static final String NAV_CHAT = "chat";
    private static final String NAV_FAKE_LLM_CHAT = "fake_llm_chat";
    private static final String NAV_WIDGETS = "widgets";
    private static final String NAV_LAYOUT = "layout";
    private static final String NAV_DATA = "data";
    private static final String NAV_COMPONENT = "component";
    private static final String NAV_ABOUT = "about";

    private ShowcaseDemo() {
    }

    /** Renders the demo. Invoked once per session by the runtime. */
    public static void run() {
        SessionState state = St.state();

        St.sidebar(() -> {
            St.header("streamlit4j");
            St.markdown("Interactive data apps, written in idiomatic Java.");
            St.divider();

            St.markdown("##### Get started");
            if (St.button("Hello — slider + button + toast")) {
                state.put(STATE_KEY, NAV_HELLO);
            }
            if (St.button("Chat (echo bot)")) {
                state.put(STATE_KEY, NAV_CHAT);
            }
            if (St.button("Fake LLM chat")) {
                state.put(STATE_KEY, NAV_FAKE_LLM_CHAT);
            }

            St.markdown("##### Element catalog");
            if (St.button("Input widgets")) {
                state.put(STATE_KEY, NAV_WIDGETS);
            }
            if (St.button("Layout primitives")) {
                state.put(STATE_KEY, NAV_LAYOUT);
            }
            if (St.button("Data & charts")) {
                state.put(STATE_KEY, NAV_DATA);
            }
            if (St.button("Custom component")) {
                state.put(STATE_KEY, NAV_COMPONENT);
            }

            St.divider();
            if (St.button("About")) {
                state.put(STATE_KEY, NAV_ABOUT);
            }
            St.markdown("[GitHub](https://github.com/t-izuno/streamlit4j)");
        });

        String selected = state.get(STATE_KEY, String.class).orElse(NAV_HELLO);
        switch (selected) {
            case NAV_CHAT -> ChatDemo.run();
            case NAV_FAKE_LLM_CHAT -> FakeLlmChatDemo.run();
            case NAV_WIDGETS -> WidgetsDemo.run();
            case NAV_LAYOUT -> LayoutDemo.run();
            case NAV_DATA -> DataDemo.run();
            case NAV_COMPONENT -> ComponentDemo.run();
            case NAV_ABOUT -> renderAbout();
            default -> Hello.run();
        }
    }

    private static void renderAbout() {
        St.title("About streamlit4j");
        St.markdown("""
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
     * @param args
     *            optional single positional argument: the listen port (default {@value #DEFAULT_PORT})
     *
     * @throws Exception
     *             when the server fails to start
     */
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        try (Streamlit4jServer server = new Streamlit4jServer(port, () -> ShowcaseDemo::run)) {
            server.start();
            Thread.currentThread().join();
        }
    }
}
