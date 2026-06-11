package io.streamlit4j.server;

import io.streamlit4j.core.bootstrap.Bootstrap;
import io.streamlit4j.core.bootstrap.Streamlit4jApplication;
import io.streamlit4j.core.port.EntrypointSource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler;

/**
 * Embedded Jetty 12 server that hosts the streamlit4j WebSocket protocol endpoint, the download handler, and the
 * bundled SPA. Intended for standalone use from a plain {@code main} when streamlit4j is adopted as a library.
 */
public final class Streamlit4jServer implements AutoCloseable {

    private final Server jetty;
    private final ServerConnector connector;
    private final Streamlit4jApplication app;
    private final ConnectionRegistry connections = new ConnectionRegistry();

    /**
     * Configures (but does not start) a server on the given port.
     *
     * @param port
     *            TCP port (0 for an ephemeral port)
     * @param entrypoints
     *            source of script entrypoints
     */
    public Streamlit4jServer(int port, EntrypointSource entrypoints) {
        this.app = Bootstrap.standalone(entrypoints);
        this.jetty = new Server();
        this.connector = new ServerConnector(jetty);
        this.connector.setPort(port);
        this.jetty.addConnector(connector);
        configureHandlers();
    }

    private void configureHandlers() {
        ContextHandler context = new ContextHandler("/");
        WebSocketUpgradeHandler wsHandler = WebSocketUpgradeHandler.from(jetty, container -> {
            container.addMapping("/ws", (req, resp, callback) -> new ProtocolEndpoint(app.startSession(),
                    app.processWidgetEvent(), connections));
        });
        DownloadHandler downloadHandler = new DownloadHandler(app.downloads());
        ResourceHandler frontend = frontendHandler();
        Handler.Sequence sequence = new Handler.Sequence(wsHandler, downloadHandler,
                new FrontendRootHandler(frontend.getBaseResource()), frontend);
        context.setHandler(sequence);
        jetty.setHandler(context);
    }

    /**
     * Serves the SPA {@code index.html} at the root path {@code /}. Necessary because Jetty 12's
     * {@link ResourceHandler} welcome-file dispatch does not reliably fire when the base resource lives inside a JAR.
     */
    private static final class FrontendRootHandler extends Handler.Abstract {
        private final Resource indexResource;

        FrontendRootHandler(Resource base) {
            Resource resolved = base.resolve("index.html");
            if (resolved == null || !resolved.exists()) {
                throw new IllegalStateException("frontend index.html missing under " + base.getURI());
            }
            this.indexResource = resolved;
        }

        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception {
            String pathInContext = Request.getPathInContext(request);
            if (!"/".equals(pathInContext) && !pathInContext.isEmpty()) {
                return false;
            }
            response.getHeaders().put(HttpHeader.CONTENT_TYPE, MimeTypes.Type.TEXT_HTML_UTF_8.asString());
            try (var in = indexResource.newInputStream()) {
                byte[] bytes = in.readAllBytes();
                response.write(true, java.nio.ByteBuffer.wrap(bytes), callback);
            } catch (IOException e) {
                callback.failed(e);
            }
            return true;
        }
    }

    private static final String FRONTEND_INDEX_RESOURCE = "META-INF/resources/streamlit4j/index.html";

    /**
     * Builds a {@link ResourceHandler} that serves the bundled streamlit4j SPA from the classpath. Assets live in
     * {@code META-INF/resources/streamlit4j/} (contributed by the {@code streamlit4j-frontend-assets} jar). {@code /}
     * dispatches to {@code index.html} via the welcome-file mechanism.
     * <p>
     * The base resource is derived from the URL of {@code index.html} instead of resolving the directory directly,
     * since classloader-based directory lookups inside JAR files are unreliable.
     */
    private ResourceHandler frontendHandler() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL indexUrl = loader.getResource(FRONTEND_INDEX_RESOURCE);
        if (indexUrl == null) {
            throw new IllegalStateException(
                    "streamlit4j frontend bundle not found on classpath (missing streamlit4j-frontend-assets jar?)");
        }
        String url = indexUrl.toString();
        String baseUrl = url.substring(0, url.length() - "index.html".length());
        URI baseUri;
        try {
            baseUri = new URI(baseUrl);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("invalid frontend base URI: " + baseUrl, e);
        }
        ResourceHandler handler = new ResourceHandler();
        Resource base = ResourceFactory.of(jetty).newResource(baseUri);
        handler.setBaseResource(base);
        handler.setDirAllowed(false);
        handler.setWelcomeFiles(List.of("index.html"));
        return handler;
    }

    /**
     * Broadcasts a reload notice to every active connection.
     *
     * @param reason
     *            diagnostic reason embedded in the notice
     */
    public void notifyReload(String reason) {
        connections.broadcastReload(reason);
    }

    /**
     * Returns the count of currently active WebSocket sessions.
     *
     * @return active connection count
     */
    public int activeConnections() {
        return connections.activeConnections();
    }

    /**
     * Starts the embedded Jetty server and prints a startup banner with the local URL to {@code System.out}.
     *
     * @throws Exception
     *             when Jetty fails to start
     */
    public void start() throws Exception {
        jetty.start();
        printStartupBanner();
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private void printStartupBanner() {
        String url = "http://localhost:" + port();
        System.out.println();
        System.out.println("  streamlit4j is ready.");
        System.out.println();
        System.out.println("  Local URL: " + url);
        System.out.println("  WebSocket: " + url.replaceFirst("http", "ws") + "/ws");
        System.out.println();
        System.out.println("  Press Ctrl+C to stop.");
        System.out.println();
    }

    /**
     * Returns the listening port. Useful when the constructor port was 0.
     *
     * @return TCP port the server is bound to
     */
    public int port() {
        return connector.getLocalPort();
    }

    @Override
    public void close() throws Exception {
        jetty.stop();
        app.close();
    }
}
