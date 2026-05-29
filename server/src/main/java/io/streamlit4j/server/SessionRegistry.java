package io.streamlit4j.server;

import io.streamlit4j.core.runtime.ScriptRunner;
import io.streamlit4j.core.runtime.Session;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionRegistry implements AutoCloseable {

    private final ScriptRunner runner = new ScriptRunner();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public Session create() {
        String id = "s-" + UUID.randomUUID().toString().substring(0, 8);
        Session session = new Session(id, runner);
        sessions.put(id, session);
        return session;
    }

    public void remove(String id) {
        sessions.remove(id);
    }

    public int activeCount() {
        return sessions.size();
    }

    @Override
    public void close() {
        sessions.clear();
        runner.close();
    }
}
