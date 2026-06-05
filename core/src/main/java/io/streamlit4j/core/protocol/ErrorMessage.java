package io.streamlit4j.core.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Server → client envelope reporting an error encountered during script execution.
 *
 * @param v protocol version
 * @param type envelope type discriminator ({@value #TYPE})
 * @param sessionId owning session id
 * @param message short error message
 * @param stackTrace optional stack trace text (may be null)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorMessage(int v, String type, String sessionId, String message, String stackTrace)
        implements Envelope {

    /** Envelope type discriminator. */
    public static final String TYPE = "error";

    /**
     * Convenience factory that stamps the current protocol version and type tag.
     *
     * @param sessionId owning session id
     * @param message short error message
     * @param stackTrace optional stack trace text
     * @return a populated {@code ErrorMessage}
     */
    public static ErrorMessage of(String sessionId, String message, String stackTrace) {
        return new ErrorMessage(PROTOCOL_VERSION, TYPE, sessionId, message, stackTrace);
    }
}
