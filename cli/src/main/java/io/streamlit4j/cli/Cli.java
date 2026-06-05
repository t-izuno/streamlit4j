package io.streamlit4j.cli;

import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.core.runtime.SourceWatcher;
import io.streamlit4j.examples.Hello;
import io.streamlit4j.server.Streamlit4jServer;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Cli {

    private static final int DEFAULT_PORT = 8501;

    private Cli() {}

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        Path watchDir = null;
        for (int i = 0; i < args.length; i++) {
            if ("--watch".equals(args[i]) && i + 1 < args.length) {
                watchDir = Paths.get(args[++i]);
            } else if ("--port".equals(args[i]) && i + 1 < args.length) {
                port = Integer.parseInt(args[++i]);
            } else if (args[i].matches("\\d+")) {
                port = Integer.parseInt(args[i]);
            }
        }

        EntrypointSource entrypoints = () -> Hello::run;
        try (Streamlit4jServer server = new Streamlit4jServer(port, entrypoints)) {
            server.start();
            System.out.println("streamlit4j listening on ws://localhost:" + server.port() + "/ws");

            if (watchDir != null) {
                try (SourceWatcher ignored =
                        new SourceWatcher(watchDir, p -> server.notifyReload("source_change:" + p))) {
                    System.out.println("watching " + watchDir + " for source changes");
                    Thread.currentThread().join();
                }
            } else {
                Thread.currentThread().join();
            }
        }
    }
}
