package io.streamlit4j.core.api;

import io.streamlit4j.core.runtime.CacheAccess;
import java.time.Duration;
import java.util.function.Supplier;

/** Data and resource caching helpers. */
final class CacheOps {

    private CacheOps() {
    }

    static <T> T cacheData(String key, Duration ttl, Supplier<T> loader) {
        return CacheAccess.dataCache().getOrLoad(key, ttl, loader);
    }

    static <T> T cacheResource(String key, Supplier<T> loader) {
        return CacheAccess.resourceCache().getOrLoad(key, Duration.ofDays(365), loader);
    }
}
