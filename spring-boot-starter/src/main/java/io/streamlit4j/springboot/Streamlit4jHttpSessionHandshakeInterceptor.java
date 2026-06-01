package io.streamlit4j.springboot;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * Captures the HTTP session id (if present) into the WebSocket handshake attributes so
 * downstream listeners can correlate streamlit4j sessions with their hosting HTTP session.
 *
 * <p>Works transparently with both vanilla servlet sessions and Spring Session, since both
 * surface as {@link HttpSession} through the Servlet API.
 */
public class Streamlit4jHttpSessionHandshakeInterceptor implements HandshakeInterceptor {

    public static final String HTTP_SESSION_ID_ATTRIBUTE = "streamlit4j.httpSessionId";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpSession httpSession = servletRequest.getServletRequest().getSession(false);
            if (httpSession != null) {
                attributes.put(HTTP_SESSION_ID_ATTRIBUTE, httpSession.getId());
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    public static String httpSessionId(WebSocketSession session) {
        Object value = session.getAttributes().get(HTTP_SESSION_ID_ATTRIBUTE);
        return value instanceof String id ? id : null;
    }
}
