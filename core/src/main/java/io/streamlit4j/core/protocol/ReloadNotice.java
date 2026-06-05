package io.streamlit4j.core.protocol;

/**
 * Server → client envelope asking the client to reload (e.g. after source change).
 *
 * @param v protocol version
 * @param type envelope type discriminator ({@value #TYPE})
 * @param sessionId owning session id
 * @param reason free-text reason shown for diagnostics (e.g. {@code "source_change:..."})
 */
public record ReloadNotice(int v, String type, String sessionId, String reason) implements Envelope {

    /** Envelope type discriminator. */
    public static final String TYPE = "reload";

    /**
     * Convenience factory that stamps the current protocol version and type tag.
     *
     * @param sessionId owning session id
     * @param reason diagnostic reason
     * @return a populated {@code ReloadNotice}
     */
    public static ReloadNotice of(String sessionId, String reason) {
        return new ReloadNotice(PROTOCOL_VERSION, TYPE, sessionId, reason);
    }
}
