package io.streamlit4j.core.domain;

import java.util.Map;
import java.util.Optional;

public final class SessionState {

    private final Map<String, Object> store;

    public SessionState(Map<String, Object> store) {
        this.store = store;
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = store.get(key);
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    public <T> T getOrDefault(String key, Class<T> type, T fallback) {
        return get(key, type).orElse(fallback);
    }

    public void put(String key, Object value) {
        store.put(key, value);
    }

    public boolean contains(String key) {
        return store.containsKey(key);
    }

    public void remove(String key) {
        store.remove(key);
    }

    public int size() {
        return store.size();
    }
}
