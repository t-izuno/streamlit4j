package io.streamlit4j.core.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class InMemoryCacheStoreTest {

    @Test
    void getOrLoadLoadsOnceWithinTtl() {
        InMemoryCacheStore cache = new InMemoryCacheStore();
        AtomicInteger calls = new AtomicInteger();

        String first = cache.getOrLoad("k", Duration.ofMinutes(5), () -> {
            calls.incrementAndGet();
            return "loaded";
        });
        String second = cache.getOrLoad("k", Duration.ofMinutes(5), () -> {
            calls.incrementAndGet();
            return "loaded-again";
        });

        assertThat(first).isEqualTo("loaded");
        assertThat(second).isEqualTo("loaded");
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void invalidateForcesReload() {
        InMemoryCacheStore cache = new InMemoryCacheStore();
        AtomicInteger calls = new AtomicInteger();

        cache.getOrLoad("k", Duration.ofMinutes(5), () -> {
            calls.incrementAndGet();
            return "v1";
        });
        cache.invalidate("k");
        String reloaded = cache.getOrLoad("k", Duration.ofMinutes(5), () -> {
            calls.incrementAndGet();
            return "v2";
        });

        assertThat(reloaded).isEqualTo("v2");
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void getReturnsValueWhenFresh() {
        InMemoryCacheStore cache = new InMemoryCacheStore();
        cache.getOrLoad("k", Duration.ofMinutes(5), () -> "v");
        assertThat(cache.<String> get("k")).contains("v");
    }

    @Test
    void getReturnsEmptyWhenMissing() {
        InMemoryCacheStore cache = new InMemoryCacheStore();
        assertThat(cache.get("missing")).isEmpty();
    }

    @Test
    void getReturnsEmptyWhenExpired() throws InterruptedException {
        InMemoryCacheStore cache = new InMemoryCacheStore();
        cache.getOrLoad("k", Duration.ofMillis(10), () -> "v");
        Thread.sleep(30);
        assertThat(cache.get("k")).isEmpty();
    }

    @Test
    void invalidateAllRemovesEveryEntry() {
        InMemoryCacheStore cache = new InMemoryCacheStore();
        cache.getOrLoad("a", Duration.ofMinutes(5), () -> "1");
        cache.getOrLoad("b", Duration.ofMinutes(5), () -> "2");
        assertThat(cache.size()).isEqualTo(2);
        cache.invalidateAll();
        assertThat(cache.size()).isZero();
    }

    @Test
    void expiredEntryReloads() throws InterruptedException {
        InMemoryCacheStore cache = new InMemoryCacheStore();
        AtomicInteger calls = new AtomicInteger();

        cache.getOrLoad("k", Duration.ofMillis(10), () -> {
            calls.incrementAndGet();
            return "v1";
        });
        Thread.sleep(30);
        cache.getOrLoad("k", Duration.ofMillis(10), () -> {
            calls.incrementAndGet();
            return "v2";
        });

        assertThat(calls.get()).isEqualTo(2);
    }
}
