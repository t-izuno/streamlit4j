package io.streamlit4j.core.runtime;

import io.streamlit4j.core.protocol.RenderNode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Per-render thread-local context that accumulates emitted nodes and exposes the active session state to the script.
 * {@code St.*} entry points read from the currently bound context via {@link #current()}.
 */
public final class RenderContext {

    private static final ThreadLocal<RenderContext> CURRENT = new ThreadLocal<>();

    private final Map<String, Object> sessionState;
    private final Deque<List<RenderNode>> frames = new ArrayDeque<>();
    private boolean formSuppressed;

    /**
     * Creates a context backed by the given session state map.
     *
     * @param sessionState
     *            session-state backing map (mutable)
     */
    public RenderContext(Map<String, Object> sessionState) {
        this.sessionState = Objects.requireNonNull(sessionState, "sessionState");
        this.frames.push(new ArrayList<>());
    }

    /**
     * Returns the context bound to the current thread.
     *
     * @return the active context
     *
     * @throws IllegalStateException
     *             when no context is bound
     */
    public static RenderContext current() {
        RenderContext ctx = CURRENT.get();
        if (ctx == null) {
            throw new IllegalStateException("No RenderContext bound to current thread");
        }
        return ctx;
    }

    /**
     * Binds the given context to the current thread.
     *
     * @param ctx
     *            context to bind
     */
    public static void bind(RenderContext ctx) {
        CURRENT.set(ctx);
    }

    /** Unbinds any context from the current thread. */
    public static void unbind() {
        CURRENT.remove();
    }

    /**
     * Returns the session-state backing map (mutable).
     *
     * @return session state map
     */
    public Map<String, Object> sessionState() {
        return sessionState;
    }

    /**
     * Appends a node to the top frame.
     *
     * @param node
     *            node to append
     */
    public void addNode(RenderNode node) {
        frames.peek().add(node);
    }

    /** Pushes a new empty child frame onto the stack (used by container widgets). */
    public void pushFrame() {
        frames.push(new ArrayList<>());
    }

    /**
     * Pops the top frame and returns its accumulated children.
     *
     * @return immutable snapshot of the popped frame's children
     */
    public List<RenderNode> popFrame() {
        return List.copyOf(frames.pop());
    }

    /**
     * Builds the root node from the bottom frame's accumulated children.
     *
     * @return new root render node
     */
    public RenderNode buildRoot() {
        return RenderNode.root(List.copyOf(frames.peek()));
    }

    /** Resets the frame stack and form-suppression flag to a fresh empty state. */
    public void reset() {
        frames.clear();
        frames.push(new ArrayList<>());
        formSuppressed = false;
    }

    /**
     * Returns whether widgets inside the current frame should suppress immediate event delivery.
     *
     * @return {@code true} while a form is buffering events
     */
    public boolean isFormSuppressed() {
        return formSuppressed;
    }

    /**
     * Sets the form-suppression flag.
     *
     * @param suppressed
     *            new value
     */
    public void setFormSuppressed(boolean suppressed) {
        formSuppressed = suppressed;
    }
}
