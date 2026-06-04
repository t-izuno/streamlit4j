package io.streamlit4j.core.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Serializes custom component arguments and return values across the WebSocket
 * boundary. Arguments flow Java → JSON (server → client); return values flow
 * JSON → Java (client → server) using the type declared by
 * {@code CustomComponent.resultType()}.
 *
 * <p>This is the canonical bridge between {@code St.component(...)} (domain-level
 * typed API) and the wire format defined by {@link Codec}. It deliberately reuses
 * the shared protocol {@link com.fasterxml.jackson.databind.ObjectMapper} so that
 * encoding inside render_delta and decoding inside widget_event share one schema.
 */
public final class ComponentCodec {

    private ComponentCodec() {}

    /**
     * Encodes a component argument value to its JSON tree representation. Supports
     * primitives, {@code Map}/{@code List}, records, and any Jackson-bindable POJO.
     */
    public static JsonNode encodeArg(Object value) {
        return Codec.mapper().valueToTree(value);
    }

    /**
     * Decodes a JSON tree node into the declared result type. Returns {@code null}
     * when the node is missing or JSON-null.
     */
    public static <R> R decodeReturn(JsonNode node, Class<R> resultType) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return Codec.mapper().treeToValue(node, resultType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to decode component value as " + resultType.getName(), e);
        }
    }

    /**
     * Coerces a raw stored value (Java instance, {@link JsonNode}, or any
     * Jackson-convertible object) into the declared result type. Returns the
     * supplied fallback when the value is null or cannot be converted.
     */
    public static <R> R coerce(Object raw, Class<R> resultType, R fallback) {
        if (raw == null) {
            return fallback;
        }
        if (resultType.isInstance(raw)) {
            return resultType.cast(raw);
        }
        if (raw instanceof JsonNode node) {
            try {
                R decoded = decodeReturn(node, resultType);
                return decoded == null ? fallback : decoded;
            } catch (IllegalArgumentException e) {
                return fallback;
            }
        }
        try {
            return Codec.mapper().convertValue(raw, resultType);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
