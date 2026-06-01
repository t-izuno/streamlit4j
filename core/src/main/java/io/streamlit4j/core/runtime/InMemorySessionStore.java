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

public final class InMemorySessionStore implements SessionStore {

    public static final Duration DEFAULT_IDLE_TIMEOUT = Duration.ofMinutes(30);

    private final ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final List<SessionLifecycleListener> listeners = new CopyOnWriteArrayList<>();
    private final Duration idleTimeout;

    public InMemorySessionStore() {
        this(DEFAULT_IDLE_TIMEOUT);
    }

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

    public void addListener(SessionLifecycleListener listener) {
        listeners.add(listener);
    }

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
