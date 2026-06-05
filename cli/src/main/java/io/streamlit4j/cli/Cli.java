package io.streamlit4j.cli;

import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.core.runtime.SourceWatcher;
import io.streamlit4j.examples.Hello;
import io.streamlit4j.server.Streamlit4jServer;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standalone CLI entry point that boots an embedded Jetty server with the
 * {@code Hello} demo. Distributed via JBang for one-line installs.
 */
public final class Cli {

    private static final int DEFAULT_PORT = 8501;
    private static final Logger LOG = LoggerFactory.getLogger(Cli.class);

    private Cli() {}

    /**
     * CLI entry point.
     *
     * @param args supports {@code --port <n>}, {@code --watch <dir>}, or a bare port number
     * @throws Exception when the server fails to start or the watcher fails
     */
    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        Path watchDir = null;
        int i = 0;
        while (i < args.length) {
            String arg = args[i];
            if ("--watch".equals(arg) && i + 1 < args.length) {
                watchDir = Paths.get(args[i + 1]);
                i += 2;
            } else if ("--port".equals(arg) && i + 1 < args.length) {
                port = Integer.parseInt(args[i + 1]);
                i += 2;
            } else {
                if (arg.matches("\\d+")) {
                    port = Integer.parseInt(arg);
                }
                i++;
            }
        }

        EntrypointSource entrypoints = () -> Hello::run;
        try (Streamlit4jServer server = new Streamlit4jServer(port, entrypoints)) {
            server.start();
            LOG.info("streamlit4j listening on ws://localhost:{}/ws", server.port());

            if (watchDir != null) {
                try (SourceWatcher ignored =
                        new SourceWatcher(watchDir, p -> server.notifyReload("source_change:" + p))) {
                    LOG.info("watching {} for source changes", watchDir);
                    Thread.currentThread().join();
                }
            } else {
                Thread.currentThread().join();
            }
        }
    }
}
