package io.streamlit4j.core.runtime;

import io.streamlit4j.core.protocol.RenderNode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RenderContext {

    private static final ThreadLocal<RenderContext> CURRENT = new ThreadLocal<>();

    private final Map<String, Object> sessionState;
    private final Deque<List<RenderNode>> frames = new ArrayDeque<>();
    private boolean formSuppressed;

    public RenderContext(Map<String, Object> sessionState) {
        this.sessionState = Objects.requireNonNull(sessionState, "sessionState");
        this.frames.push(new ArrayList<>());
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
        frames.peek().add(node);
    }

    public void pushFrame() {
        frames.push(new ArrayList<>());
    }

    public List<RenderNode> popFrame() {
        return List.copyOf(frames.pop());
    }

    public RenderNode buildRoot() {
        return RenderNode.root(List.copyOf(frames.peek()));
    }

    public void reset() {
        frames.clear();
        frames.push(new ArrayList<>());
        formSuppressed = false;
    }

    public boolean isFormSuppressed() {
        return formSuppressed;
    }

    public void setFormSuppressed(boolean suppressed) {
        formSuppressed = suppressed;
    }
}
