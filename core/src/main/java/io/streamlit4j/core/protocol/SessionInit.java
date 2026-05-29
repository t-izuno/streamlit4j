package io.streamlit4j.core.protocol;

public record SessionInit(int v, String type, String sessionId, RenderNode root) implements Envelope {

    public static final String TYPE = "session_init";

    public static SessionInit of(String sessionId, RenderNode root) {
        return new SessionInit(PROTOCOL_VERSION, TYPE, sessionId, root);
    }
}
