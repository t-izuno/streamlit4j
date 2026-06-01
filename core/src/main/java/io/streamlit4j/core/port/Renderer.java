package io.streamlit4j.core.port;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;

public interface Renderer {

    RenderNode render(Session session, Runnable entrypoint);
}
