package io.streamlit4j.core.runtime;

import io.streamlit4j.core.protocol.RenderNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RenderContext {

    private static final ThreadLocal<RenderContext> CURRENT = new ThreadLocal<>();

    private final Map<String, Object> sessionState;
    private final List<RenderNode> children = new ArrayList<>();

    public RenderContext(Map<String, Object> sessionState) {
        this.sessionState = Objects.requireNonNull(sessionState, "sessionState");
    }

    public static RenderContext current() {
        RenderContext ctx = CURRENT.get();
        if (ctx == null) {
            throw new IllegalStateException("No RenderContext bound to current thread");
        }
        return ctx;
    }

    public static void bind(RenderContext ctx) {
        CURRENT.set(ctx);
    }

    public static void unbind() {
        CURRENT.remove();
    }

    public Map<String, Object> sessionState() {
        return sessionState;
    }

    public void addNode(RenderNode node) {
        children.add(node);
    }

    public RenderNode buildRoot() {
        return RenderNode.root(List.copyOf(children));
    }
}
