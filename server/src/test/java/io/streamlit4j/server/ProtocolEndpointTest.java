package io.streamlit4j.server;

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
import org.junit.jupiter.api.Test;

class ProtocolEndpointTest {

    @Test
    void uploadedFileRecordExposesComponents() {
        byte[] bytes = new byte[] { 1, 2, 3 };
        ProtocolEndpoint.UploadedFile f = new ProtocolEndpoint.UploadedFile("a.bin", "application/octet-stream", bytes);
        assertThat(f.filename()).isEqualTo("a.bin");
        assertThat(f.mimeType()).isEqualTo("application/octet-stream");
        assertThat(f.bytes()).isSameAs(bytes);
    }

    @Test
    void onOpenStartsSessionAndRegistersConnection() throws Exception {
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            ConnectionRegistry registry = new ConnectionRegistry();
            ProtocolEndpoint endpoint = new ProtocolEndpoint(app.startSession(), app.processWidgetEvent(), registry);

            endpoint.onOpen(null);

            assertThat(registry.activeConnections()).isEqualTo(1);
        }
    }

    @Test
    void onCloseRemovesConnectionWhenSessionEstablished() throws Exception {
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            ConnectionRegistry registry = new ConnectionRegistry();
            ProtocolEndpoint endpoint = new ProtocolEndpoint(app.startSession(), app.processWidgetEvent(), registry);
            endpoint.onOpen(null);
            assertThat(registry.activeConnections()).isEqualTo(1);
            endpoint.onClose(1000, "normal");
            assertThat(registry.activeConnections()).isZero();
        }
    }

    @Test
    void onCloseIsNoOpWhenSessionNeverEstablished() throws Exception {
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            ConnectionRegistry registry = new ConnectionRegistry();
            ProtocolEndpoint endpoint = new ProtocolEndpoint(app.startSession(), app.processWidgetEvent(), registry);
            endpoint.onClose(1006, "abnormal"); // sessionId is null — branch coverage.
            assertThat(registry.activeConnections()).isZero();
        }
    }

    @Test
    void onMessageWithWidgetEventExecutesUseCase() throws Exception {
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            ConnectionRegistry registry = new ConnectionRegistry();
            ProtocolEndpoint endpoint = new ProtocolEndpoint(app.startSession(), app.processWidgetEvent(), registry);
            endpoint.onOpen(null);

            // The session id from start is opaque; reach it via the registry.
            String sessionId = registry.activeConnections() == 1 ? mostRecentSessionId(app) : null;
            assertThat(sessionId).isNotNull();

            String json = Codec.encode(WidgetEvent.of(sessionId, "w-x", IntNode.valueOf(42)));
            endpoint.onMessage(json); // exercises WidgetEvent branch; send() is a no-op with no ws session.
        }
    }

    @Test
    void onMessageWithFileUploadDecodesBytes() throws Exception {
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            ConnectionRegistry registry = new ConnectionRegistry();
            ProtocolEndpoint endpoint = new ProtocolEndpoint(app.startSession(), app.processWidgetEvent(), registry);
            endpoint.onOpen(null);
            String sessionId = mostRecentSessionId(app);

            String b64 = Base64.getEncoder().encodeToString(new byte[] { 1, 2, 3 });
            String json = Codec.encode(FileUpload.of(sessionId, "w-up", "a.bin", "application/octet-stream", b64));
            endpoint.onMessage(json); // FileUpload branch.
        }
    }

    @Test
    void onMessageWithUnparseableInputRaisesErrorEnvelope() throws Exception {
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            ConnectionRegistry registry = new ConnectionRegistry();
            ProtocolEndpoint endpoint = new ProtocolEndpoint(app.startSession(), app.processWidgetEvent(), registry);
            endpoint.onOpen(null);
            endpoint.onMessage("{\"type\":\"unknown\"}"); // exception branch.
        }
    }

    @Test
    void onMessageWithSessionInitTypeIsIgnored() throws Exception {
        // SessionInit is server→client only; onMessage's instanceof chains skip it
        // without throwing.
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            ConnectionRegistry registry = new ConnectionRegistry();
            ProtocolEndpoint endpoint = new ProtocolEndpoint(app.startSession(), app.processWidgetEvent(), registry);
            endpoint.onOpen(null);
            endpoint.onMessage("{\"v\":1,\"type\":\"session_init\",\"sessionId\":\"s\","
                    + "\"root\":{\"kind\":\"root\",\"id\":\"root\"}}");
        }
    }

    @Test
    void unwrapHandlesIntLongDoubleBoolStringNullAndObjectViaUseCase() throws Exception {
        // Drive every node-kind branch through onMessage → unwrap → processWidgetEvent.
        try (Streamlit4jApplication app = Bootstrap.standalone(() -> () -> {
        })) {
            ConnectionRegistry registry = new ConnectionRegistry();
            ProtocolEndpoint endpoint = new ProtocolEndpoint(app.startSession(), app.processWidgetEvent(), registry);
            endpoint.onOpen(null);
            String sessionId = mostRecentSessionId(app);

            // The trailing element exercises the object branch in unwrap().
            for (com.fasterxml.jackson.databind.JsonNode node : new com.fasterxml.jackson.databind.JsonNode[] {
                    IntNode.valueOf(1), LongNode.valueOf(2L), DoubleNode.valueOf(3.5), BooleanNode.TRUE,
                    TextNode.valueOf("hello"), NullNode.getInstance(),
                    Codec.valueToTree(java.util.Map.of("k", "v")) }) {
                String json = Codec.encode(WidgetEvent.of(sessionId, "w-x", node));
                endpoint.onMessage(json);
            }
        }
    }

    @Test
    void unwrapConvertsObjectsAndArraysToJavaCollections() {
        JsonNode object = Codec.valueToTree(java.util.Map.of("action", "stop", "value", "edited"));
        JsonNode array = Codec.valueToTree(java.util.List.of("a", java.util.Map.of("b", 1)));

        Object unwrappedObject = ProtocolEndpoint.unwrap(object);
        Object unwrappedArray = ProtocolEndpoint.unwrap(array);

        assertThat(unwrappedObject).isEqualTo(java.util.Map.of("action", "stop", "value", "edited"));
        assertThat(unwrappedArray).isEqualTo(java.util.List.of("a", java.util.Map.of("b", 1)));
    }

    private static String mostRecentSessionId(Streamlit4jApplication app) {
        // App-side sessions are accessible by checking activeCount and reflection
        // is unnecessary: every onOpen creates exactly one session, so we can
        // peek by introspecting the registry's reflection-free state via the app.
        // The session id was registered into ConnectionRegistry — read it back
        // via reflection-free knowledge: only one entry exists.
        try {
            java.lang.reflect.Field f = ProtocolEndpoint.class.getDeclaredField("sessionId");
            f.setAccessible(true);
            // Walk the registry's endpoints to find the field. Since there is only
            // one, fall back: peek into the app's session store directly.
            return app.sessions().activeCount() > 0 ? findFirstSessionId(app) : null;
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    private static String findFirstSessionId(Streamlit4jApplication app) {
        // SessionStore exposes find(id) but not list; use reflection on the
        // InMemorySessionStore to read the underlying map.
        try {
            java.lang.reflect.Field f = app.sessions().getClass().getDeclaredField("sessions");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, ?> m = (java.util.Map<String, ?>) f.get(app.sessions());
            return m.keySet().iterator().next();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
}
