package io.streamlit4j.core.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.ScriptRunner;
import org.junit.jupiter.api.Test;

class WidgetOverloadsTest {

    @Test
    void codeWithLanguageEmitsLanguageProp() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode root = runner.render(session, () -> St.code("System.out.println();", "java"));
            assertThat(root.children().get(0).props()).containsEntry("body", "System.out.println();")
                    .containsEntry("language", "java");
        }
    }

    @Test
    void metricWithDeltaEmitsDeltaProp() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode root = runner.render(session, () -> St.metric("M", 100, "+5%"));
            assertThat(root.children().get(0).props()).containsEntry("label", "M").containsEntry("value", 100)
                    .containsEntry("delta", "+5%");
        }
    }

    @Test
    void writeStringifiesValue() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode root = runner.render(session, () -> St.write(42));
            assertThat(root.children().get(0).props()).containsEntry("value", "42");
        }
    }

    @Test
    void writeStringifiesNullAsString() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode root = runner.render(session, () -> St.write(null));
            assertThat(root.children().get(0).props()).containsEntry("value", "null");
        }
    }
}
