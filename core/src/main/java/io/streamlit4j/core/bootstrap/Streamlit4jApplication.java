package io.streamlit4j.core.bootstrap;

import io.streamlit4j.core.application.ProcessWidgetEvent;
import io.streamlit4j.core.application.StartSession;
import io.streamlit4j.core.port.DownloadStore;
import io.streamlit4j.core.port.SessionStore;

public final class Streamlit4jApplication implements AutoCloseable {

    private final StartSession startSession;
    private final ProcessWidgetEvent processWidgetEvent;
    private final SessionStore sessions;
    private final DownloadStore downloads;
    private final AutoCloseable resourceCloser;

    Streamlit4jApplication(
            StartSession startSession,
            ProcessWidgetEvent processWidgetEvent,
            SessionStore sessions,
            DownloadStore downloads,
            AutoCloseable resourceCloser) {
        this.startSession = startSession;
        this.processWidgetEvent = processWidgetEvent;
        this.sessions = sessions;
        this.downloads = downloads;
        this.resourceCloser = resourceCloser;
    }

    public StartSession startSession() {
        return startSession;
    }

    public ProcessWidgetEvent processWidgetEvent() {
        return processWidgetEvent;
    }

    public SessionStore sessions() {
        return sessions;
    }

    public DownloadStore downloads() {
        return downloads;
    }

    public Metrics metrics() {
        return new Metrics(sessions.activeCount());
    }

    @Override
    public void close() throws Exception {
        resourceCloser.close();
    }

    public record Metrics(int activeSessions) {}
}
