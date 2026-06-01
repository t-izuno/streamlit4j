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

public final class Session {

    private final String id;
    private final ConcurrentMap<String, Object> state = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);
    private final ReentrantLock rerunLock = new ReentrantLock();
    private volatile Instant lastActivity = Instant.now();
    private volatile RenderNode lastRoot;

    public Session(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public Map<String, Object> state() {
        return state;
    }

    public SessionState typedState() {
        return new SessionState(state);
    }

    public void updateWidget(String widgetId, Object value) {
        state.put(widgetId, value);
        touch();
    }

    public long nextSeq() {
        return seq.incrementAndGet();
    }

    public Instant lastActivity() {
        return lastActivity;
    }

    public void touch() {
        lastActivity = Instant.now();
    }

    public Optional<RenderNode> lastRoot() {
        return Optional.ofNullable(lastRoot);
    }

    public void setLastRoot(RenderNode root) {
        this.lastRoot = root;
    }

    public <T> T withRerunLock(Supplier<T> work) {
        rerunLock.lock();
        try {
            return work.get();
        } finally {
            rerunLock.unlock();
        }
    }
}
