package io.streamlit4j.core.protocol;

import java.util.List;

/**
 * Server → client envelope carrying a render-tree delta as a list of patches.
 *
 * @param v
 *            protocol version
 * @param type
 *            envelope type discriminator ({@value #TYPE})
 * @param sessionId
 *            owning session id
 * @param seq
 *            monotonic frame sequence number
 * @param patches
 *            keyed-diff patches to apply on the client
 */
public record RenderDelta(int v, String type, String sessionId, long seq, List<Patch> patches) implements Envelope {

    /** Envelope type discriminator. */
    public static final String TYPE = "render_delta";

    /**
     * Canonical constructor; freezes the patches list to an immutable copy.
     *
     * @param v
     *            protocol version
     * @param type
     *            envelope type discriminator
     * @param sessionId
     *            owning session id
     * @param seq
     *            monotonic frame sequence number
     * @param patches
     *            keyed-diff patches (null treated as empty)
     */
    public RenderDelta(int v, String type, String sessionId, long seq, List<Patch> patches) {
        this.v = v;
        this.type = type;
        this.sessionId = sessionId;
        this.seq = seq;
        this.patches = patches == null ? List.of() : List.copyOf(patches);
    }

    /**
     * Convenience factory that stamps the current protocol version and type tag.
     *
     * @param sessionId
     *            owning session id
     * @param seq
     *            monotonic frame sequence number
     * @param patches
     *            keyed-diff patches
     *
     * @return a populated {@code RenderDelta}
     */
    public static RenderDelta of(String sessionId, long seq, List<Patch> patches) {
        return new RenderDelta(PROTOCOL_VERSION, TYPE, sessionId, seq, patches);
    }
}
