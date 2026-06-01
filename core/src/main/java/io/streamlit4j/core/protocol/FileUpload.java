package io.streamlit4j.core.protocol;

public record FileUpload(
        int v, String type, String sessionId, String widgetId, String filename, String mimeType, String contentBase64)
        implements Envelope {

    public static final String TYPE = "file_upload";

    public static FileUpload of(
            String sessionId, String widgetId, String filename, String mimeType, String contentBase64) {
        return new FileUpload(PROTOCOL_VERSION, TYPE, sessionId, widgetId, filename, mimeType, contentBase64);
    }
}
