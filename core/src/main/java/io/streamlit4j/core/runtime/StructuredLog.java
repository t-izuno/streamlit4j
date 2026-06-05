package io.streamlit4j.core.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single-line key=value structured logger for runtime events. Centralizing the
 * log keys here keeps log-grep / metric scrapers consistent across modules.
 */
public final class StructuredLog {

    private static final Logger LOG = LoggerFactory.getLogger("streamlit4j");

    private StructuredLog() {}

    /**
     * Logs the completion of a script rerun.
     *
     * @param sessionId owning session id
     * @param seq frame sequence number
     * @param durationMs render duration in milliseconds
     * @param elementCount number of emitted render nodes
     */
    public static void rerun(String sessionId, long seq, long durationMs, int elementCount) {
        LOG.info("event=rerun session={} seq={} duration_ms={} elements={}", sessionId, seq, durationMs, elementCount);
    }

    /**
     * Logs that a new session was created.
     *
     * @param sessionId new session id
     */
    public static void sessionCreated(String sessionId) {
        LOG.info("event=session_created session={}", sessionId);
    }

    /**
     * Logs that a session was explicitly destroyed.
     *
     * @param sessionId destroyed session id
     */
    public static void sessionDestroyed(String sessionId) {
        LOG.info("event=session_destroyed session={}", sessionId);
    }

    /**
     * Logs that a session was evicted due to idle timeout.
     *
     * @param sessionId expired session id
     */
    public static void sessionExpired(String sessionId) {
        LOG.info("event=session_expired session={}", sessionId);
    }

    /**
     * Logs an error event.
     *
     * @param sessionId owning session id
     * @param message short error message
     */
    public static void error(String sessionId, String message) {
        LOG.error("event=error session={} message={}", sessionId, message);
    }
}
