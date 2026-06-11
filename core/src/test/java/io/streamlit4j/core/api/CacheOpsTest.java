package io.streamlit4j.core.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.runtime.CacheAccess;
import io.streamlit4j.core.runtime.InMemoryCacheStore;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CacheOpsTest {

    @BeforeEach
    void freshCaches() {
        CacheAccess.useDataCache(new InMemoryCacheStore());
        CacheAccess.useResourceCache(new InMemoryCacheStore());
    }

    @AfterEach
    void restoreCaches() {
        CacheAccess.useDataCache(new InMemoryCacheStore());
        CacheAccess.useResourceCache(new InMemoryCacheStore());
    }

    @Test
    void cacheDataLoadsOnceWithinTtl() {
        AtomicInteger calls = new AtomicInteger();
        String first = St.cacheData("k", Duration.ofMinutes(5), () -> {
            calls.incrementAndGet();
            return "v";
        });
        String second = St.cacheData("k", Duration.ofMinutes(5), () -> {
            calls.incrementAndGet();
            return "v2";
        });
        assertThat(first).isEqualTo("v");
        assertThat(second).isEqualTo("v");
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void cacheResourceLoadsOnce() {
        AtomicInteger calls = new AtomicInteger();
        String first = St.cacheResource("k", () -> {
            calls.incrementAndGet();
            return "r";
        });
        String second = St.cacheResource("k", () -> {
            calls.incrementAndGet();
            return "r2";
        });
        assertThat(first).isEqualTo("r");
        assertThat(second).isEqualTo("r");
        assertThat(calls.get()).isEqualTo(1);
    }
}
