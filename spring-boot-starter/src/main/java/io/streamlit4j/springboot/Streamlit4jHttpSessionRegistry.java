package io.streamlit4j.springboot;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Maintains the binding between HTTP session ids and streamlit4j internal session ids.
 * <p>
 * When the HTTP session is destroyed (logout, expiry, or Spring Session backend eviction), the streamlit4j sessions
 * bound to it are looked up here so they can be terminated in lock-step.
 */
public class Streamlit4jHttpSessionRegistry {

    private final ConcurrentMap<String, Set<String>> bindings = new ConcurrentHashMap<>();

    /** Creates an empty registry. */
    public Streamlit4jHttpSessionRegistry() {
    }

    /**
     * Records a binding between an HTTP session and a streamlit4j session.
     *
     * @param httpSessionId
     *            HTTP session id
     * @param streamlit4jSessionId
     *            streamlit4j session id
     */
    public void bind(String httpSessionId, String streamlit4jSessionId) {
        bindings.computeIfAbsent(httpSessionId, key -> ConcurrentHashMap.newKeySet()).add(streamlit4jSessionId);
    }

    /**
     * Removes a single binding.
     *
     * @param httpSessionId
     *            HTTP session id
     * @param streamlit4jSessionId
     *            streamlit4j session id
     */
    public void unbind(String httpSessionId, String streamlit4jSessionId) {
        bindings.computeIfPresent(httpSessionId, (key, set) -> {
            set.remove(streamlit4jSessionId);
            return set.isEmpty() ? null : set;
        });
    }

    /**
     * Atomically removes all bindings for the HTTP session and returns the previously bound streamlit4j ids.
     *
     * @param httpSessionId
     *            HTTP session id
     *
     * @return snapshot of removed streamlit4j session ids
     */
    public Set<String> drain(String httpSessionId) {
        Set<String> removed = bindings.remove(httpSessionId);
        return removed == null ? Set.of() : Set.copyOf(removed);
    }

    /**
     * Returns the number of HTTP sessions with at least one binding.
     *
     * @return active HTTP session count
     */
    public int activeHttpSessions() {
        return bindings.size();
    }
}
