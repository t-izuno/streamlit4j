package io.streamlit4j.core.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON wire-format codec for protocol {@link Envelope}s. Wraps a shared
 * {@link ObjectMapper} so all encoding/decoding sites use one schema.
 */
public final class Codec {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Codec() {}

    /**
     * Encodes the envelope to its JSON wire form.
     *
     * @param envelope envelope to encode
     * @return JSON string suitable for WebSocket text frames
     */
    public static String encode(Envelope envelope) {
        try {
            return MAPPER.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to encode envelope", e);
        }
    }

    /**
     * Decodes a JSON string into the appropriate {@link Envelope} subtype based
     * on the {@code "type"} field.
     *
     * @param json JSON wire frame
     * @return decoded envelope
     */
    public static Envelope decode(String json) {
        try {
            JsonNode node = MAPPER.readTree(json);
            JsonNode typeNode = node.get("type");
            if (typeNode == null) {
                throw new IllegalArgumentException("Missing 'type' field");
            }
            String type = typeNode.asText();
            return switch (type) {
                case SessionInit.TYPE -> MAPPER.treeToValue(node, SessionInit.class);
                case RenderDelta.TYPE -> MAPPER.treeToValue(node, RenderDelta.class);
                case WidgetEvent.TYPE -> MAPPER.treeToValue(node, WidgetEvent.class);
                case ErrorMessage.TYPE -> MAPPER.treeToValue(node, ErrorMessage.class);
                case FileUpload.TYPE -> MAPPER.treeToValue(node, FileUpload.class);
                case ReloadNotice.TYPE -> MAPPER.treeToValue(node, ReloadNotice.class);
                default -> throw new IllegalArgumentException("Unknown envelope type: " + type);
            };
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to decode envelope", e);
        }
    }

    /**
     * Returns the shared {@link ObjectMapper} so callers can opt into the same
     * serialization configuration (mainly {@link ComponentCodec}).
     *
     * @return shared mapper instance
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
