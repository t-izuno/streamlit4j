package io.streamlit4j.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketSession;

class Streamlit4jPrincipalHandshakeInterceptorTest {

    private final Streamlit4jPrincipalHandshakeInterceptor interceptor = new Streamlit4jPrincipalHandshakeInterceptor();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void copiesAuthenticationIntoAttributes() {
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Map<String, Object> attributes = new HashMap<>();
        boolean proceed = interceptor.beforeHandshake(stubRequest(), stubResponse(), null, attributes);

        assertThat(proceed).isTrue();
        assertThat(attributes).containsEntry(Streamlit4jPrincipalHandshakeInterceptor.AUTHENTICATION_ATTRIBUTE, auth);
    }

    @Test
    void skipsAttributeWhenUnauthenticated() {
        Map<String, Object> attributes = new HashMap<>();
        boolean proceed = interceptor.beforeHandshake(stubRequest(), stubResponse(), null, attributes);

        assertThat(proceed).isTrue();
        assertThat(attributes).doesNotContainKey(Streamlit4jPrincipalHandshakeInterceptor.AUTHENTICATION_ATTRIBUTE);
    }

    @Test
    void skipsAttributeWhenAuthenticationPresentButNotAuthenticated() {
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "n/a");
        auth.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Map<String, Object> attributes = new HashMap<>();
        interceptor.beforeHandshake(stubRequest(), stubResponse(), null, attributes);

        assertThat(attributes).doesNotContainKey(Streamlit4jPrincipalHandshakeInterceptor.AUTHENTICATION_ATTRIBUTE);
    }

    @Test
    void afterHandshakeIsNoOp() {
        interceptor.afterHandshake(stubRequest(), stubResponse(), null, null);
    }

    @Test
    void currentAuthenticationReturnsNullWhenAttributeIsNotAuthentication() {
        WebSocketSession session = stubSession(
                Map.of(Streamlit4jPrincipalHandshakeInterceptor.AUTHENTICATION_ATTRIBUTE, "not-an-auth-object"));
        assertThat(Streamlit4jPrincipalHandshakeInterceptor.currentAuthentication(session)).isNull();
    }

    @Test
    void currentAuthenticationReadsFromWebSocketSession() {
        Authentication auth = new UsernamePasswordAuthenticationToken("bob", "n/a", List.of());
        WebSocketSession session = stubSession(
                Map.of(Streamlit4jPrincipalHandshakeInterceptor.AUTHENTICATION_ATTRIBUTE, auth));

        assertThat(Streamlit4jPrincipalHandshakeInterceptor.currentAuthentication(session)).isSameAs(auth);
    }

    @Test
    void currentAuthenticationReturnsNullWhenMissing() {
        WebSocketSession session = stubSession(Map.of());
        assertThat(Streamlit4jPrincipalHandshakeInterceptor.currentAuthentication(session)).isNull();
    }

    private static ServerHttpRequest stubRequest() {
        MockHttpServletRequest mock = new MockHttpServletRequest("GET", "/ws");
        mock.setScheme("http");
        mock.setServerName("localhost");
        return new ServletServerHttpRequest(mock);
    }

    private static ServerHttpResponse stubResponse() {
        return new ServletServerHttpResponse(new MockHttpServletResponse());
    }

    private static WebSocketSession stubSession(Map<String, Object> attributes) {
        WebSocketSessionWithAttributes session = new WebSocketSessionWithAttributes();
        session.attributes.putAll(attributes);
        return session;
    }
}
