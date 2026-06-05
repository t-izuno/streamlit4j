package io.streamlit4j.core.runtime;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches a filesystem directory and invokes a callback for each change event.
 * Used by the CLI {@code --watch} flag to trigger reloads on source edits.
 */
public final class SourceWatcher implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SourceWatcher.class);

    private final WatchService watcher;
    private final Thread thread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Creates a watcher rooted at {@code directory} and starts a virtual thread
     * that invokes {@code onChange} for each modify / create / delete event.
     *
     * @param directory directory to observe
     * @param onChange callback invoked with the absolute changed path
     * @throws Exception if the filesystem watch service cannot be opened
     */
    public SourceWatcher(Path directory, Consumer<Path> onChange) throws Exception {
        this.watcher = FileSystems.getDefault().newWatchService();
        directory.register(
                watcher,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE);
        this.thread = Thread.ofVirtual().name("streamlit4j-watcher").start(() -> loop(directory, onChange));
    }

    private void loop(Path base, Consumer<Path> onChange) {
        while (running.get()) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            for (var event : key.pollEvents()) {
                Object ctx = event.context();
                if (ctx instanceof Path p) {
                    Path full = base.resolve(p);
                    LOG.info("event=source_changed path={}", full);
                    onChange.accept(full);
                }
            }
            if (!key.reset()) {
                return;
            }
        }
    }

    @Override
    public void close() throws Exception {
        running.set(false);
        thread.interrupt();
        watcher.close();
    }
}
