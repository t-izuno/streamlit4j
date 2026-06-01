package io.streamlit4j.core.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StructuredLog {

    private static final Logger LOG = LoggerFactory.getLogger("streamlit4j");

    private StructuredLog() {}

    public static void rerun(String sessionId, long seq, long durationMs, int elementCount) {
        LOG.info("event=rerun session={} seq={} duration_ms={} elements={}", sessionId, seq, durationMs, elementCount);
    }

    public static void sessionCreated(String sessionId) {
        LOG.info("event=session_created session={}", sessionId);
    }

    public static void sessionDestroyed(String sessionId) {
        LOG.info("event=session_destroyed session={}", sessionId);
    }

    public static void sessionExpired(String sessionId) {
        LOG.info("event=session_expired session={}", sessionId);
    }

    public static void error(String sessionId, String message) {
        LOG.error("event=error session={} message={}", sessionId, message);
    }
}
