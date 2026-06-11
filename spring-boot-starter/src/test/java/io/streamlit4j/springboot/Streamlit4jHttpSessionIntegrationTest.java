package io.streamlit4j.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.bootstrap.Bootstrap;
import io.streamlit4j.core.bootstrap.Streamlit4jApplication;
import io.streamlit4j.core.domain.Session;
import jakarta.servlet.http.HttpSessionEvent;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

class Streamlit4jHttpSessionIntegrationTest {

    @Test
    void handshakeInterceptorCapturesHttpSessionId() {
        Streamlit4jHttpSessionHandshakeInterceptor interceptor = new Streamlit4jHttpSessionHandshakeInterceptor();
        MockHttpServletRequest mock = new MockHttpServletRequest("GET", "/ws");
        MockHttpSession httpSession = new MockHttpSession();
        mock.setSession(httpSession);

        Map<String, Object> attributes = new HashMap<>();
        boolean proceed = interceptor.beforeHandshake(new ServletServerHttpRequest(mock),
                new org.springframework.http.server.ServletServerHttpResponse(new MockHttpServletResponse()), null,
                attributes);

        assertThat(proceed).isTrue();
        assertThat(attributes).containsEntry(Streamlit4jHttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTRIBUTE,
                httpSession.getId());
    }

    @Test
    void handshakeInterceptorSkipsWhenRequestIsNotServletServerRequest() {
        Streamlit4jHttpSessionHandshakeInterceptor interceptor = new Streamlit4jHttpSessionHandshakeInterceptor();
        org.springframework.http.server.ServerHttpRequest req = org.mockito.Mockito
                .mock(org.springframework.http.server.ServerHttpRequest.class);
        Map<String, Object> attributes = new HashMap<>();
        boolean proceed = interceptor.beforeHandshake(req,
                new org.springframework.http.server.ServletServerHttpResponse(new MockHttpServletResponse()), null,
                attributes);
        assertThat(proceed).isTrue();
        assertThat(attributes).doesNotContainKey(Streamlit4jHttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTRIBUTE);
    }

    @Test
    void afterHandshakeIsNoOp() {
        Streamlit4jHttpSessionHandshakeInterceptor interceptor = new Streamlit4jHttpSessionHandshakeInterceptor();
        MockHttpServletRequest mock = new MockHttpServletRequest("GET", "/ws");
        interceptor.afterHandshake(new ServletServerHttpRequest(mock),
                new org.springframework.http.server.ServletServerHttpResponse(new MockHttpServletResponse()), null,
                null);
    }

    @Test
    void httpSessionIdReturnsNullWhenAttributeIsNotString() {
        WebSocketSessionWithAttributes session = new WebSocketSessionWithAttributes();
        session.attributes.put(Streamlit4jHttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTRIBUTE, 12345);
        assertThat(Streamlit4jHttpSessionHandshakeInterceptor.httpSessionId(session)).isNull();
    }

    @Test
    void unbindRemovesLastEntryAndClearsHttpKey() {
        Streamlit4jHttpSessionRegistry registry = new Streamlit4jHttpSessionRegistry();
        registry.bind("http-1", "stream-a");
        registry.unbind("http-1", "stream-a");
        assertThat(registry.activeHttpSessions()).isZero();
    }

    @Test
    void unbindRemovesOneOfManyAndKeepsHttpKey() {
        Streamlit4jHttpSessionRegistry registry = new Streamlit4jHttpSessionRegistry();
        registry.bind("http-1", "stream-a");
        registry.bind("http-1", "stream-b");
        registry.unbind("http-1", "stream-a");
        assertThat(registry.activeHttpSessions()).isEqualTo(1);
    }

    @Test
    void handshakeInterceptorSkipsWhenNoHttpSession() {
        Streamlit4jHttpSessionHandshakeInterceptor interceptor = new Streamlit4jHttpSessionHandshakeInterceptor();
        MockHttpServletRequest mock = new MockHttpServletRequest("GET", "/ws");

        Map<String, Object> attributes = new HashMap<>();
        interceptor.beforeHandshake(new ServletServerHttpRequest(mock),
                new org.springframework.http.server.ServletServerHttpResponse(new MockHttpServletResponse()), null,
                attributes);

        assertThat(attributes).doesNotContainKey(Streamlit4jHttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTRIBUTE);
    }

    @Test
    void httpSessionListenerTerminatesBoundStreamlit4jSessions() {
        Streamlit4jApplication application = Bootstrap.standalone(() -> () -> {
        });
        try {
            Streamlit4jHttpSessionRegistry registry = new Streamlit4jHttpSessionRegistry();
            Streamlit4jHttpSessionListener listener = new Streamlit4jHttpSessionListener(registry, application);

            Session streamlit4jSession = application.sessions().create();
            String httpSessionId = "http-1";
            registry.bind(httpSessionId, streamlit4jSession.id());

            assertThat(application.sessions().find(streamlit4jSession.id())).isPresent();

            MockHttpSession httpSession = new MockHttpSession(null, httpSessionId);
            listener.sessionDestroyed(new HttpSessionEvent(httpSession));

            assertThat(application.sessions().find(streamlit4jSession.id())).isEmpty();
            assertThat(registry.activeHttpSessions()).isZero();
        } finally {
            try {
                application.close();
            } catch (Exception ignored) {
                // best-effort close
            }
        }
    }

    @Test
    void registryHandlesUnbindForUnknownIds() {
        Streamlit4jHttpSessionRegistry registry = new Streamlit4jHttpSessionRegistry();
        registry.unbind("nonexistent", "also-nonexistent");
        assertThat(registry.activeHttpSessions()).isZero();
    }

    @Test
    void registryDrainsAllBindingsForHttpSession() {
        Streamlit4jHttpSessionRegistry registry = new Streamlit4jHttpSessionRegistry();
        registry.bind("http-1", "s-a");
        registry.bind("http-1", "s-b");
        registry.bind("http-2", "s-c");

        assertThat(registry.drain("http-1")).containsExactlyInAnyOrder("s-a", "s-b");
        assertThat(registry.drain("http-1")).isEmpty();
        assertThat(registry.activeHttpSessions()).isEqualTo(1);
    }

    @Test
    void binderRegistersBindingFromHandshakeAttributes() {
        Streamlit4jHttpSessionRegistry registry = new Streamlit4jHttpSessionRegistry();
        Streamlit4jHttpSessionBinder binder = new Streamlit4jHttpSessionBinder(registry);

        WebSocketSessionWithAttributes session = new WebSocketSessionWithAttributes();
        session.attributes.put(Streamlit4jHttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTRIBUTE, "http-1");

        binder.onConnectionEstablished(session, "stream-1");
        assertThat(registry.activeHttpSessions()).isEqualTo(1);

        binder.onConnectionClosed(session, "stream-1");
        assertThat(registry.activeHttpSessions()).isZero();
    }

    @Test
    void binderIsNoOpWhenHttpSessionMissing() {
        Streamlit4jHttpSessionRegistry registry = new Streamlit4jHttpSessionRegistry();
        Streamlit4jHttpSessionBinder binder = new Streamlit4jHttpSessionBinder(registry);

        WebSocketSessionWithAttributes session = new WebSocketSessionWithAttributes();
        binder.onConnectionEstablished(session, "stream-1");

        assertThat(registry.activeHttpSessions()).isZero();
    }
}
