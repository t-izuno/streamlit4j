package io.streamlit4j.core.api;

import io.streamlit4j.core.domain.SessionState;
import io.streamlit4j.core.runtime.ControlSignals;
import io.streamlit4j.core.runtime.RenderContext;

/** Script-flow control (rerun / stop / state). */
final class ControlOps {

    private ControlOps() {}

    static void rerun() {
        throw new ControlSignals.RerunRequested();
    }

    static void stop() {
        throw new ControlSignals.StopRequested();
    }

    static SessionState state() {
        return new SessionState(RenderContext.current().sessionState());
    }
}
