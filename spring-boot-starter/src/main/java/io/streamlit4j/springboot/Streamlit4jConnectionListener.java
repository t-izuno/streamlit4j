package io.streamlit4j.springboot;

import org.springframework.web.socket.WebSocketSession;

/**
 * Hook for observing streamlit4j WebSocket connection lifecycle from spring-boot-starter
 * adapters (e.g. HTTP session binding). Default methods are no-ops; integrations override
 * only what they need.
 */
public interface Streamlit4jConnectionListener {

    Streamlit4jConnectionListener NO_OP = new Streamlit4jConnectionListener() {};

    default void onConnectionEstablished(WebSocketSession session, String streamlit4jSessionId) {}

    default void onConnectionClosed(WebSocketSession session, String streamlit4jSessionId) {}
}
