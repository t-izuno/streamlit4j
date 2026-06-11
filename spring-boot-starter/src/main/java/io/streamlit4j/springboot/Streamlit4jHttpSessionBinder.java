package io.streamlit4j.springboot;

import org.springframework.web.socket.WebSocketSession;

/**
 * Connection listener that records the HTTP session ↔ streamlit4j session binding in the
 * {@link Streamlit4jHttpSessionRegistry} so HTTP session destruction can cascade.
 */
public class Streamlit4jHttpSessionBinder implements Streamlit4jConnectionListener {

    private final Streamlit4jHttpSessionRegistry registry;

    /**
     * Creates a binder writing into the given registry.
     *
     * @param registry
     *            HTTP ↔ streamlit4j session registry
     */
    public Streamlit4jHttpSessionBinder(Streamlit4jHttpSessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onConnectionEstablished(WebSocketSession session, String streamlit4jSessionId) {
        String httpSessionId = Streamlit4jHttpSessionHandshakeInterceptor.httpSessionId(session);
        if (httpSessionId != null) {
            registry.bind(httpSessionId, streamlit4jSessionId);
        }
    }

    @Override
    public void onConnectionClosed(WebSocketSession session, String streamlit4jSessionId) {
        String httpSessionId = Streamlit4jHttpSessionHandshakeInterceptor.httpSessionId(session);
        if (httpSessionId != null) {
            registry.unbind(httpSessionId, streamlit4jSessionId);
        }
    }
}
