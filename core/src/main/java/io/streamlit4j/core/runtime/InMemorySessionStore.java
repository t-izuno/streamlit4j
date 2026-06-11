package io.streamlit4j.core.runtime;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.port.SessionLifecycleListener;
import io.streamlit4j.core.port.SessionStore;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory implementation of {@link SessionStore} suitable for single-process deployments. Includes an idle-timeout
 * sweeper exposed via {@link #evictIdle()}.
 */
public final class InMemorySessionStore implements SessionStore {

    /** Default idle timeout applied when the no-arg constructor is used. */
    public static final Duration DEFAULT_IDLE_TIMEOUT = Duration.ofMinutes(30);

    private final ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final List<SessionLifecycleListener> listeners = new CopyOnWriteArrayList<>();
    private final Duration idleTimeout;

    /** Creates a store with the {@link #DEFAULT_IDLE_TIMEOUT} idle timeout. */
    public InMemorySessionStore() {
        this(DEFAULT_IDLE_TIMEOUT);
    }

    /**
     * Creates a store with a custom idle timeout.
     *
     * @param idleTimeout
     *            maximum allowed inactivity before {@link #evictIdle()} removes a session
     */
    public InMemorySessionStore(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @Override
    public Session create() {
        String id = "s-" + UUID.randomUUID().toString().substring(0, 8);
        Session session = new Session(id);
        sessions.put(id, session);
        fire(SessionLifecycleListener.Event.CREATED, session);
        return session;
    }

    @Override
    public Optional<Session> find(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    @Override
    public void remove(String id) {
        Session removed = sessions.remove(id);
        if (removed != null) {
            fire(SessionLifecycleListener.Event.DESTROYED, removed);
        }
    }

    @Override
    public int activeCount() {
        return sessions.size();
    }

    /**
     * Registers a session lifecycle listener.
     *
     * @param listener
     *            listener to notify on create / destroy / expire events
     */
    public void addListener(SessionLifecycleListener listener) {
        listeners.add(listener);
    }

    /**
     * Evicts sessions whose last activity is older than the configured idle timeout.
     *
     * @return number of sessions evicted
     */
    public int evictIdle() {
        Instant cutoff = Instant.now().minus(idleTimeout);
        int evicted = 0;
        for (Session session : sessions.values()) {
            if (session.lastActivity().isBefore(cutoff) && sessions.remove(session.id(), session)) {
                fire(SessionLifecycleListener.Event.EXPIRED, session);
                evicted++;
            }
        }
        return evicted;
    }

    private void fire(SessionLifecycleListener.Event event, Session session) {
        for (SessionLifecycleListener listener : listeners) {
            listener.on(event, session);
        }
    }
}
