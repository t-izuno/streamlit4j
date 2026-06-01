package io.streamlit4j.core.application;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.core.port.Renderer;
import io.streamlit4j.core.port.SessionStore;
import io.streamlit4j.core.protocol.Patch;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.protocol.RenderTreeDiff;
import java.util.List;

public final class ProcessWidgetEvent {

    private final SessionStore sessions;
    private final EntrypointSource entrypoints;
    private final Renderer renderer;

    public ProcessWidgetEvent(SessionStore sessions, EntrypointSource entrypoints, Renderer renderer) {
        this.sessions = sessions;
        this.entrypoints = entrypoints;
        this.renderer = renderer;
    }

    public Result execute(String sessionId, String widgetId, Object value) {
        Session session =
                sessions.find(sessionId).orElseThrow(() -> new IllegalStateException("Unknown session: " + sessionId));
        RenderNode previous = session.lastRoot().orElse(null);
        session.updateWidget(widgetId, value);
        RenderNode current = renderer.render(session, entrypoints.next());
        List<Patch> patches = RenderTreeDiff.diff(previous, current);
        long seq = session.nextSeq();
        return new Result(sessionId, seq, current, patches);
    }

    public record Result(String sessionId, long seq, RenderNode root, List<Patch> patches) {}
}
