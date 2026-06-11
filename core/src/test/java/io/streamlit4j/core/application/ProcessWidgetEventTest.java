package io.streamlit4j.core.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.port.Renderer;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.InMemorySessionStore;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ProcessWidgetEventTest {

    @Test
    void executeProducesReplacePatchOnFirstFrame() {
        InMemorySessionStore sessions = new InMemorySessionStore();
        Session session = sessions.create();
        RenderNode newRoot = RenderNode.root(List.of(new RenderNode("t", "w", Map.of("v", 1), List.of())));
        Renderer renderer = (s, r) -> newRoot;
        ProcessWidgetEvent useCase = new ProcessWidgetEvent(sessions, () -> () -> {
        }, renderer);

        ProcessWidgetEvent.Result result = useCase.execute(session.id(), "w-1", 99);

        assertThat(result.sessionId()).isEqualTo(session.id());
        assertThat(result.seq()).isEqualTo(1L);
        assertThat(result.root()).isSameAs(newRoot);
        assertThat(result.patches()).hasSize(1);
        assertThat(result.patches().get(0).op()).isEqualTo("replace");
        assertThat(session.state().get("w-1")).isEqualTo(99);
    }

    @Test
    void executeReusesPreviousRootForDiff() {
        InMemorySessionStore sessions = new InMemorySessionStore();
        Session session = sessions.create();
        RenderNode initial = RenderNode.root(List.of());
        session.setLastRoot(initial);

        AtomicReference<RenderNode> captured = new AtomicReference<>();
        Renderer renderer = (s, r) -> {
            captured.set(s.lastRoot().orElse(null));
            return initial;
        };
        ProcessWidgetEvent useCase = new ProcessWidgetEvent(sessions, () -> () -> {
        }, renderer);

        ProcessWidgetEvent.Result result = useCase.execute(session.id(), "w-1", "x");

        assertThat(captured.get()).isSameAs(initial);
        assertThat(result.patches()).isEmpty(); // unchanged tree
    }

    @Test
    void executeThrowsForUnknownSession() {
        InMemorySessionStore sessions = new InMemorySessionStore();
        ProcessWidgetEvent useCase = new ProcessWidgetEvent(sessions, () -> () -> {
        }, (s, r) -> RenderNode.root(List.of()));

        assertThatThrownBy(() -> useCase.execute("nope", "w", 1)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unknown session");
    }
}
