package io.streamlit4j.core.runtime;

/**
 * Thrown by {@code St.rerun()} to request an immediate re-execution of the script. Used purely for control flow, not
 * error handling.
 */
public final class RerunRequested extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Constructs the rerun signal with a fixed message. */
    public RerunRequested() {
        super("rerun");
    }
}
