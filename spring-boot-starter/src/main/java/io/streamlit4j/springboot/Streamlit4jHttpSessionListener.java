package io.streamlit4j.springboot;

import io.streamlit4j.core.bootstrap.Streamlit4jApplication;
import io.streamlit4j.core.port.SessionStore;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Servlet {@link HttpSessionListener} that terminates streamlit4j internal sessions bound to a destroyed HTTP session.
 * Triggered for both servlet-native expiry and Spring Session destruction events (since Spring Session also fires this
 * listener).
 */
public class Streamlit4jHttpSessionListener implements HttpSessionListener {

    private final Streamlit4jHttpSessionRegistry registry;
    private final Streamlit4jApplication application;

    /**
     * Wires the listener with its dependencies.
     *
     * @param registry
     *            HTTP ↔ streamlit4j session registry
     * @param application
     *            streamlit4j application whose sessions will be terminated
     */
    public Streamlit4jHttpSessionListener(Streamlit4jHttpSessionRegistry registry, Streamlit4jApplication application) {
        this.registry = registry;
        this.application = application;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        String httpSessionId = event.getSession().getId();
        SessionStore sessions = application.sessions();
        for (String streamlit4jSessionId : registry.drain(httpSessionId)) {
            sessions.remove(streamlit4jSessionId);
        }
    }
}
