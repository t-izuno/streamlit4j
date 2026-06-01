package io.streamlit4j.server;

import io.streamlit4j.core.protocol.Envelope;
import io.streamlit4j.core.protocol.ReloadNotice;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ConnectionRegistry {

    private final ConcurrentMap<String, ProtocolEndpoint> endpoints = new ConcurrentHashMap<>();

    void register(String sessionId, ProtocolEndpoint endpoint) {
        endpoints.put(sessionId, endpoint);
    }

    void remove(String sessionId) {
        endpoints.remove(sessionId);
    }

    public int activeConnections() {
        return endpoints.size();
    }

    public void broadcastReload(String reason) {
        for (var entry : endpoints.entrySet()) {
            Envelope notice = ReloadNotice.of(entry.getKey(), reason);
            entry.getValue().deliver(notice);
        }
    }
}
