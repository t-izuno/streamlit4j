package io.streamlit4j.springboot;

import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * Copies the current {@link Authentication} from {@link SecurityContextHolder} into
 * the WebSocket handshake attributes, making it available via
 * {@link #currentAuthentication(WebSocketSession)} during message handling.
 *
 * <p>This adapter does not enforce authentication; callers must configure a
 * {@code SecurityFilterChain} that matches the streamlit4j base path. When no
 * {@link Authentication} is present (anonymous or unauthenticated request), the
 * attribute is simply not set and the handshake proceeds.
 */
public class Streamlit4jPrincipalHandshakeInterceptor implements HandshakeInterceptor {

    static final String AUTHENTICATION_ATTRIBUTE = "streamlit4j.authentication";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            attributes.put(AUTHENTICATION_ATTRIBUTE, authentication);
        }
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    public static Authentication currentAuthentication(WebSocketSession session) {
        Object value = session.getAttributes().get(AUTHENTICATION_ATTRIBUTE);
        return value instanceof Authentication auth ? auth : null;
    }
}
