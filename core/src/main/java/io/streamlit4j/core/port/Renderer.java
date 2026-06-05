package io.streamlit4j.core.port;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;

/**
 * Port that executes a script entrypoint inside the streamlit4j runtime and
 * returns the resulting render tree.
 */
public interface Renderer {

    /**
     * Renders one frame.
     *
     * @param session active session whose state and widget events drive the run
     * @param entrypoint script body to execute
     * @return the new render-tree root
     */
    RenderNode render(Session session, Runnable entrypoint);
}
