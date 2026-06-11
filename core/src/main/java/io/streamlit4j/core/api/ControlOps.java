package io.streamlit4j.core.api;

import io.streamlit4j.core.domain.SessionState;
import io.streamlit4j.core.runtime.RenderContext;
import io.streamlit4j.core.runtime.RerunRequested;
import io.streamlit4j.core.runtime.StopRequested;

/** Script-flow control (rerun / stop / state). */
final class ControlOps {

    private ControlOps() {
    }

    static void rerun() {
        throw new RerunRequested();
    }

    static void stop() {
        throw new StopRequested();
    }

    static SessionState state() {
        return new SessionState(RenderContext.current().sessionState());
    }
}
