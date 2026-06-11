package io.streamlit4j.springboot;

import org.springframework.web.socket.WebSocketSession;

/**
 * Hook for observing streamlit4j WebSocket connection lifecycle from spring-boot-starter adapters (e.g. HTTP session
 * binding). Default methods are no-ops; integrations override only what they need.
 */
public interface Streamlit4jConnectionListener {

    /** No-op listener used when no integration is configured. */
    Streamlit4jConnectionListener NO_OP = new Streamlit4jConnectionListener() {
    };

    /**
     * Invoked once a streamlit4j session has been bound to the WebSocket.
     *
     * @param session
     *            Spring WebSocket session
     * @param streamlit4jSessionId
     *            streamlit4j-side session id
     */
    default void onConnectionEstablished(WebSocketSession session, String streamlit4jSessionId) {
    }

    /**
     * Invoked when the WebSocket closes and the streamlit4j session is being released.
     *
     * @param session
     *            Spring WebSocket session
     * @param streamlit4jSessionId
     *            streamlit4j-side session id
     */
    default void onConnectionClosed(WebSocketSession session, String streamlit4jSessionId) {
    }
}
