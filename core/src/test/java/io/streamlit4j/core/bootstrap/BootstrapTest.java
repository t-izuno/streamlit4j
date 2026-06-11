package io.streamlit4j.core.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.port.SessionLifecycleListener;
import io.streamlit4j.core.runtime.InMemorySessionStore;
import org.junit.jupiter.api.Test;

class BootstrapTest {

    @Test
    void standaloneWiresUseCasesAndStores() throws Exception {
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            assertThat(app.startSession()).isNotNull();
            assertThat(app.processWidgetEvent()).isNotNull();
            assertThat(app.sessions()).isNotNull();
            assertThat(app.downloads()).isNotNull();
            assertThat(app.components()).isNotNull();
            assertThat(app.metrics().activeSessions()).isZero();
        }
    }

    @Test
    void standaloneRegistersListenerThatHandlesAllLifecycleEvents() throws Exception {
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            // Drive each lifecycle event so the registered structured-log listener's
            // switch branches are all exercised. No assertions beyond no-throw —
            // the structured logger is observable only via SLF4J output.
            Session session = app.sessions().create();
            assertThat(app.metrics().activeSessions()).isEqualTo(1);
            app.sessions().remove(session.id());
            assertThat(app.metrics().activeSessions()).isZero();
        }
    }

    @Test
    void standaloneListenerHandlesExpiredEventViaEvictIdle() throws Exception {
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            InMemorySessionStore underlying = (InMemorySessionStore) app.sessions();
            Session created = underlying.create();
            java.lang.reflect.Field f = Session.class.getDeclaredField("lastActivity");
            f.setAccessible(true);
            f.set(created, java.time.Instant.now().minus(java.time.Duration.ofDays(1)));
            int evicted = underlying.evictIdle();
            assertThat(evicted).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void loggingListenerHandlesAllEventKinds() {
        SessionLifecycleListener listener = Bootstrap.loggingListener();
        Session session = new Session("s-listener");
        for (SessionLifecycleListener.Event e : SessionLifecycleListener.Event.values()) {
            listener.on(e, session);
        }
    }
}
