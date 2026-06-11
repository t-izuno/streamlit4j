package io.streamlit4j.core.protocol;

/**
 * Sealed root of all WebSocket protocol messages. Implementing records share a common envelope header (version, type
 * discriminator, session id).
 */
public sealed interface Envelope permits SessionInit, RenderDelta, WidgetEvent, ErrorMessage, FileUpload, ReloadNotice {

    /** Current protocol version. Bumped only on incompatible wire format changes. */
    int PROTOCOL_VERSION = 1;

    /**
     * Returns the protocol version of this envelope.
     *
     * @return protocol version
     */
    int v();

    /**
     * Returns the envelope type discriminator (e.g. {@code "session_init"}).
     *
     * @return type tag
     */
    String type();

    /**
     * Returns the owning session identifier.
     *
     * @return session id
     */
    String sessionId();
}
