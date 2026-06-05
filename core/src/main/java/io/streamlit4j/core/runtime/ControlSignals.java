package io.streamlit4j.core.runtime;

/**
 * Sentinel exceptions used by {@code St.rerun()} and {@code St.stop()} to
 * unwind script execution back to {@code ScriptRunner}. They are not thrown
 * for error handling — only for control flow.
 */
public final class ControlSignals {

    private ControlSignals() {}

    /** Thrown by {@code St.rerun()} to request an immediate re-execution of the script. */
    public static final class RerunRequested extends RuntimeException {
        /** Constructs the rerun signal with a fixed message. */
        public RerunRequested() {
            super("rerun");
        }
    }

    /** Thrown by {@code St.stop()} to halt the script without scheduling a re-run. */
    public static final class StopRequested extends RuntimeException {
        /** Constructs the stop signal with a fixed message. */
        public StopRequested() {
            super("stop");
        }
    }
}
