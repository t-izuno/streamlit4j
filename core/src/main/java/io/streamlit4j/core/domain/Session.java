package io.streamlit4j.core.domain;

import io.streamlit4j.core.protocol.RenderNode;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Mutable per-user session that holds widget state, render history, and a
 * monotonic sequence number for frame ordering.
 */
public final class Session {

    private final String id;
    private final ConcurrentMap<String, Object> state = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);
    private final ReentrantLock rerunLock = new ReentrantLock();
    private volatile Instant lastActivity = Instant.now();
    private volatile RenderNode lastRoot;

    /**
     * Creates a session with the given identifier.
     *
     * @param id session identifier
     */
    public Session(String id) {
        this.id = id;
    }

    /**
     * Returns the session identifier.
     *
     * @return session id
     */
    public String id() {
        return id;
    }

    /**
     * Returns the underlying mutable state map.
     *
     * @return state map (key → value)
     */
    public Map<String, Object> state() {
        return state;
    }

    /**
     * Returns a typed accessor wrapping this session's state.
     *
     * @return typed state wrapper
     */
    public SessionState typedState() {
        return new SessionState(state);
    }

    /**
     * Records a widget event by writing the new value into session state.
     *
     * @param widgetId widget id
     * @param value new value
     */
    public void updateWidget(String widgetId, Object value) {
        state.put(widgetId, value);
        touch();
    }

    /**
     * Returns the next frame sequence number.
     *
     * @return monotonically incremented sequence number
     */
    public long nextSeq() {
        return seq.incrementAndGet();
    }

    /**
     * Returns the timestamp of the most recent activity for idle-eviction.
     *
     * @return last activity instant
     */
    public Instant lastActivity() {
        return lastActivity;
    }

    /** Updates {@link #lastActivity()} to {@code now}. */
    public void touch() {
        lastActivity = Instant.now();
    }

    /**
     * Returns the last produced render root, if any.
     *
     * @return present optional when a render has completed
     */
    public Optional<RenderNode> lastRoot() {
        return Optional.ofNullable(lastRoot);
    }

    /**
     * Stores the most recent render root.
     *
     * @param root root node from the latest render
     */
    public void setLastRoot(RenderNode root) {
        this.lastRoot = root;
    }

    /**
     * Runs the given block while holding a re-run mutex to serialize rerenders
     * within this session.
     *
     * @param <T> return type
     * @param work block to invoke
     * @return value returned by {@code work}
     */
    public <T> T withRerunLock(Supplier<T> work) {
        rerunLock.lock();
        try {
            return work.get();
        } finally {
            rerunLock.unlock();
        }
    }
}
