package io.streamlit4j.server;

import io.streamlit4j.core.bootstrap.Bootstrap;
import io.streamlit4j.core.bootstrap.Streamlit4jApplication;
import io.streamlit4j.core.port.EntrypointSource;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler;

/**
 * Embedded Jetty 12 server that hosts the streamlit4j WebSocket protocol
 * endpoint and the download handler. Suitable for CLI / JBang standalone use.
 */
public final class Streamlit4jServer implements AutoCloseable {

    private final Server jetty;
    private final ServerConnector connector;
    private final Streamlit4jApplication app;
    private final ConnectionRegistry connections = new ConnectionRegistry();

    /**
     * Configures (but does not start) a server on the given port.
     *
     * @param port TCP port (0 for an ephemeral port)
     * @param entrypoints source of script entrypoints
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
            container.addMapping(
                    "/ws",
                    (req, resp, callback) ->
                            new ProtocolEndpoint(app.startSession(), app.processWidgetEvent(), connections));
        });
        DownloadHandler downloadHandler = new DownloadHandler(app.downloads());
        Handler.Sequence sequence = new Handler.Sequence(downloadHandler, wsHandler);
        context.setHandler(sequence);
        jetty.setHandler(context);
    }

    /**
     * Broadcasts a reload notice to every active connection.
     *
     * @param reason diagnostic reason embedded in the notice
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
     * Starts the embedded Jetty server.
     *
     * @throws Exception when Jetty fails to start
     */
    public void start() throws Exception {
        jetty.start();
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
