package io.streamlit4j.core.application;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.core.port.Renderer;
import io.streamlit4j.core.port.SessionStore;
import io.streamlit4j.core.protocol.RenderNode;

public final class StartSession {

    private final SessionStore sessions;
    private final EntrypointSource entrypoints;
    private final Renderer renderer;

    public StartSession(SessionStore sessions, EntrypointSource entrypoints, Renderer renderer) {
        this.sessions = sessions;
        this.entrypoints = entrypoints;
        this.renderer = renderer;
    }

    public Result execute() {
        Session session = sessions.create();
        RenderNode root = renderer.render(session, entrypoints.next());
        return new Result(session.id(), root);
    }

    public record Result(String sessionId, RenderNode root) {}
}
