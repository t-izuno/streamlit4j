package io.streamlit4j.springboot;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Maintains the binding between HTTP session ids and streamlit4j internal session ids.
 *
 * <p>When the HTTP session is destroyed (logout, expiry, or Spring Session backend
 * eviction), the streamlit4j sessions bound to it are looked up here so they can be
 * terminated in lock-step.
 */
public class Streamlit4jHttpSessionRegistry {

    private final ConcurrentMap<String, Set<String>> bindings = new ConcurrentHashMap<>();

    public void bind(String httpSessionId, String streamlit4jSessionId) {
        bindings.computeIfAbsent(httpSessionId, key -> ConcurrentHashMap.newKeySet())
                .add(streamlit4jSessionId);
    }

    public void unbind(String httpSessionId, String streamlit4jSessionId) {
        bindings.computeIfPresent(httpSessionId, (key, set) -> {
            set.remove(streamlit4jSessionId);
            return set.isEmpty() ? null : set;
        });
    }

    public Set<String> drain(String httpSessionId) {
        Set<String> removed = bindings.remove(httpSessionId);
        return removed == null ? Set.of() : Set.copyOf(removed);
    }

    public int activeHttpSessions() {
        return bindings.size();
    }
}
