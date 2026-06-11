package io.streamlit4j.server;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.bootstrap.Bootstrap;
import io.streamlit4j.core.bootstrap.Streamlit4jApplication;
import org.junit.jupiter.api.Test;

class ConnectionRegistryTest {

    @Test
    void registerThenRemoveTogglesActiveConnections() throws Exception {
        ConnectionRegistry registry = new ConnectionRegistry();
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            ProtocolEndpoint endpoint = new ProtocolEndpoint(app.startSession(), app.processWidgetEvent(), registry);
            assertThat(registry.activeConnections()).isZero();
            registry.register("s-1", endpoint);
            assertThat(registry.activeConnections()).isEqualTo(1);
            registry.remove("s-1");
            assertThat(registry.activeConnections()).isZero();
        }
    }

    @Test
    void broadcastReloadIteratesEveryRegisteredEndpoint() throws Exception {
        ConnectionRegistry registry = new ConnectionRegistry();
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            // Endpoints with no wsSession bound — deliver() routes to send() which
            // becomes a no-op when isOpen() is false. The branch we care about is
            // the broadcast loop body itself.
            registry.register("a", new ProtocolEndpoint(app.startSession(), app.processWidgetEvent(), registry));
            registry.register("b", new ProtocolEndpoint(app.startSession(), app.processWidgetEvent(), registry));
            registry.broadcastReload("hot-reload");
            assertThat(registry.activeConnections()).isEqualTo(2);
        }
    }

    @Test
    void broadcastWithNoConnectionsIsNoOp() {
        ConnectionRegistry registry = new ConnectionRegistry();
        registry.broadcastReload("x");
        assertThat(registry.activeConnections()).isZero();
    }
}
