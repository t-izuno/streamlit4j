package io.streamlit4j.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.streamlit4j.core.api.St;
import io.streamlit4j.core.protocol.Codec;
import io.streamlit4j.core.protocol.Envelope;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.protocol.RenderDelta;
import io.streamlit4j.core.protocol.SessionInit;
import io.streamlit4j.core.protocol.WidgetEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;

class Streamlit4jServerTest {

    @Test
    void startsOnEphemeralPortAndStops() throws Exception {
        try (Streamlit4jServer server = new Streamlit4jServer(0, () -> () -> {
        })) {
            server.start();
            assertThat(server.port()).isGreaterThan(0);
        }
    }

    @Test
    void servesBundledFrontendIndexHtmlAtRoot() throws Exception {
        try (Streamlit4jServer server = new Streamlit4jServer(0, () -> () -> {
        })) {
            server.start();
            HttpResponse<String> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + "/")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("content-type"))
                    .hasValueSatisfying(ct -> assertThat(ct).startsWith("text/html"));
            assertThat(response.body()).contains("<!doctype html").contains("<div id=\"root\">");
        }
    }

    @Test
    void standaloneOidcAuthRedirectsAndMaintainsCookieSession() throws Exception {
        TokenServer tokenServer = new TokenServer();
        tokenServer.start();
        StandaloneAuthConfig auth = StandaloneAuthConfig.oidc(URI.create("https://idp.example/authorize"),
                tokenServer.uri(), "client-1", "secret-1", URI.create("http://localhost/callback"));
        try (Streamlit4jServer server = new Streamlit4jServer(0, () -> () -> {
        }, auth)) {
            server.start();
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

            HttpResponse<String> root = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + "/")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(root.statusCode()).isEqualTo(302);
            assertThat(root.headers().firstValue("location")).hasValue("/auth/login");

            HttpResponse<String> login = client.send(HttpRequest
                    .newBuilder(URI.create("http://localhost:" + server.port() + "/auth/login")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(login.statusCode()).isEqualTo(302);
            assertThat(login.headers().firstValue("location"))
                    .hasValueSatisfying(location -> assertThat(location).contains("https://idp.example/authorize")
                            .contains("response_type=code").contains("client_id=client-1").contains("state="));
            String stateCookie = login.headers().firstValue("set-cookie").orElseThrow().split(";", 2)[0];
            String state = queryParam(login.headers().firstValue("location").orElseThrow(), "state");

            HttpResponse<String> callback = client.send(HttpRequest
                    .newBuilder(
                            URI.create("http://localhost:" + server.port() + "/auth/callback?code=abc&state=" + state))
                    .header("Cookie", stateCookie).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(callback.statusCode()).isEqualTo(302);
            assertThat(callback.headers().firstValue("set-cookie"))
                    .hasValueSatisfying(cookie -> assertThat(cookie).contains("streamlit4j_auth="));
            assertThat(tokenServer.requests).singleElement()
                    .satisfies(body -> assertThat(body).contains("grant_type=authorization_code").contains("code=abc")
                            .contains("client_id=client-1").contains("client_secret=secret-1"));

            String cookie = callback.headers().firstValue("set-cookie").orElseThrow().split(";", 2)[0];
            HttpResponse<String> authedRoot = client
                    .send(HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + "/"))
                            .header("Cookie", cookie).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(authedRoot.statusCode()).isEqualTo(200);
            assertThat(authedRoot.body()).contains("<!doctype html");
        } finally {
            tokenServer.stop();
        }
    }

    @Test
    void standaloneOidcCallbackRejectsInvalidState() throws Exception {
        TokenServer tokenServer = new TokenServer();
        tokenServer.start();
        StandaloneAuthConfig auth = StandaloneAuthConfig.oidc(URI.create("https://idp.example/authorize"),
                tokenServer.uri(), "client-1", "secret-1", URI.create("http://localhost/callback"));
        try (Streamlit4jServer server = new Streamlit4jServer(0, () -> () -> {
        }, auth)) {
            server.start();
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

            HttpResponse<String> callback = client.send(HttpRequest
                    .newBuilder(
                            URI.create("http://localhost:" + server.port() + "/auth/callback?code=abc&state=attacker"))
                    .GET().build(), HttpResponse.BodyHandlers.ofString());

            assertThat(callback.statusCode()).isEqualTo(400);
            assertThat(tokenServer.requests).isEmpty();
        } finally {
            tokenServer.stop();
        }
    }

    @Test
    void servesBundledFrontendAssetByExactPath() throws Exception {
        try (Streamlit4jServer server = new Streamlit4jServer(0, () -> () -> {
        })) {
            server.start();
            HttpResponse<String> indexResp = HttpClient.newHttpClient().send(HttpRequest
                    .newBuilder(URI.create("http://localhost:" + server.port() + "/index.html")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(indexResp.statusCode()).isEqualTo(200);
            assertThat(indexResp.body()).contains("<!doctype html");
        }
    }

    @Test
    void deliversSessionInitOnConnect() throws Exception {
        try (Streamlit4jServer server = new Streamlit4jServer(0, () -> () -> St.title("Hello"))) {
            server.start();
            Envelope first = connectAndAwaitFirstMessage(server.port(), null);

            assertThat(first).isInstanceOf(SessionInit.class);
            SessionInit init = (SessionInit) first;
            assertThat(init.root().children()).hasSize(1);
            assertThat(init.root().children().get(0).kind()).isEqualTo("title");
            assertThat(init.root().children().get(0).props()).containsEntry("text", "Hello");
        }
    }

    @Test
    void widgetEventTriggersRerunAndRenderDelta() throws Exception {
        try (Streamlit4jServer server = new Streamlit4jServer(0, () -> () -> St.slider("Year", 2018, 2026, 2025))) {
            server.start();

            WebSocketClient client = new WebSocketClient();
            client.start();
            try {
                CapturingClient ws = new CapturingClient();
                client.connect(ws, URI.create("ws://localhost:" + server.port() + "/ws")).get();

                Envelope first = ws.received.poll(2, TimeUnit.SECONDS);
                assertThat(first).isInstanceOf(SessionInit.class);
                String widgetId = ((SessionInit) first).root().children().get(0).id();

                WidgetEvent event = WidgetEvent.of(((SessionInit) first).sessionId(), widgetId, IntNode.valueOf(2024));
                ws.session.sendText(Codec.encode(event), Callback.NOOP);

                Envelope second = ws.received.poll(2, TimeUnit.SECONDS);
                assertThat(second).isInstanceOf(RenderDelta.class);
                RenderDelta delta = (RenderDelta) second;
                assertThat(delta.patches()).hasSize(1);
                assertThat(delta.patches().get(0).op()).isEqualTo("replace");
                assertThat(delta.patches().get(0).path()).isEqualTo("main/0");
                assertThat(delta.patches().get(0).node().props()).containsEntry("value", 2024);

                ws.session.close();
            } finally {
                client.stop();
            }
        }
    }

    @Test
    void sseEventsEndpointDeliversSessionInit() throws Exception {
        try (Streamlit4jServer server = new Streamlit4jServer(0, () -> () -> St.title("Hello SSE"))) {
            server.start();
            HttpResponse<InputStream> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + "/events")).GET().build(),
                    HttpResponse.BodyHandlers.ofInputStream());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("content-type"))
                    .hasValueSatisfying(ct -> assertThat(ct).startsWith("text/event-stream"));

            Envelope first = readSseEnvelope(response.body());
            assertThat(first).isInstanceOf(SessionInit.class);
            SessionInit init = (SessionInit) first;
            assertThat(init.root().children().get(0).kind()).isEqualTo("title");
            assertThat(init.root().children().get(0).props()).containsEntry("text", "Hello SSE");
        }
    }

    @Test
    void postedWidgetEventTriggersRenderDeltaOnSseConnection() throws Exception {
        try (Streamlit4jServer server = new Streamlit4jServer(0, () -> () -> St.slider("Year", 2018, 2026, 2025))) {
            server.start();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<InputStream> response = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + "/events")).GET().build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            SseReader reader = new SseReader(response.body());
            SessionInit init = (SessionInit) reader.readEnvelope();
            String widgetId = init.root().children().get(0).id();

            WidgetEvent event = WidgetEvent.of(init.sessionId(), widgetId, IntNode.valueOf(2024));
            HttpResponse<String> postResponse = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + "/events"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(Codec.encode(event))).build(),
                    HttpResponse.BodyHandlers.ofString());

            assertThat(postResponse.statusCode()).isEqualTo(202);
            Envelope second = reader.readEnvelope();
            assertThat(second).isInstanceOf(RenderDelta.class);
            RenderDelta delta = (RenderDelta) second;
            assertThat(delta.patches()).hasSize(1);
            assertThat(delta.patches().get(0).node().props()).containsEntry("value", 2024);
        }
    }

    @Test
    void sseDeliversWriteStreamAsIncrementalRenderDeltas() throws Exception {
        try (Streamlit4jServer server = new Streamlit4jServer(0, () -> () -> {
            String prompt = St.chatInput("Prompt");
            if (prompt != null) {
                St.writeStream(List.of("Hel", "lo"));
            }
        })) {
            server.start();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<InputStream> response = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + "/events")).GET().build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            SseReader reader = new SseReader(response.body());
            SessionInit init = (SessionInit) reader.readEnvelope();
            String widgetId = init.root().children().get(0).id();

            WidgetEvent event = WidgetEvent.of(init.sessionId(), widgetId, TextNode.valueOf("hi"));
            HttpResponse<String> postResponse = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + "/events"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(Codec.encode(event))).build(),
                    HttpResponse.BodyHandlers.ofString());

            assertThat(postResponse.statusCode()).isEqualTo(202);
            RenderDelta firstDelta = (RenderDelta) reader.readEnvelope();
            assertThat(firstChatStream(firstDelta).props()).containsEntry("tokens", List.of("Hel"));
            RenderDelta secondDelta = (RenderDelta) reader.readEnvelope();
            assertThat(firstChatStream(secondDelta).props()).containsEntry("tokens", List.of("Hel", "lo"));
        }
    }

    private Envelope connectAndAwaitFirstMessage(int port, String _unused) throws Exception {
        WebSocketClient client = new WebSocketClient();
        client.start();
        try {
            CapturingClient ws = new CapturingClient();
            client.connect(ws, URI.create("ws://localhost:" + port + "/ws")).get();
            Envelope first = ws.received.poll(2, TimeUnit.SECONDS);
            ws.session.close();
            return first;
        } finally {
            client.stop();
        }
    }

    private static Envelope readSseEnvelope(InputStream stream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("data: ")) {
                return Codec.decode(line.substring("data: ".length()));
            }
        }
        throw new AssertionError("SSE stream ended before a data line was received");
    }

    private static RenderNode firstChatStream(RenderDelta delta) {
        assertThat(delta.patches()).isNotEmpty();
        for (var patch : delta.patches()) {
            RenderNode found = firstChatStreamOrNull(patch.node());
            if (found != null) {
                return found;
            }
        }
        throw new AssertionError("No chat_stream node found in render_delta");
    }

    private static RenderNode firstChatStream(RenderNode node) {
        assertThat(node).isNotNull();
        if ("chat_stream".equals(node.kind())) {
            return node;
        }
        for (RenderNode child : node.children()) {
            RenderNode found = firstChatStreamOrNull(child);
            if (found != null) {
                return found;
            }
        }
        throw new AssertionError("No chat_stream node found below " + node.kind());
    }

    private static RenderNode firstChatStreamOrNull(RenderNode node) {
        if (node == null) {
            return null;
        }
        if ("chat_stream".equals(node.kind())) {
            return node;
        }
        for (RenderNode child : node.children()) {
            RenderNode found = firstChatStreamOrNull(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static final class SseReader {
        private final BufferedReader reader;

        SseReader(InputStream stream) {
            this.reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }

        Envelope readEnvelope() throws Exception {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    return Codec.decode(line.substring("data: ".length()));
                }
            }
            throw new AssertionError("SSE stream ended before a data line was received");
        }
    }

    private static String queryParam(String uri, String name) {
        String query = URI.create(uri).getQuery();
        assertThat(query).isNotNull();
        for (String part : query.split("&")) {
            String[] pieces = part.split("=", 2);
            if (pieces.length == 2 && pieces[0].equals(name)) {
                return URLDecoder.decode(pieces[1], StandardCharsets.UTF_8);
            }
        }
        throw new AssertionError("Missing query parameter: " + name);
    }

    private static final class TokenServer {
        private final com.sun.net.httpserver.HttpServer server;
        private final List<String> requests = new ArrayList<>();

        TokenServer() throws Exception {
            server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(0), 0);
            server.createContext("/token", exchange -> {
                requests.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                byte[] body = "{\"id_token\":\"id-token\",\"access_token\":\"access-token\"}"
                        .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
        }

        void start() {
            server.start();
        }

        void stop() {
            server.stop(0);
        }

        URI uri() {
            return URI.create("http://localhost:" + server.getAddress().getPort() + "/token");
        }
    }

    @WebSocket
    public static class CapturingClient {
        final BlockingQueue<Envelope> received = new LinkedBlockingQueue<>();
        Session session;

        @OnWebSocketOpen
        public void onOpen(Session session) {
            this.session = session;
        }

        @OnWebSocketMessage
        public void onMessage(String text) {
            received.add(Codec.decode(text));
        }
    }
}
