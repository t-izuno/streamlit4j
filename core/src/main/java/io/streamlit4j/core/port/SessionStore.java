package io.streamlit4j.core.port;

import io.streamlit4j.core.domain.Session;
import java.util.Optional;

/**
 * Port that owns the lifecycle of {@link Session} instances. Implementations decide the storage strategy (in-memory,
 * distributed, etc.).
 */
public interface SessionStore {

    /**
     * Creates a new session with a freshly generated identifier.
     *
     * @return the new session
     */
    Session create();

    /**
     * Looks up a session by id.
     *
     * @param id
     *            session identifier
     *
     * @return present optional when the session is still active
     */
    Optional<Session> find(String id);

    /**
     * Removes the session with the given id, if any.
     *
     * @param id
     *            session identifier
     */
    void remove(String id);

    /**
     * Returns the number of currently active sessions.
     *
     * @return active session count
     */
    int activeCount();
}
