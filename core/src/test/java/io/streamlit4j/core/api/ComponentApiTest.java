package io.streamlit4j.core.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.domain.CustomComponent;
import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.ComponentRegistryAccess;
import io.streamlit4j.core.runtime.InMemoryComponentRegistry;
import io.streamlit4j.core.runtime.ScriptRunner;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComponentApiTest {

    @BeforeEach
    void resetRegistry() {
        ComponentRegistryAccess.use(new InMemoryComponentRegistry());
    }

    @AfterEach
    void restoreRegistry() {
        ComponentRegistryAccess.use(new InMemoryComponentRegistry());
    }

    @Test
    void registerComponentAddsToActiveRegistry() {
        CustomComponent<String> spec = new CustomComponent<>("c1", String.class);
        CustomComponent<String> returned = St.registerComponent(spec);
        assertThat(returned).isSameAs(spec);
        assertThat(ComponentRegistryAccess.registry().find("c1")).contains(spec);
    }

    @Test
    void componentEmitsNodeWithDefaultValueOnFirstRender() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            CustomComponent<String> spec = new CustomComponent<>("c", String.class);
            String[] holder = new String[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.component(spec, Map.of("x", 1), "def"));
            assertThat(holder[0]).isEqualTo("def");
            RenderNode emitted = root.children().get(0);
            assertThat(emitted.kind()).isEqualTo("component");
            assertThat(emitted.props()).containsEntry("name", "c");
            assertThat(emitted.props()).containsEntry("value", "def");
        }
    }

    @Test
    void componentWithoutDefaultReturnsNullOnFirstRender() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            CustomComponent<String> spec = new CustomComponent<>("c", String.class);
            String[] holder = new String[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.component(spec, Map.of()));
            assertThat(holder[0]).isNull();
            // Value is omitted when null.
            assertThat(root.children().get(0).props()).doesNotContainKey("value");
        }
    }

    @Test
    void componentReadsStoredValueOnRerun() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            CustomComponent<String> spec = new CustomComponent<>("c", String.class);
            String[] holder = new String[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.component(spec, Map.of(), "def"));
            session.updateWidget(root.children().get(0).id(), "updated");
            runner.render(session, () -> holder[0] = St.component(spec, Map.of(), "def"));
            assertThat(holder[0]).isEqualTo("updated");
        }
    }

    @Test
    void displayOnlyComponentEmitsWithoutReturnValue() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            RenderNode root = runner.render(session, () -> St.component("display", Map.of("y", 2)));
            assertThat(root.children().get(0).kind()).isEqualTo("component");
            assertThat(root.children().get(0).props()).containsEntry("name", "display");
        }
    }
}
