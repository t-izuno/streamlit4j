package io.streamlit4j.server;

import com.fasterxml.jackson.databind.JsonNode;
import io.streamlit4j.core.application.ProcessWidgetEvent;
import io.streamlit4j.core.application.StartSession;
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
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Jetty WebSocket endpoint that bridges client envelopes to the core {@link StartSession} / {@link ProcessWidgetEvent}
 * use cases.
 */
@WebSocket
public final class ProtocolEndpoint {

    private final StartSession startSession;
    private final ProcessWidgetEvent processWidgetEvent;
    private final ConnectionRegistry connections;
    private String sessionId;
    private org.eclipse.jetty.websocket.api.Session wsSession;

    /**
     * Wires the endpoint with its application dependencies.
     *
     * @param startSession
     *            start-session use case
     * @param processWidgetEvent
     *            widget-event use case
     * @param connections
     *            shared connection registry
     */
    public ProtocolEndpoint(StartSession startSession, ProcessWidgetEvent processWidgetEvent,
            ConnectionRegistry connections) {
        this.startSession = startSession;
        this.processWidgetEvent = processWidgetEvent;
        this.connections = connections;
    }

    /**
     * Jetty callback: WebSocket opened. Starts a fresh session and sends the initial render.
     *
     * @param ws
     *            Jetty session handle
     */
    @OnWebSocketOpen
    public void onOpen(org.eclipse.jetty.websocket.api.Session ws) {
        this.wsSession = ws;
        try {
            StartSession.Result result = startSession.execute();
            this.sessionId = result.sessionId();
            connections.register(sessionId, this);
            send(SessionInit.of(result.sessionId(), result.root()));
        } catch (Exception e) {
            send(ErrorMessage.of(sessionId, e.getMessage(), stackTrace(e)));
        }
    }

    /**
     * Jetty callback: text frame received.
     *
     * @param text
     *            JSON-encoded envelope from the client
     */
    @OnWebSocketMessage
    public void onMessage(String text) {
        try {
            Envelope incoming = Codec.decode(text);
            if (incoming instanceof WidgetEvent event) {
                ProcessWidgetEvent.Result result = processWidgetEvent.execute(sessionId, event.widgetId(),
                        unwrap(event.value()));
                send(RenderDelta.of(sessionId, result.seq(), result.patches()));
            } else if (incoming instanceof FileUpload upload) {
                byte[] bytes = Base64.getDecoder().decode(upload.contentBase64());
                UploadedFile file = new UploadedFile(upload.filename(), upload.mimeType(), bytes);
                ProcessWidgetEvent.Result result = processWidgetEvent.execute(sessionId, upload.widgetId(), file);
                send(RenderDelta.of(sessionId, result.seq(), result.patches()));
            }
        } catch (Exception e) {
            send(ErrorMessage.of(sessionId, e.getMessage(), stackTrace(e)));
        }
    }

    /**
     * Jetty callback: WebSocket closed. Removes this endpoint from the registry.
     *
     * @param statusCode
     *            close status code
     * @param reason
     *            close reason
     */
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (sessionId != null) {
            connections.remove(sessionId);
        }
    }

    void deliver(Envelope envelope) {
        send(envelope);
    }

    private void send(Envelope envelope) {
        if (wsSession != null && wsSession.isOpen()) {
            wsSession.sendText(Codec.encode(envelope), Callback.NOOP);
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
     * @param filename
     *            original filename reported by the client
     * @param mimeType
     *            MIME type
     * @param bytes
     *            raw decoded bytes
     */
    public record UploadedFile(String filename, String mimeType, byte[] bytes) {
    }
}
