package io.streamlit4j.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.IntNode;
import io.streamlit4j.core.api.St;
import io.streamlit4j.core.protocol.Codec;
import io.streamlit4j.core.protocol.Envelope;
import io.streamlit4j.core.protocol.RenderDelta;
import io.streamlit4j.core.protocol.SessionInit;
import io.streamlit4j.core.protocol.WidgetEvent;
import java.net.URI;
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
        try (Streamlit4jServer server = new Streamlit4jServer(0, () -> () -> {})) {
            server.start();
            assertThat(server.port()).isGreaterThan(0);
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
                client.connect(ws, URI.create("ws://localhost:" + server.port() + "/ws"))
                        .get();

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
