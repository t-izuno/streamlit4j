package io.streamlit4j.core.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.streamlit4j.core.protocol.RenderNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RenderContextTest {

    @AfterEach
    void clearThreadLocal() {
        RenderContext.unbind();
    }

    @Test
    void currentThrowsWhenNoContextBound() {
        assertThatThrownBy(RenderContext::current).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No RenderContext");
    }

    @Test
    void bindThenCurrentReturnsBoundContext() {
        RenderContext ctx = new RenderContext(new HashMap<>());
        RenderContext.bind(ctx);
        assertThat(RenderContext.current()).isSameAs(ctx);
    }

    @Test
    void unbindRemovesContext() {
        RenderContext.bind(new RenderContext(new HashMap<>()));
        RenderContext.unbind();
        assertThatThrownBy(RenderContext::current).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sessionStateReturnsBackingMap() {
        Map<String, Object> store = new HashMap<>();
        store.put("k", 1);
        RenderContext ctx = new RenderContext(store);
        assertThat(ctx.sessionState()).isSameAs(store);
    }

    @Test
    void addNodeAccumulatesIntoTopFrameThenBuildRoot() {
        RenderContext ctx = new RenderContext(new HashMap<>());
        RenderNode n = new RenderNode("text", "w", Map.of(), List.of());
        ctx.addNode(n);
        RenderNode root = ctx.buildRoot();
        assertThat(root.children()).containsExactly(n);
    }

    @Test
    void pushFrameAndPopFrameReturnNestedChildren() {
        RenderContext ctx = new RenderContext(new HashMap<>());
        ctx.pushFrame();
        RenderNode child = new RenderNode("text", "w", Map.of(), List.of());
        ctx.addNode(child);
        List<RenderNode> popped = ctx.popFrame();
        assertThat(popped).containsExactly(child);
    }

    @Test
    void resetClearsFramesAndFormFlag() {
        RenderContext ctx = new RenderContext(new HashMap<>());
        ctx.addNode(new RenderNode("text", "w", Map.of(), List.of()));
        ctx.setFormSuppressed(true);
        ctx.reset();
        assertThat(ctx.buildRoot().children()).isEmpty();
        assertThat(ctx.isFormSuppressed()).isFalse();
    }

    @Test
    void formSuppressedFlagRoundTrips() {
        RenderContext ctx = new RenderContext(new HashMap<>());
        assertThat(ctx.isFormSuppressed()).isFalse();
        ctx.setFormSuppressed(true);
        assertThat(ctx.isFormSuppressed()).isTrue();
        ctx.setFormSuppressed(false);
        assertThat(ctx.isFormSuppressed()).isFalse();
    }

    @Test
    void constructorRejectsNullSessionState() {
        assertThatThrownBy(() -> new RenderContext(null)).isInstanceOf(NullPointerException.class);
    }
}
