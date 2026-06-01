package io.streamlit4j.core.runtime;

public final class ControlSignals {

    private ControlSignals() {}

    public static final class RerunRequested extends RuntimeException {
        public RerunRequested() {
            super("rerun");
        }
    }

    public static final class StopRequested extends RuntimeException {
        public StopRequested() {
            super("stop");
        }
    }
}
