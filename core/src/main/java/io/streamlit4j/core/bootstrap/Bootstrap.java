package io.streamlit4j.core.bootstrap;

import io.streamlit4j.core.application.ProcessWidgetEvent;
import io.streamlit4j.core.application.StartSession;
import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.core.port.SessionLifecycleListener;
import io.streamlit4j.core.runtime.ComponentRegistryAccess;
import io.streamlit4j.core.runtime.DownloadAccess;
import io.streamlit4j.core.runtime.InMemoryComponentRegistry;
import io.streamlit4j.core.runtime.InMemoryDownloadStore;
import io.streamlit4j.core.runtime.InMemorySessionStore;
import io.streamlit4j.core.runtime.ScriptRunner;
import io.streamlit4j.core.runtime.StructuredLog;

/**
 * Composition root that wires all in-memory adapters into a single {@link Streamlit4jApplication} for the standalone
 * (CLI / embedded) profile. The Spring Boot starter wires the same use cases against Spring-managed beans.
 */
public final class Bootstrap {

    private Bootstrap() {
    }

    /**
     * Builds a fully-wired application backed by in-memory stores.
     *
     * @param entrypoints
     *            source of script entrypoints
     *
     * @return ready-to-use application instance
     */
    public static Streamlit4jApplication standalone(EntrypointSource entrypoints) {
        ScriptRunner renderer = new ScriptRunner();
        InMemorySessionStore store = new InMemorySessionStore();
        InMemoryDownloadStore downloads = new InMemoryDownloadStore();
        DownloadAccess.use(downloads);
        InMemoryComponentRegistry components = new InMemoryComponentRegistry();
        ComponentRegistryAccess.use(components);
        store.addListener((event, session) -> {
            switch (event) {
                case CREATED -> StructuredLog.sessionCreated(session.id());
                case DESTROYED -> StructuredLog.sessionDestroyed(session.id());
                case EXPIRED -> StructuredLog.sessionExpired(session.id());
            }
        });
        StartSession start = new StartSession(store, entrypoints, renderer);
        ProcessWidgetEvent process = new ProcessWidgetEvent(store, entrypoints, renderer);
        return new Streamlit4jApplication(start, process, store, downloads, components, renderer);
    }

    /**
     * Returns a {@link SessionLifecycleListener} that emits structured log events on session create / destroy / expire
     * transitions.
     *
     * @return logging listener
     */
    public static SessionLifecycleListener loggingListener() {
        return (event, session) -> {
            switch (event) {
                case CREATED -> StructuredLog.sessionCreated(session.id());
                case DESTROYED -> StructuredLog.sessionDestroyed(session.id());
                case EXPIRED -> StructuredLog.sessionExpired(session.id());
            }
        };
    }
}
