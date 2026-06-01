package io.streamlit4j.core.bootstrap;

import io.streamlit4j.core.application.ProcessWidgetEvent;
import io.streamlit4j.core.application.StartSession;
import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.core.port.SessionLifecycleListener;
import io.streamlit4j.core.runtime.DownloadAccess;
import io.streamlit4j.core.runtime.InMemoryDownloadStore;
import io.streamlit4j.core.runtime.InMemorySessionStore;
import io.streamlit4j.core.runtime.ScriptRunner;
import io.streamlit4j.core.runtime.StructuredLog;

public final class Bootstrap {

    private Bootstrap() {}

    public static Streamlit4jApplication standalone(EntrypointSource entrypoints) {
        ScriptRunner renderer = new ScriptRunner();
        InMemorySessionStore store = new InMemorySessionStore();
        InMemoryDownloadStore downloads = new InMemoryDownloadStore();
        DownloadAccess.use(downloads);
        store.addListener((event, session) -> {
            switch (event) {
                case CREATED -> StructuredLog.sessionCreated(session.id());
                case DESTROYED -> StructuredLog.sessionDestroyed(session.id());
                case EXPIRED -> StructuredLog.sessionExpired(session.id());
            }
        });
        StartSession start = new StartSession(store, entrypoints, renderer);
        ProcessWidgetEvent process = new ProcessWidgetEvent(store, entrypoints, renderer);
        return new Streamlit4jApplication(start, process, store, downloads, renderer);
    }

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
