package io.streamlit4j.server;

import io.streamlit4j.core.bootstrap.Bootstrap;
import io.streamlit4j.core.bootstrap.Streamlit4jApplication;
import io.streamlit4j.core.port.EntrypointSource;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler;

public final class Streamlit4jServer implements AutoCloseable {

    private final Server jetty;
    private final ServerConnector connector;
    private final Streamlit4jApplication app;
    private final ConnectionRegistry connections = new ConnectionRegistry();

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

    public void notifyReload(String reason) {
        connections.broadcastReload(reason);
    }

    public int activeConnections() {
        return connections.activeConnections();
    }

    public void start() throws Exception {
        jetty.start();
    }

    public int port() {
        return connector.getLocalPort();
    }

    @Override
    public void close() throws Exception {
        jetty.stop();
        app.close();
    }
}
