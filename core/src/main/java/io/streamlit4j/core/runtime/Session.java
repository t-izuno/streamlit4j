package io.streamlit4j.core.runtime;

import io.streamlit4j.core.protocol.RenderNode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public final class Session {

    private final String id;
    private final ConcurrentMap<String, Object> state = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);
    private final ScriptRunner runner;

    public Session(String id, ScriptRunner runner) {
        this.id = id;
        this.runner = runner;
    }

    public String id() {
        return id;
    }

    public Map<String, Object> state() {
        return state;
    }

    public long nextSeq() {
        return seq.incrementAndGet();
    }

    public RenderNode rerun(Runnable entrypoint) throws InterruptedException, ExecutionException {
        return runner.submit(new RenderContext(state), entrypoint).get();
    }

    public void updateWidget(String widgetId, Object value) {
        state.put(widgetId, value);
    }
}
