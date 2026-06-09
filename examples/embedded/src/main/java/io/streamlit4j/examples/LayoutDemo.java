package io.streamlit4j.examples;

import io.streamlit4j.core.api.St;
import io.streamlit4j.server.Streamlit4jServer;
import java.util.List;

/**
 * Demonstrates the layout primitives: columns / container / expander / tabs /
 * sidebar / form. Runnable as
 * {@code java -cp <classpath> io.streamlit4j.examples.LayoutDemo [port]}.
 */
public final class LayoutDemo {

    private static final int DEFAULT_PORT = 8501;

    private LayoutDemo() {}

    /** Renders the demo. Invoked once per session by the runtime. */
    public static void run() {
        St.title("Layout primitives");
        St.markdown("Compose pages with **columns**, **tabs**, **expanders**, and **forms**.");

        St.sidebar(() -> {
            St.subheader("Sidebar");
            St.markdown("Settings or navigation typically live here.");
            St.checkbox("Show advanced", false);
        });

        St.header("Columns");
        St.columns(3, index -> {
            St.subheader("Column " + (index + 1));
            St.metric("Value", (index + 1) * 100);
        });

        St.header("Tabs");
        St.tabs(List.of("Overview", "Details", "Logs"), index -> {
            switch (index) {
                case 0 -> St.markdown("Summary card lives here.");
                case 1 -> St.markdown("Long-form detail body lives here.");
                default -> St.code("2026-06-01 12:00:00 INFO  started\n", "log");
            }
        });

        St.header("Expander");
        St.expander("Show diagnostics", () -> {
            St.json("{\"status\":\"ok\",\"latencyMs\":42}");
        });

        St.header("Form");
        St.form("login", () -> {
            String user = St.textInput("Username", "");
            String password = St.textInput("Password", "");
            if (St.formSubmitButton("Sign in")) {
                St.toast("Signed in as " + (user.isBlank() ? "(anonymous)" : user));
                St.write("password length: " + password.length());
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
        try (Streamlit4jServer server = new Streamlit4jServer(port, () -> LayoutDemo::run)) {
            server.start();
            Thread.currentThread().join();
        }
    }
}
