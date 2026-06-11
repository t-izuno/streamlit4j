package io.streamlit4j.examples;

import io.streamlit4j.core.api.St;
import io.streamlit4j.core.domain.CustomComponent;
import io.streamlit4j.server.Streamlit4jServer;
import java.util.Map;

/**
 * Demonstrates the in-process custom component path: a React renderer bundled with the SPA and registered through
 * {@code component-builtins.ts}, paired with a {@link CustomComponent} declaration on the Java side. Runnable as
 * {@code java -cp <classpath> io.streamlit4j.examples.ComponentDemo [port]}.
 * <p>
 * iframe-isolated components are not supported (see {@code docs/devel/design.md} §9 for the rationale).
 */
public final class ComponentDemo {

    private static final int DEFAULT_PORT = 8501;

    private static final CustomComponent<Integer> STAR_RATING = St
            .registerComponent(new CustomComponent<>("star-rating", Integer.class));

    private ComponentDemo() {
    }

    /** Renders the demo. Invoked once per session by the runtime. */
    public static void run() {
        St.title("Custom components");
        St.markdown("An in-process React renderer registered on both the Java and TypeScript sides.");

        St.header("In-process: star rating");
        int rating = St.component(STAR_RATING, Map.of("label", "Rate this demo", "max", 5), 0);
        St.write("Current rating: " + rating);
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
        try (Streamlit4jServer server = new Streamlit4jServer(port, () -> ComponentDemo::run)) {
            server.start();
            Thread.currentThread().join();
        }
    }
}
