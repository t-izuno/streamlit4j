package io.streamlit4j.core.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ScriptRunnerTest {

    @Test
    void renderRunsEntrypointAndProducesRoot() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1");
            AtomicInteger calls = new AtomicInteger();
            Runnable entry = calls::incrementAndGet;
            RenderNode root = runner.render(session, entry);
            assertThat(root.kind()).isEqualTo("root");
            assertThat(calls.get()).isEqualTo(1);
            assertThat(session.lastRoot()).contains(root);
        }
    }

    @Test
    void rerunSignalRestartsEntrypointUntilCompletion() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1");
            AtomicInteger calls = new AtomicInteger();
            Runnable entry = () -> {
                if (calls.incrementAndGet() < 3) {
                    throw new RerunRequested();
                }
            };
            runner.render(session, entry);
            assertThat(calls.get()).isEqualTo(3);
        }
    }

    @Test
    void stopSignalEndsRenderImmediately() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1");
            AtomicInteger calls = new AtomicInteger();
            Runnable entry = () -> {
                calls.incrementAndGet();
                throw new StopRequested();
            };
            RenderNode root = runner.render(session, entry);
            assertThat(calls.get()).isEqualTo(1);
            assertThat(root.kind()).isEqualTo("root");
        }
    }

    @Test
    void tooManyRerunsRaisesIllegalStateException() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1");
            Runnable entry = () -> {
                throw new RerunRequested();
            };
            assertThatThrownBy(() -> runner.render(session, entry)).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Render failed");
        }
    }

    @Test
    void renderFailurePropagatesAsRuntimeException() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1");
            Runnable entry = () -> {
                throw new IllegalArgumentException("script error");
            };
            assertThatThrownBy(() -> runner.render(session, entry)).isInstanceOf(RuntimeException.class);
        }
    }
}
