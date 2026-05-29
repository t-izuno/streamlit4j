package io.streamlit4j.core.protocol;

import java.util.List;

public record RenderDelta(int v, String type, String sessionId, long seq, List<Patch> patches) implements Envelope {

    public static final String TYPE = "render_delta";

    public RenderDelta {
        patches = patches == null ? List.of() : List.copyOf(patches);
    }

    public static RenderDelta of(String sessionId, long seq, List<Patch> patches) {
        return new RenderDelta(PROTOCOL_VERSION, TYPE, sessionId, seq, patches);
    }
}
