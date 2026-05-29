package io.streamlit4j.server;

import java.util.function.Supplier;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler;

public final class Streamlit4jServer implements AutoCloseable {

    private final Server jetty;
    private final ServerConnector connector;
    private final SessionRegistry sessions;
    private final Supplier<Runnable> entrypointFactory;

    public Streamlit4jServer(int port, Supplier<Runnable> entrypointFactory) {
        this.entrypointFactory = entrypointFactory;
        this.sessions = new SessionRegistry();
        this.jetty = new Server();
        this.connector = new ServerConnector(jetty);
        this.connector.setPort(port);
        this.jetty.addConnector(connector);
        configureHandlers();
    }

    private void configureHandlers() {
        ContextHandler context = new ContextHandler("/");
        WebSocketUpgradeHandler wsHandler = WebSocketUpgradeHandler.from(jetty, container -> {
            container.addMapping("/ws", (req, resp, callback) -> new ProtocolEndpoint(sessions, entrypointFactory));
        });
        context.setHandler(wsHandler);
        jetty.setHandler(context);
    }

    public void start() throws Exception {
        jetty.start();
    }

    public int port() {
        return connector.getLocalPort();
    }

    public int activeSessions() {
        return sessions.activeCount();
    }

    @Override
    public void close() throws Exception {
        jetty.stop();
        sessions.close();
    }
}
