package io.streamlit4j.core.protocol;

/**
 * Client → server envelope carrying a single uploaded file as base64.
 *
 * @param v protocol version
 * @param type envelope type discriminator ({@value #TYPE})
 * @param sessionId owning session id
 * @param widgetId source widget id (file uploader)
 * @param filename original filename reported by the client
 * @param mimeType client-reported MIME type
 * @param contentBase64 base64-encoded payload
 */
public record FileUpload(
        int v, String type, String sessionId, String widgetId, String filename, String mimeType, String contentBase64)
        implements Envelope {

    /** Envelope type discriminator. */
    public static final String TYPE = "file_upload";

    /**
     * Convenience factory that stamps the current protocol version and type tag.
     *
     * @param sessionId owning session id
     * @param widgetId source widget id
     * @param filename uploaded filename
     * @param mimeType client-reported MIME type
     * @param contentBase64 base64-encoded payload
     * @return a populated {@code FileUpload}
     */
    public static FileUpload of(
            String sessionId, String widgetId, String filename, String mimeType, String contentBase64) {
        return new FileUpload(PROTOCOL_VERSION, TYPE, sessionId, widgetId, filename, mimeType, contentBase64);
    }
}
