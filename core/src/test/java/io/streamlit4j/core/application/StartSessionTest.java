package io.streamlit4j.core.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.InMemorySessionStore;
import java.util.List;
import org.junit.jupiter.api.Test;

class StartSessionTest {

    @Test
    void executeCreatesSessionAndReturnsInitialRoot() {
        InMemorySessionStore sessions = new InMemorySessionStore();
        RenderNode rendered = RenderNode.root(List.of());
        Runnable entry = () -> {
        };
        StartSession useCase = new StartSession(sessions, () -> entry, (session, e) -> rendered);

        StartSession.Result result = useCase.execute();

        assertThat(result.sessionId()).isNotBlank();
        assertThat(result.root()).isSameAs(rendered);
        assertThat(sessions.find(result.sessionId())).isPresent();
    }

    @Test
    void resultExposesIdAndRoot() {
        RenderNode root = RenderNode.root(List.of());
        StartSession.Result result = new StartSession.Result("s-1", root);
        assertThat(result.sessionId()).isEqualTo("s-1");
        assertThat(result.root()).isSameAs(root);
    }
}
