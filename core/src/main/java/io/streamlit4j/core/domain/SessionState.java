package io.streamlit4j.core.domain;

import java.util.Map;
import java.util.Optional;

/**
 * Typed accessor over a session's mutable key/value state map. Exposed to scripts via {@code St.state()} for free-form
 * data sharing across reruns.
 */
public final class SessionState {

    private final Map<String, Object> store;

    /**
     * Wraps the given mutable map.
     *
     * @param store
     *            backing map (mutated through this accessor)
     */
    public SessionState(Map<String, Object> store) {
        this.store = store;
    }

    /**
     * Retrieves the value stored under {@code key} when its runtime type matches.
     *
     * @param <T>
     *            expected type
     * @param key
     *            state key
     * @param type
     *            expected runtime type
     *
     * @return present optional when the stored value is an instance of {@code type}
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = store.get(key);
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    /**
     * Returns the stored value or {@code fallback} when missing or mistyped.
     *
     * @param <T>
     *            expected type
     * @param key
     *            state key
     * @param type
     *            expected runtime type
     * @param fallback
     *            default returned when no compatible value exists
     *
     * @return stored value or {@code fallback}
     */
    public <T> T getOrDefault(String key, Class<T> type, T fallback) {
        return get(key, type).orElse(fallback);
    }

    /**
     * Writes {@code value} into the state map.
     *
     * @param key
     *            state key
     * @param value
     *            value to store
     */
    public void put(String key, Object value) {
        store.put(key, value);
    }

    /**
     * Reports whether the state map contains an entry for {@code key}.
     *
     * @param key
     *            state key
     *
     * @return {@code true} when an entry exists
     */
    public boolean contains(String key) {
        return store.containsKey(key);
    }

    /**
     * Removes the entry for {@code key}, if any.
     *
     * @param key
     *            state key
     */
    public void remove(String key) {
        store.remove(key);
    }

    /**
     * Returns the number of entries currently stored.
     *
     * @return entry count
     */
    public int size() {
        return store.size();
    }
}
