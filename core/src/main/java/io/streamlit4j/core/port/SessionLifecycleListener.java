package io.streamlit4j.core.port;

import io.streamlit4j.core.domain.Session;

@FunctionalInterface
public interface SessionLifecycleListener {

    enum Event {
        CREATED,
        EXPIRED,
        DESTROYED
    }

    void on(Event event, Session session);
}
