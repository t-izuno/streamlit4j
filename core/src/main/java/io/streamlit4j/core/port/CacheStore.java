package io.streamlit4j.core.port;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Port for a key/value cache backing {@code St.cacheData} and {@code St.cacheResource}. Implementations decide eviction
 * policy (TTL is honored when supplied).
 */
public interface CacheStore {

    /**
     * Returns the value cached under {@code key}, or loads and caches it.
     *
     * @param <T>
     *            stored value type
     * @param key
     *            cache key (caller-defined namespace)
     * @param ttl
     *            maximum lifetime of a freshly loaded entry
     * @param loader
     *            supplier invoked when no fresh entry exists
     *
     * @return cached or freshly loaded value
     */
    <T> T getOrLoad(String key, Duration ttl, Supplier<T> loader);

    /**
     * Returns the value cached under {@code key}, if present and still valid.
     *
     * @param <T>
     *            stored value type
     * @param key
     *            cache key
     *
     * @return present optional when a fresh entry exists
     */
    <T> Optional<T> get(String key);

    /**
     * Removes the entry for {@code key}, if any.
     *
     * @param key
     *            cache key
     */
    void invalidate(String key);

    /** Removes all entries from the store. */
    void invalidateAll();
}
