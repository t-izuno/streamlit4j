package io.streamlit4j.core.runtime;

import io.streamlit4j.core.port.CacheStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public final class InMemoryCacheStore implements CacheStore {

    private final ConcurrentMap<String, Entry> entries = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Duration ttl, Supplier<T> loader) {
        Entry existing = entries.get(key);
        if (existing != null && !existing.isExpired()) {
            return (T) existing.value;
        }
        T value = loader.get();
        entries.put(key, new Entry(value, Instant.now().plus(ttl)));
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        Entry e = entries.get(key);
        if (e == null || e.isExpired()) {
            return Optional.empty();
        }
        return Optional.of((T) e.value);
    }

    @Override
    public void invalidate(String key) {
        entries.remove(key);
    }

    @Override
    public void invalidateAll() {
        entries.clear();
    }

    public int size() {
        return entries.size();
    }

    private static final class Entry {
        final Object value;
        final Instant expiresAt;

        Entry(Object value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
