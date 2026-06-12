package io.streamlit4j.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.streamlit4j.core.bootstrap.Bootstrap;
import io.streamlit4j.core.bootstrap.Streamlit4jApplication;
import io.streamlit4j.core.protocol.Codec;
import io.streamlit4j.core.protocol.FileUpload;
import io.streamlit4j.core.protocol.WidgetEvent;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

class Streamlit4jWebSocketHandlerTest {

    private Streamlit4jApplication app;

    @BeforeEach
    void setUp() {
        app = Bootstrap.standalone(() -> () -> {
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        app.close();
    }

    @Test
    void establishedRegistersSessionAndDispatchesListener() throws Exception {
        AtomicReference<String> established = new AtomicReference<>();
        Streamlit4jConnectionListener listener = new Streamlit4jConnectionListener() {
            @Override
            public void onConnectionEstablished(WebSocketSession session, String streamlit4jSessionId) {
                established.set(streamlit4jSessionId);
            }
        };
        Streamlit4jWebSocketHandler handler = new Streamlit4jWebSocketHandler(app, listener);
        CapturingWebSocketSession ws = new CapturingWebSocketSession();

        handler.afterConnectionEstablished(ws);

        assertThat(handler.activeConnections()).isEqualTo(1);
        assertThat(ws.getAttributes()).containsKey(Streamlit4jWebSocketHandler.SESSION_ID_ATTRIBUTE);
        assertThat(established.get()).isNotNull();
        assertThat(ws.sent).hasSize(1); // SessionInit
    }

    @Test
    void singleArgConstructorUsesNoOpListener() throws Exception {
        Streamlit4jWebSocketHandler handler = new Streamlit4jWebSocketHandler(app);
        CapturingWebSocketSession ws = new CapturingWebSocketSession();
        handler.afterConnectionEstablished(ws);
        assertThat(handler.activeConnections()).isEqualTo(1);
    }

    @Test
    void closedRemovesSessionAndDispatchesListener() throws Exception {
        AtomicReference<String> closed = new AtomicReference<>();
        Streamlit4jConnectionListener listener = new Streamlit4jConnectionListener() {
            @Override
            public void onConnectionClosed(WebSocketSession session, String streamlit4jSessionId) {
                closed.set(streamlit4jSessionId);
            }
        };
        Streamlit4jWebSocketHandler handler = new Streamlit4jWebSocketHandler(app, listener);
        CapturingWebSocketSession ws = new CapturingWebSocketSession();
        handler.afterConnectionEstablished(ws);

        handler.afterConnectionClosed(ws, CloseStatus.NORMAL);

        assertThat(handler.activeConnections()).isZero();
        assertThat(closed.get()).isNotNull();
    }

    @Test
    void closedIsNoOpWhenSessionNeverEstablished() {
        Streamlit4jWebSocketHandler handler = new Streamlit4jWebSocketHandler(app);
        CapturingWebSocketSession ws = new CapturingWebSocketSession();
        handler.afterConnectionClosed(ws, CloseStatus.NORMAL);
        assertThat(handler.activeConnections()).isZero();
    }

    @Test
    void handleTextWidgetEventTriggersDelta() throws Exception {
        Streamlit4jWebSocketHandler handler = new Streamlit4jWebSocketHandler(app);
        CapturingWebSocketSession ws = new CapturingWebSocketSession();
        handler.afterConnectionEstablished(ws);
        String sessionId = (String) ws.getAttributes().get(Streamlit4jWebSocketHandler.SESSION_ID_ATTRIBUTE);
        ws.sent.clear();

        String payload = Codec.encode(WidgetEvent.of(sessionId, "w-1", IntNode.valueOf(7)));
        handler.handleMessage(ws, new TextMessage(payload));

        assertThat(ws.sent).hasSize(1);
        assertThat(ws.sent.get(0)).contains("\"type\":\"render_delta\"");
    }

    @Test
    void handleTextFileUploadDecodesBase64() throws Exception {
        Streamlit4jWebSocketHandler handler = new Streamlit4jWebSocketHandler(app);
        CapturingWebSocketSession ws = new CapturingWebSocketSession();
        handler.afterConnectionEstablished(ws);
        String sessionId = (String) ws.getAttributes().get(Streamlit4jWebSocketHandler.SESSION_ID_ATTRIBUTE);
        ws.sent.clear();

        String b64 = Base64.getEncoder().encodeToString(new byte[] { 1, 2, 3 });
        String payload = Codec.encode(FileUpload.of(sessionId, "w-up", "a.bin", "application/octet-stream", b64));
        handler.handleMessage(ws, new TextMessage(payload));

        assertThat(ws.sent).hasSize(1);
        assertThat(ws.sent.get(0)).contains("\"type\":\"render_delta\"");
    }

    @Test
    void handleTextBadPayloadProducesErrorEnvelope() throws Exception {
        Streamlit4jWebSocketHandler handler = new Streamlit4jWebSocketHandler(app);
        CapturingWebSocketSession ws = new CapturingWebSocketSession();
        handler.afterConnectionEstablished(ws);
        ws.sent.clear();

        handler.handleMessage(ws, new TextMessage("{\"type\":\"unknown\"}"));

        assertThat(ws.sent).hasSize(1);
        assertThat(ws.sent.get(0)).contains("\"type\":\"error\"");
    }

    @Test
    void handleTextSessionInitTypeIsIgnored() throws Exception {
        // SessionInit is server→client; client sending it lands in neither branch
        // → no envelope is sent back.
        Streamlit4jWebSocketHandler handler = new Streamlit4jWebSocketHandler(app);
        CapturingWebSocketSession ws = new CapturingWebSocketSession();
        handler.afterConnectionEstablished(ws);
        ws.sent.clear();

        handler.handleMessage(ws, new TextMessage("{\"v\":1,\"type\":\"session_init\",\"sessionId\":\"s\","
                + "\"root\":{\"kind\":\"root\",\"id\":\"root\"}}"));

        assertThat(ws.sent).isEmpty();
    }

    @Test
    void handleTextAllJsonNodeKindsForUnwrap() throws Exception {
        Streamlit4jWebSocketHandler handler = new Streamlit4jWebSocketHandler(app);
        CapturingWebSocketSession ws = new CapturingWebSocketSession();
        handler.afterConnectionEstablished(ws);
        String sessionId = (String) ws.getAttributes().get(Streamlit4jWebSocketHandler.SESSION_ID_ATTRIBUTE);

        JsonNode[] nodes = { IntNode.valueOf(1), LongNode.valueOf(2L), DoubleNode.valueOf(3.5), BooleanNode.TRUE,
                TextNode.valueOf("x"), NullNode.getInstance(), Codec.valueToTree(Map.of("k", "v")) };
        for (JsonNode node : nodes) {
            String payload = Codec.encode(WidgetEvent.of(sessionId, "w-x", node));
            handler.handleMessage(ws, new TextMessage(payload));
        }
        // Each successful dispatch adds a render_delta.
        assertThat(ws.sent).hasSizeGreaterThanOrEqualTo(nodes.length);
    }

    @Test
    void sendIsNoOpWhenSessionClosed() throws Exception {
        Streamlit4jWebSocketHandler handler = new Streamlit4jWebSocketHandler(app);
        CapturingWebSocketSession ws = new CapturingWebSocketSession();
        ws.close(); // mark as not open
        handler.afterConnectionEstablished(ws); // should still register but send becomes no-op.
        assertThat(ws.sent).isEmpty();
    }

    @Test
    void uploadedFileRecordRoundTrips() {
        byte[] bytes = new byte[] { 7, 8 };
        Streamlit4jWebSocketHandler.UploadedFile f = new Streamlit4jWebSocketHandler.UploadedFile("a.bin",
                "application/octet-stream", bytes);
        assertThat(f.filename()).isEqualTo("a.bin");
        assertThat(f.mimeType()).isEqualTo("application/octet-stream");
        assertThat(f.bytes()).isSameAs(bytes);
    }

    static final class CapturingWebSocketSession implements WebSocketSession {
        final java.util.Map<String, Object> attributes = new java.util.HashMap<>();
        final List<String> sent = new java.util.ArrayList<>();
        private boolean open = true;

        @Override
        public java.util.Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public String getId() {
            return "ws-cap";
        }

        @Override
        public java.net.URI getUri() {
            return java.net.URI.create("ws://localhost/ws");
        }

        @Override
        public org.springframework.http.HttpHeaders getHandshakeHeaders() {
            return new org.springframework.http.HttpHeaders();
        }

        @Override
        public java.security.Principal getPrincipal() {
            return null;
        }

        @Override
        public java.net.InetSocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public java.net.InetSocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public String getAcceptedProtocol() {
            return null;
        }

        @Override
        public void setTextMessageSizeLimit(int messageSizeLimit) {
        }

        @Override
        public int getTextMessageSizeLimit() {
            return 0;
        }

        @Override
        public void setBinaryMessageSizeLimit(int messageSizeLimit) {
        }

        @Override
        public int getBinaryMessageSizeLimit() {
            return 0;
        }

        @Override
        public java.util.List<org.springframework.web.socket.WebSocketExtension> getExtensions() {
            return java.util.List.of();
        }

        @Override
        public void sendMessage(WebSocketMessage<?> message) {
            if (message instanceof TextMessage tm) {
                sent.add(tm.getPayload());
            }
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() {
            open = false;
        }

        @Override
        public void close(org.springframework.web.socket.CloseStatus status) {
            open = false;
        }
    }
}
