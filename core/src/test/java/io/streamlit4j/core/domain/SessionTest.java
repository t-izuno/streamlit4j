package io.streamlit4j.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.streamlit4j.core.protocol.RenderNode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SessionTest {

    @Test
    void idReturnsConstructorValue() {
        Session session = new Session("s-1");
        assertThat(session.id()).isEqualTo("s-1");
    }

    @Test
    void stateIsInitiallyEmptyAndMutable() {
        Session session = new Session("s-1");
        assertThat(session.state()).isEmpty();
        session.state().put("k", "v");
        assertThat(session.state()).containsEntry("k", "v");
    }

    @Test
    void typedStateWrapsBackingMap() {
        Session session = new Session("s-1");
        session.state().put("name", "Alice");
        SessionState typed = session.typedState();
        assertThat(typed.get("name", String.class)).contains("Alice");
    }

    @Test
    void updateWidgetWritesValueAndTouchesActivity() throws InterruptedException {
        Session session = new Session("s-1");
        Instant before = session.lastActivity();
        Thread.sleep(2);
        session.updateWidget("w-x", 42);
        assertThat(session.state().get("w-x")).isEqualTo(42);
        assertThat(session.lastActivity()).isAfterOrEqualTo(before);
    }

    @Test
    void nextSeqIncrementsMonotonically() {
        Session session = new Session("s-1");
        assertThat(session.nextSeq()).isEqualTo(1);
        assertThat(session.nextSeq()).isEqualTo(2);
        assertThat(session.nextSeq()).isEqualTo(3);
    }

    @Test
    void touchUpdatesLastActivity() throws InterruptedException {
        Session session = new Session("s-1");
        Instant before = session.lastActivity();
        Thread.sleep(2);
        session.touch();
        assertThat(session.lastActivity()).isAfter(before);
    }

    @Test
    void lastRootIsEmptyByDefault() {
        Session session = new Session("s-1");
        assertThat(session.lastRoot()).isEmpty();
    }

    @Test
    void setLastRootIsObservableViaLastRoot() {
        Session session = new Session("s-1");
        RenderNode root = RenderNode.root(List.of());
        session.setLastRoot(root);
        assertThat(session.lastRoot()).contains(root);
    }

    @Test
    void withRerunLockReturnsBlockResult() {
        Session session = new Session("s-1");
        Integer result = session.withRerunLock(() -> 42);
        assertThat(result).isEqualTo(42);
    }

    @Test
    void withRerunLockReleasesOnException() {
        Session session = new Session("s-1");
        assertThatThrownBy(() -> session.withRerunLock(() -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);
        // After exception, lock should be re-acquirable.
        assertThat(session.withRerunLock(() -> "ok")).isEqualTo("ok");
    }

    @Test
    void stateMapIsTheSameInstanceAcrossCalls() {
        Session session = new Session("s-1");
        Map<String, Object> first = session.state();
        Map<String, Object> second = session.state();
        assertThat(first).isSameAs(second);
    }
}
