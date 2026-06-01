package io.streamlit4j.core.port;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public interface CacheStore {

    <T> T getOrLoad(String key, Duration ttl, Supplier<T> loader);

    <T> Optional<T> get(String key);

    void invalidate(String key);

    void invalidateAll();
}
