package io.streamlit4j.core.port;

import io.streamlit4j.core.domain.Session;

/**
 * Observer notified when a {@link Session} transitions through its lifecycle.
 */
@FunctionalInterface
public interface SessionLifecycleListener {

    /** Lifecycle transition kind. */
    enum Event {
        /** Session was just created. */
        CREATED,
        /** Session was evicted due to idle timeout. */
        EXPIRED,
        /** Session was explicitly destroyed. */
        DESTROYED
    }

    /**
     * Handles a lifecycle event.
     *
     * @param event transition kind
     * @param session affected session
     */
    void on(Event event, Session session);
}
