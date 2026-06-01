package io.streamlit4j.core.protocol;

public record ReloadNotice(int v, String type, String sessionId, String reason) implements Envelope {

    public static final String TYPE = "reload";

    public static ReloadNotice of(String sessionId, String reason) {
        return new ReloadNotice(PROTOCOL_VERSION, TYPE, sessionId, reason);
    }
}
