package io.streamlit4j.core.runtime;

/**
 * Thrown by {@code St.stop()} to halt the script without scheduling a re-run. Used purely for control flow, not error
 * handling.
 */
public final class StopRequested extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Constructs the stop signal with a fixed message. */
    public StopRequested() {
        super("stop");
    }
}
