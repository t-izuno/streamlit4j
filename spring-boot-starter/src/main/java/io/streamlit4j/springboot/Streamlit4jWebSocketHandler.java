package io.streamlit4j.springboot;

import com.fasterxml.jackson.databind.JsonNode;
import io.streamlit4j.core.application.ProcessWidgetEvent;
import io.streamlit4j.core.application.StartSession;
import io.streamlit4j.core.bootstrap.Streamlit4jApplication;
import io.streamlit4j.core.protocol.Codec;
import io.streamlit4j.core.protocol.Envelope;
import io.streamlit4j.core.protocol.ErrorMessage;
import io.streamlit4j.core.protocol.FileUpload;
import io.streamlit4j.core.protocol.RenderDelta;
import io.streamlit4j.core.protocol.SessionInit;
import io.streamlit4j.core.protocol.WidgetEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Spring WebSocket adapter that bridges {@link WebSocketSession} to streamlit4j core
 * application use cases. One instance is shared across all clients; per-connection state
 * lives in {@link WebSocketSession#getAttributes()} so the handler stays stateless.
 */
public class Streamlit4jWebSocketHandler extends TextWebSocketHandler {

    /** Attribute key used to store the streamlit4j session id on the WebSocket session. */
    public static final String SESSION_ID_ATTRIBUTE = "streamlit4j.sessionId";

    private final Streamlit4jApplication application;
    private final Streamlit4jConnectionListener connectionListener;
    private final ConcurrentMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * Creates a handler with a no-op connection listener.
     *
     * @param application streamlit4j application
     */
    public Streamlit4jWebSocketHandler(Streamlit4jApplication application) {
        this(application, Streamlit4jConnectionListener.NO_OP);
    }

    /**
     * Creates a handler with a custom connection listener.
     *
     * @param application streamlit4j application
     * @param listener connection lifecycle hook
     */
    public Streamlit4jWebSocketHandler(Streamlit4jApplication application, Streamlit4jConnectionListener listener) {
        this.application = application;
        this.connectionListener = listener;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession ws) throws Exception {
        try {
            StartSession.Result result = application.startSession().execute();
            String sessionId = result.sessionId();
            ws.getAttributes().put(SESSION_ID_ATTRIBUTE, sessionId);
            activeSessions.put(sessionId, ws);
            connectionListener.onConnectionEstablished(ws, sessionId);
            send(ws, SessionInit.of(sessionId, result.root()));
        } catch (Exception e) {
            send(ws, ErrorMessage.of(null, e.getMessage(), stackTrace(e)));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession ws, TextMessage message) throws Exception {
        String sessionId = (String) ws.getAttributes().get(SESSION_ID_ATTRIBUTE);
        try {
            Envelope incoming = Codec.decode(message.getPayload());
            ProcessWidgetEvent processor = application.processWidgetEvent();
            if (incoming instanceof WidgetEvent event) {
                ProcessWidgetEvent.Result result =
                        processor.execute(sessionId, event.widgetId(), unwrap(event.value()));
                send(ws, RenderDelta.of(sessionId, result.seq(), result.patches()));
            } else if (incoming instanceof FileUpload upload) {
                byte[] bytes = Base64.getDecoder().decode(upload.contentBase64());
                UploadedFile file = new UploadedFile(upload.filename(), upload.mimeType(), bytes);
                ProcessWidgetEvent.Result result = processor.execute(sessionId, upload.widgetId(), file);
                send(ws, RenderDelta.of(sessionId, result.seq(), result.patches()));
            }
        } catch (Exception e) {
            send(ws, ErrorMessage.of(sessionId, e.getMessage(), stackTrace(e)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession ws, CloseStatus status) {
        String sessionId = (String) ws.getAttributes().remove(SESSION_ID_ATTRIBUTE);
        if (sessionId != null) {
            activeSessions.remove(sessionId);
            connectionListener.onConnectionClosed(ws, sessionId);
        }
    }

    /**
     * Returns the count of currently active WebSocket sessions.
     *
     * @return active connection count
     */
    public int activeConnections() {
        return activeSessions.size();
    }

    private void send(WebSocketSession ws, Envelope envelope) throws java.io.IOException {
        if (ws.isOpen()) {
            ws.sendMessage(new TextMessage(Codec.encode(envelope)));
        }
    }

    private static Object unwrap(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isInt()) {
            return node.asInt();
        }
        if (node.isLong()) {
            return node.asLong();
        }
        if (node.isDouble() || node.isFloat()) {
            return node.asDouble();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isTextual()) {
            return node.asText();
        }
        return node;
    }

    private static String stackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Decoded file-upload payload delivered to the script as a widget value.
     *
     * @param filename original filename reported by the client
     * @param mimeType MIME type
     * @param bytes raw decoded bytes
     */
    public record UploadedFile(String filename, String mimeType, byte[] bytes) {}
}
