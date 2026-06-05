package io.streamlit4j.core.protocol;

/**
 * Server → client envelope carrying the initial render tree for a new session.
 *
 * @param v protocol version
 * @param type envelope type discriminator ({@value #TYPE})
 * @param sessionId owning session id
 * @param root initial render-tree root
 */
public record SessionInit(int v, String type, String sessionId, RenderNode root) implements Envelope {

    /** Envelope type discriminator. */
    public static final String TYPE = "session_init";

    /**
     * Convenience factory that stamps the current protocol version and type tag.
     *
     * @param sessionId owning session id
     * @param root initial render-tree root
     * @return a populated {@code SessionInit}
     */
    public static SessionInit of(String sessionId, RenderNode root) {
        return new SessionInit(PROTOCOL_VERSION, TYPE, sessionId, root);
    }
}
