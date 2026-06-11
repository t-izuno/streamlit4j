package io.streamlit4j.core.application;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.core.port.Renderer;
import io.streamlit4j.core.port.SessionStore;
import io.streamlit4j.core.protocol.Patch;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.protocol.RenderTreeDiff;
import java.util.List;

/**
 * Application use case that updates session state with an incoming widget event and re-runs the script to produce the
 * next render-tree diff.
 */
public final class ProcessWidgetEvent {

    private final SessionStore sessions;
    private final EntrypointSource entrypoints;
    private final Renderer renderer;

    /**
     * Wires the use case with its dependencies.
     *
     * @param sessions
     *            session store port
     * @param entrypoints
     *            entrypoint source port
     * @param renderer
     *            renderer port
     */
    public ProcessWidgetEvent(SessionStore sessions, EntrypointSource entrypoints, Renderer renderer) {
        this.sessions = sessions;
        this.entrypoints = entrypoints;
        this.renderer = renderer;
    }

    /**
     * Applies the widget event to the named session and returns the next render frame.
     *
     * @param sessionId
     *            owning session id
     * @param widgetId
     *            widget that emitted the event
     * @param value
     *            new widget value
     *
     * @return result containing the new root and the patch list to send
     */
    public Result execute(String sessionId, String widgetId, Object value) {
        Session session = sessions.find(sessionId)
                .orElseThrow(() -> new IllegalStateException("Unknown session: " + sessionId));
        RenderNode previous = session.lastRoot().orElse(null);
        session.updateWidget(widgetId, value);
        RenderNode current = renderer.render(session, entrypoints.next());
        List<Patch> patches = RenderTreeDiff.diff(previous, current);
        long seq = session.nextSeq();
        return new Result(sessionId, seq, current, patches);
    }

    /**
     * Outcome of {@link #execute(String, String, Object)}.
     *
     * @param sessionId
     *            owning session id
     * @param seq
     *            monotonic sequence number for this frame
     * @param root
     *            current render root
     * @param patches
     *            keyed-diff patch list to apply on the client
     */
    public record Result(String sessionId, long seq, RenderNode root, List<Patch> patches) {
    }
}
