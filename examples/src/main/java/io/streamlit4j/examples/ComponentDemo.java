package io.streamlit4j.examples;

import io.streamlit4j.core.api.St;
import io.streamlit4j.core.domain.CustomComponent;
import java.util.Map;

/**
 * Demonstrates the in-process custom component path: a React renderer bundled
 * with the SPA and registered through {@code component-builtins.ts}, paired
 * with a {@link CustomComponent} declaration on the Java side.
 *
 * <p>Pair with a {@link io.streamlit4j.server.Streamlit4jServer} instance, e.g.
 *
 * <pre>{@code
 * try (var server = new Streamlit4jServer(8501, () -> ComponentDemo::run)) {
 *   server.start();
 *   Thread.currentThread().join();
 * }
 * }</pre>
 *
 * <p>iframe-isolated components are not supported (see {@code docs/design.md}
 * §9 for the rationale).
 */
public final class ComponentDemo {

    private static final CustomComponent<Integer> STAR_RATING =
            St.registerComponent(new CustomComponent<>("star-rating", Integer.class));

    private ComponentDemo() {}

    /** Renders the demo. Invoked once per session by the runtime. */
    public static void run() {
        St.title("Custom components");
        St.markdown("An in-process React renderer registered on both the Java and TypeScript sides.");

        St.header("In-process: star rating");
        int rating = St.component(STAR_RATING, Map.of("label", "Rate this demo", "max", 5), 0);
        St.write("Current rating: " + rating);
    }
}
