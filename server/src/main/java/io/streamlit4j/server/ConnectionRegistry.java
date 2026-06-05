package io.streamlit4j.server;

import io.streamlit4j.core.protocol.Envelope;
import io.streamlit4j.core.protocol.ReloadNotice;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracks active WebSocket sessions for server-wide broadcasts (e.g. reload notices).
 */
public final class ConnectionRegistry {

    private final ConcurrentMap<String, ProtocolEndpoint> endpoints = new ConcurrentHashMap<>();

    /** Creates an empty registry. */
    public ConnectionRegistry() {}

    void register(String sessionId, ProtocolEndpoint endpoint) {
        endpoints.put(sessionId, endpoint);
    }

    void remove(String sessionId) {
        endpoints.remove(sessionId);
    }

    /**
     * Returns the current count of active WebSocket sessions.
     *
     * @return active session count
     */
    public int activeConnections() {
        return endpoints.size();
    }

    /**
     * Sends a reload notice to every active session.
     *
     * @param reason diagnostic reason embedded in the notice
     */
    public void broadcastReload(String reason) {
        for (var entry : endpoints.entrySet()) {
            Envelope notice = ReloadNotice.of(entry.getKey(), reason);
            entry.getValue().deliver(notice);
        }
    }
}
