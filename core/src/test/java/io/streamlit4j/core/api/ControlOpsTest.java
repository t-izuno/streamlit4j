package io.streamlit4j.core.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.runtime.RerunRequested;
import io.streamlit4j.core.runtime.ScriptRunner;
import io.streamlit4j.core.runtime.StopRequested;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ControlOpsTest {

    @Test
    void rerunThrowsRerunRequested() {
        assertThatThrownBy(St::rerun).isInstanceOf(RerunRequested.class);
    }

    @Test
    void stopThrowsStopRequested() {
        assertThatThrownBy(St::stop).isInstanceOf(StopRequested.class);
    }

    @Test
    void stateExposesSessionStateMap() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            session.state().put("k", 1);
            AtomicInteger captured = new AtomicInteger();
            runner.render(session, () -> {
                Integer v = St.state().get("k", Integer.class).orElse(-1);
                captured.set(v);
            });
            assertThat(captured.get()).isEqualTo(1);
        }
    }
}
