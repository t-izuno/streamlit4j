package io.streamlit4j.core.runtime;

import org.junit.jupiter.api.Test;

class StructuredLogTest {

    // StructuredLog is a thin wrapper around SLF4J; we just exercise each
    // method to drive coverage and confirm none of them throw.

    @Test
    void rerunDoesNotThrow() {
        StructuredLog.rerun("s-1", 1L, 12L, 3);
    }

    @Test
    void sessionCreatedDoesNotThrow() {
        StructuredLog.sessionCreated("s-1");
    }

    @Test
    void sessionDestroyedDoesNotThrow() {
        StructuredLog.sessionDestroyed("s-1");
    }

    @Test
    void sessionExpiredDoesNotThrow() {
        StructuredLog.sessionExpired("s-1");
    }

    @Test
    void errorDoesNotThrow() {
        StructuredLog.error("s-1", "boom");
    }
}
