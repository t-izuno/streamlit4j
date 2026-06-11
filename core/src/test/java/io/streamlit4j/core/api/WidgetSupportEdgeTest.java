package io.streamlit4j.core.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.ScriptRunner;
import org.junit.jupiter.api.Test;

class WidgetSupportEdgeTest {

    // Exercises the type-mismatch branches in WidgetSupport.readStored /
    // readStoredInt / readStoredDouble by storing the wrong type and verifying
    // the fallback is returned.

    @Test
    void readStoredFallsBackWhenStoredTypeMismatched() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            String[] holder = new String[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.textInput("k", "def"));
            session.updateWidget(root.children().get(0).id(), 12345); // Wrong type.
            runner.render(session, () -> holder[0] = St.textInput("k", "def"));
            assertThat(holder[0]).isEqualTo("def");
        }
    }

    @Test
    void readStoredIntFallsBackWhenStoredTypeNotNumeric() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            int[] holder = new int[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.slider("k", 0, 100, 25));
            session.updateWidget(root.children().get(0).id(), "not-a-number");
            runner.render(session, () -> holder[0] = St.slider("k", 0, 100, 25));
            assertThat(holder[0]).isEqualTo(25);
        }
    }

    @Test
    void readStoredDoubleFallsBackWhenStoredTypeNotNumeric() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            double[] holder = new double[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.numberInput("k", 1.5));
            session.updateWidget(root.children().get(0).id(), "not-a-number");
            runner.render(session, () -> holder[0] = St.numberInput("k", 1.5));
            assertThat(holder[0]).isEqualTo(1.5);
        }
    }
}
