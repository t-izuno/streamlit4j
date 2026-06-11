package io.streamlit4j.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SessionStateTest {

    @Test
    void getReturnsValueWhenTypeMatches() {
        Map<String, Object> store = new HashMap<>();
        store.put("name", "Alice");
        SessionState state = new SessionState(store);
        assertThat(state.get("name", String.class)).contains("Alice");
    }

    @Test
    void getReturnsEmptyWhenKeyAbsent() {
        SessionState state = new SessionState(new HashMap<>());
        assertThat(state.get("missing", String.class)).isEmpty();
    }

    @Test
    void getReturnsEmptyWhenTypeMismatched() {
        Map<String, Object> store = new HashMap<>();
        store.put("num", 42);
        SessionState state = new SessionState(store);
        assertThat(state.get("num", String.class)).isEmpty();
    }

    @Test
    void getOrDefaultReturnsValueWhenPresent() {
        Map<String, Object> store = new HashMap<>();
        store.put("k", 7);
        SessionState state = new SessionState(store);
        assertThat(state.getOrDefault("k", Integer.class, 0)).isEqualTo(7);
    }

    @Test
    void getOrDefaultReturnsFallbackWhenMissing() {
        SessionState state = new SessionState(new HashMap<>());
        assertThat(state.getOrDefault("k", Integer.class, 99)).isEqualTo(99);
    }

    @Test
    void getOrDefaultReturnsFallbackWhenTypeMismatched() {
        Map<String, Object> store = new HashMap<>();
        store.put("k", "string");
        SessionState state = new SessionState(store);
        assertThat(state.getOrDefault("k", Integer.class, -1)).isEqualTo(-1);
    }

    @Test
    void putWritesToBackingMap() {
        Map<String, Object> store = new HashMap<>();
        SessionState state = new SessionState(store);
        state.put("foo", "bar");
        assertThat(store).containsEntry("foo", "bar");
    }

    @Test
    void containsTrueAfterPut() {
        SessionState state = new SessionState(new HashMap<>());
        state.put("k", 1);
        assertThat(state.contains("k")).isTrue();
        assertThat(state.contains("missing")).isFalse();
    }

    @Test
    void removeDeletesEntry() {
        Map<String, Object> store = new HashMap<>();
        store.put("k", 1);
        SessionState state = new SessionState(store);
        state.remove("k");
        assertThat(store).doesNotContainKey("k");
    }

    @Test
    void sizeReflectsBackingMap() {
        Map<String, Object> store = new HashMap<>();
        store.put("a", 1);
        store.put("b", 2);
        SessionState state = new SessionState(store);
        assertThat(state.size()).isEqualTo(2);
    }
}
