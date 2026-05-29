package io.streamlit4j.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.ScriptRunner;
import io.streamlit4j.core.runtime.Session;
import org.junit.jupiter.api.Test;

class StTest {

    @Test
    void titleEmitsTitleNode() throws Exception {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1", runner);
            RenderNode root = session.rerun(() -> St.title("Hello"));

            assertThat(root.children()).hasSize(1);
            RenderNode title = root.children().get(0);
            assertThat(title.kind()).isEqualTo("title");
            assertThat(title.props()).containsEntry("text", "Hello");
        }
    }

    @Test
    void sliderReturnsDefaultOnFirstRun() throws Exception {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1", runner);
            int[] captured = new int[1];
            session.rerun(() -> captured[0] = St.slider("Year", 2018, 2026, 2025));

            assertThat(captured[0]).isEqualTo(2025);
        }
    }

    @Test
    void sliderReturnsStoredValueOnRerun() throws Exception {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1", runner);
            int[] firstId = new int[1];

            RenderNode firstRoot = session.rerun(() -> {
                St.slider("Year", 2018, 2026, 2025);
                firstId[0] = 1;
            });
            String widgetId = firstRoot.children().get(0).id();

            session.updateWidget(widgetId, 2024);

            int[] captured = new int[1];
            session.rerun(() -> captured[0] = St.slider("Year", 2018, 2026, 2025));

            assertThat(captured[0]).isEqualTo(2024);
        }
    }

    @Test
    void multipleWidgetsGetDistinctIds() throws Exception {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1", runner);
            RenderNode root = session.rerun(() -> {
                St.title("Dashboard");
                St.markdown("Body");
                St.slider("Year", 2018, 2026, 2025);
            });

            assertThat(root.children()).hasSize(3);
            assertThat(root.children().stream().map(RenderNode::id).distinct()).hasSize(3);
        }
    }
}
