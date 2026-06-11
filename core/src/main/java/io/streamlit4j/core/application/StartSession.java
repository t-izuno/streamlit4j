package io.streamlit4j.core.application;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.port.EntrypointSource;
import io.streamlit4j.core.port.Renderer;
import io.streamlit4j.core.port.SessionStore;
import io.streamlit4j.core.protocol.RenderNode;

/**
 * Application use case that creates a fresh session and produces the first render frame from the resolved entrypoint.
 */
public final class StartSession {

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
    public StartSession(SessionStore sessions, EntrypointSource entrypoints, Renderer renderer) {
        this.sessions = sessions;
        this.entrypoints = entrypoints;
        this.renderer = renderer;
    }

    /**
     * Creates a new session and renders the first frame.
     *
     * @return result with the new session id and initial render root
     */
    public Result execute() {
        Session session = sessions.create();
        RenderNode root = renderer.render(session, entrypoints.next());
        return new Result(session.id(), root);
    }

    /**
     * Outcome of {@link #execute()}.
     *
     * @param sessionId
     *            new session id
     * @param root
     *            initial render-tree root
     */
    public record Result(String sessionId, RenderNode root) {
    }
}
