package io.streamlit4j.core.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class Codec {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Codec() {}

    public static String encode(Envelope envelope) {
        try {
            return MAPPER.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to encode envelope", e);
        }
    }

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
                default -> throw new IllegalArgumentException("Unknown envelope type: " + type);
            };
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to decode envelope", e);
        }
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
