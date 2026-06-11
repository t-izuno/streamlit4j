package io.streamlit4j.core.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SourceWatcherTest {

    @Test
    void notifiesOnFileCreate(@TempDir Path dir) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Path> seen = new AtomicReference<>();
        try (SourceWatcher watcher = new SourceWatcher(dir, p -> {
            seen.compareAndSet(null, p);
            latch.countDown();
        })) {
            Path file = dir.resolve("a.txt");
            Files.writeString(file, "hi");
            assertThat(latch.await(20, TimeUnit.SECONDS)).isTrue();
            assertThat(seen.get()).isEqualTo(file);
        }
    }

    @Test
    void closeStopsWatcherWithoutError(@TempDir Path dir) throws Exception {
        SourceWatcher watcher = new SourceWatcher(dir, p -> {
        });
        watcher.close();
    }
}
