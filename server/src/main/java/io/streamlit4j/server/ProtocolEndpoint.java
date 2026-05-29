package io.streamlit4j.server;

import com.fasterxml.jackson.databind.JsonNode;
import io.streamlit4j.core.protocol.Codec;
import io.streamlit4j.core.protocol.Envelope;
import io.streamlit4j.core.protocol.ErrorMessage;
import io.streamlit4j.core.protocol.Patch;
import io.streamlit4j.core.protocol.RenderDelta;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.protocol.SessionInit;
import io.streamlit4j.core.protocol.WidgetEvent;
import io.streamlit4j.core.runtime.Session;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Supplier;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public final class ProtocolEndpoint {

    private final SessionRegistry sessions;
    private final Supplier<Runnable> entrypointFactory;
    private Session domainSession;
    private org.eclipse.jetty.websocket.api.Session wsSession;

    public ProtocolEndpoint(SessionRegistry sessions, Supplier<Runnable> entrypointFactory) {
        this.sessions = sessions;
        this.entrypointFactory = entrypointFactory;
    }

    @OnWebSocketOpen
    public void onOpen(org.eclipse.jetty.websocket.api.Session ws) {
        this.wsSession = ws;
        this.domainSession = sessions.create();
        try {
            RenderNode root = domainSession.rerun(entrypointFactory.get());
            send(SessionInit.of(domainSession.id(), root));
        } catch (Exception e) {
            send(ErrorMessage.of(domainSession.id(), e.getMessage(), stackTrace(e)));
        }
    }

    @OnWebSocketMessage
    public void onMessage(String text) {
        try {
            Envelope incoming = Codec.decode(text);
            if (incoming instanceof WidgetEvent event) {
                domainSession.updateWidget(event.widgetId(), unwrap(event.value()));
                RenderNode root = domainSession.rerun(entrypointFactory.get());
                long seq = domainSession.nextSeq();
                send(RenderDelta.of(domainSession.id(), seq, List.of(Patch.replace("/", root))));
            }
        } catch (Exception e) {
            send(ErrorMessage.of(domainSession.id(), e.getMessage(), stackTrace(e)));
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (domainSession != null) {
            sessions.remove(domainSession.id());
        }
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
        return node.asText();
    }

    private static String stackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
