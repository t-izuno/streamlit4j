package io.streamlit4j.core.bootstrap;

import io.streamlit4j.core.application.ProcessWidgetEvent;
import io.streamlit4j.core.application.StartSession;
import io.streamlit4j.core.port.ComponentRegistry;
import io.streamlit4j.core.port.DownloadStore;
import io.streamlit4j.core.port.SessionStore;

/**
 * Fully-wired application instance produced by {@link Bootstrap}. Holds the
 * use cases and the shared adapters needed to serve a single process.
 */
public final class Streamlit4jApplication implements AutoCloseable {

    private final StartSession startSession;
    private final ProcessWidgetEvent processWidgetEvent;
    private final SessionStore sessions;
    private final DownloadStore downloads;
    private final ComponentRegistry components;
    private final AutoCloseable resourceCloser;

    Streamlit4jApplication(
            StartSession startSession,
            ProcessWidgetEvent processWidgetEvent,
            SessionStore sessions,
            DownloadStore downloads,
            ComponentRegistry components,
            AutoCloseable resourceCloser) {
        this.startSession = startSession;
        this.processWidgetEvent = processWidgetEvent;
        this.sessions = sessions;
        this.downloads = downloads;
        this.components = components;
        this.resourceCloser = resourceCloser;
    }

    /**
     * Returns the start-session use case.
     *
     * @return start-session
     */
    public StartSession startSession() {
        return startSession;
    }

    /**
     * Returns the widget-event use case.
     *
     * @return process-widget-event
     */
    public ProcessWidgetEvent processWidgetEvent() {
        return processWidgetEvent;
    }

    /**
     * Returns the active session store.
     *
     * @return session store
     */
    public SessionStore sessions() {
        return sessions;
    }

    /**
     * Returns the active download store.
     *
     * @return download store
     */
    public DownloadStore downloads() {
        return downloads;
    }

    /**
     * Returns the active component registry.
     *
     * @return component registry
     */
    public ComponentRegistry components() {
        return components;
    }

    /**
     * Returns a point-in-time snapshot of runtime metrics.
     *
     * @return metrics snapshot
     */
    public Metrics metrics() {
        return new Metrics(sessions.activeCount());
    }

    @Override
    public void close() throws Exception {
        resourceCloser.close();
    }

    /**
     * Runtime metrics snapshot.
     *
     * @param activeSessions current active session count
     */
    public record Metrics(int activeSessions) {}
}
