package io.streamlit4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.streamlit4j.core.domain.CustomComponent;
import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.ScriptRunner;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CustomComponentTest {

    @Test
    void componentEmitsNodeWithNameAndArgs() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1");
            CustomComponent<String> myChart = new CustomComponent<>("my-chart", String.class);

            RenderNode root =
                    runner.render(session, () -> St.component(myChart, Map.of("series", "sales", "color", "#4f46e5")));

            assertThat(root.children()).hasSize(1);
            RenderNode node = root.children().get(0);
            assertThat(node.kind()).isEqualTo("component");
            assertThat(node.props()).containsEntry("name", "my-chart");
            assertThat(node.props()).extracting("args").isInstanceOfSatisfying(Map.class, args -> {
                assertThat(args).containsEntry("series", "sales");
                assertThat(args).containsEntry("color", "#4f46e5");
            });
        }
    }

    @Test
    void componentReturnsDefaultValueOnFirstRun() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1");
            CustomComponent<String> picker = new CustomComponent<>("color-picker", String.class);

            String[] captured = new String[1];
            runner.render(session, () -> captured[0] = St.component(picker, Map.of(), "#000000"));

            assertThat(captured[0]).isEqualTo("#000000");
        }
    }

    @Test
    void componentReturnsStoredValueOnRerun() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1");
            CustomComponent<String> picker = new CustomComponent<>("color-picker", String.class);

            RenderNode firstRoot = runner.render(session, () -> St.component(picker, Map.of(), "#000000"));
            String widgetId = firstRoot.children().get(0).id();
            session.updateWidget(widgetId, "#ff0000");

            String[] captured = new String[1];
            runner.render(session, () -> captured[0] = St.component(picker, Map.of(), "#000000"));

            assertThat(captured[0]).isEqualTo("#ff0000");
        }
    }

    @Test
    void displayOnlyComponentDoesNotReadStoredValue() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1");

            RenderNode root = runner.render(session, () -> St.component("banner", Map.of("text", "hi")));

            assertThat(root.children()).hasSize(1);
            RenderNode node = root.children().get(0);
            assertThat(node.kind()).isEqualTo("component");
            assertThat(node.props()).containsEntry("name", "banner");
            assertThat(node.props()).doesNotContainKey("value");
        }
    }

    @Test
    void differentArgsProduceDifferentIds() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-1");
            CustomComponent<String> chart = new CustomComponent<>("chart", String.class);

            RenderNode root = runner.render(session, () -> {
                St.component(chart, Map.of("series", "a"));
                St.component(chart, Map.of("series", "b"));
            });

            assertThat(root.children()).hasSize(2);
            assertThat(root.children().get(0).id())
                    .isNotEqualTo(root.children().get(1).id());
        }
    }

    @Test
    void blankNameIsRejected() {
        assertThatThrownBy(() -> new CustomComponent<>(" ", String.class)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void voidComponentFactoryReturnsCorrectType() {
        CustomComponent<Void> banner = CustomComponent.ofVoid("banner");
        assertThat(banner.name()).isEqualTo("banner");
        assertThat(banner.resultType()).isEqualTo(Void.class);
    }
}
