package io.streamlit4j.server;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.port.DownloadStore;
import io.streamlit4j.core.runtime.InMemoryDownloadStore;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DownloadHandlerTest {

    private Server jetty;
    private int port;
    private InMemoryDownloadStore store;

    @BeforeEach
    void start() throws Exception {
        store = new InMemoryDownloadStore();
        DownloadHandler handler = new DownloadHandler(store);
        jetty = new Server();
        ServerConnector connector = new ServerConnector(jetty);
        connector.setPort(0);
        jetty.addConnector(connector);
        ContextHandler context = new ContextHandler("/");
        context.setHandler(handler);
        jetty.setHandler(context);
        jetty.start();
        port = connector.getLocalPort();
    }

    @AfterEach
    void stop() throws Exception {
        jetty.stop();
    }

    @Test
    void serveByRegisteredKeyReturns200WithBytes() throws Exception {
        DownloadStore.Asset asset = new DownloadStore.Asset("a.txt", "text/plain", "hello".getBytes());
        String key = store.register(asset);
        HttpResponse<byte[]> resp = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/download/" + key)).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
        assertThat(resp.statusCode()).isEqualTo(200);
        assertThat(new String(resp.body())).isEqualTo("hello");
        assertThat(resp.headers().firstValue("content-type")).hasValue("text/plain");
        assertThat(resp.headers().firstValue("content-disposition"))
                .hasValueSatisfying(v -> assertThat(v).contains("a.txt"));
    }

    @Test
    void unknownKeyReturns404() throws Exception {
        HttpResponse<String> resp = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/download/missing")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(resp.statusCode()).isEqualTo(404);
    }

    @Test
    void nonDownloadPathReturns404FromJetty() throws Exception {
        // Request a path outside the /download/ prefix — DownloadHandler returns
        // false; Jetty has no other handler in this minimal setup so it 404s.
        HttpResponse<String> resp = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/other/x")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(resp.statusCode()).isEqualTo(404);
    }
}
