package io.streamlit4j.examples;

import io.streamlit4j.core.api.St;
import io.streamlit4j.server.Streamlit4jServer;

/**
 * Minimal "hello world" script demonstrating title / markdown / slider / button. Runnable as
 * {@code java -cp <classpath> io.streamlit4j.examples.Hello [port]}.
 */
public final class Hello {

    private static final int DEFAULT_PORT = 8501;

    private Hello() {
    }

    /** Renders the demo. Invoked once per session by the runtime. */
    public static void run() {
        St.title("Hello, streamlit4j!");
        St.markdown("This is the **streamlit4j** PoC sample.");
        int year = St.slider("Year", 2018, 2026, 2025);
        St.metric("Selected year", year);
        if (St.button("Greet")) {
            St.toast("Hello from year " + year);
        }
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
        try (Streamlit4jServer server = new Streamlit4jServer(port, () -> Hello::run)) {
            server.start();
            Thread.currentThread().join();
        }
    }
}
