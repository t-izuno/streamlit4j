package io.streamlit4j.core.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ComponentCodecTest {

    record Point(int x, int y) {
    }

    @Test
    void encodeArgWrapsPrimitiveAsJsonNode() {
        JsonNode node = ComponentCodec.encodeArg(42);
        assertThat(node.isInt()).isTrue();
        assertThat(node.asInt()).isEqualTo(42);
    }

    @Test
    void encodeArgWrapsMapAsObjectNode() {
        JsonNode node = ComponentCodec.encodeArg(Map.of("color", "#fff", "size", 12));
        assertThat(node.isObject()).isTrue();
        assertThat(node.get("color").asText()).isEqualTo("#fff");
        assertThat(node.get("size").asInt()).isEqualTo(12);
    }

    @Test
    void encodeArgWrapsRecordAsObjectNode() {
        JsonNode node = ComponentCodec.encodeArg(new Point(3, 4));
        assertThat(node.isObject()).isTrue();
        assertThat(node.get("x").asInt()).isEqualTo(3);
        assertThat(node.get("y").asInt()).isEqualTo(4);
    }

    @Test
    void encodeArgWrapsListAsArrayNode() {
        JsonNode node = ComponentCodec.encodeArg(List.of("a", "b"));
        assertThat(node.isArray()).isTrue();
        assertThat(node).hasSize(2);
        assertThat(node.get(0).asText()).isEqualTo("a");
    }

    @Test
    void decodeReturnConvertsObjectNodeToRecord() {
        JsonNode node = ComponentCodec.encodeArg(new Point(7, 9));
        Point decoded = ComponentCodec.decodeReturn(node, Point.class);
        assertThat(decoded).isEqualTo(new Point(7, 9));
    }

    @Test
    void decodeReturnReturnsNullForNullNode() {
        assertThat(ComponentCodec.decodeReturn(null, String.class)).isNull();
        assertThat(ComponentCodec.decodeReturn(NullNode.getInstance(), String.class)).isNull();
    }

    @Test
    void coerceReturnsInstanceWhenAlreadyCorrectType() {
        String result = ComponentCodec.coerce("already", String.class, "fallback");
        assertThat(result).isEqualTo("already");
    }

    @Test
    void coerceDecodesJsonNodeUsingResultType() {
        JsonNode node = ComponentCodec.encodeArg(new Point(1, 2));
        Point result = ComponentCodec.coerce(node, Point.class, new Point(0, 0));
        assertThat(result).isEqualTo(new Point(1, 2));
    }

    @Test
    void coerceDecodesPrimitiveJsonNode() {
        Integer result = ComponentCodec.coerce(IntNode.valueOf(99), Integer.class, 0);
        assertThat(result).isEqualTo(99);
    }

    @Test
    void coerceReturnsFallbackForNullRaw() {
        String result = ComponentCodec.coerce(null, String.class, "fallback");
        assertThat(result).isEqualTo("fallback");
    }

    @Test
    void coerceFallsBackWhenJsonNodeCannotBeConverted() {
        Point result = ComponentCodec.coerce(IntNode.valueOf(1), Point.class, new Point(-1, -1));
        assertThat(result).isEqualTo(new Point(-1, -1));
    }

    @Test
    void coerceConvertsMapToRecordViaJackson() {
        Point result = ComponentCodec.coerce(Map.of("x", 5, "y", 6), Point.class, new Point(0, 0));
        assertThat(result).isEqualTo(new Point(5, 6));
    }

    @Test
    void coerceFallsBackWhenJsonNodeDecodesToNull() {
        // NullNode → decodeReturn returns null → coerce returns fallback.
        Integer result = ComponentCodec.coerce(NullNode.getInstance(), Integer.class, 42);
        assertThat(result).isEqualTo(42);
    }

    @Test
    void coerceFallsBackForUnconvertibleRaw() {
        Point result = ComponentCodec.coerce("not-a-point", Point.class, new Point(-1, -1));
        assertThat(result).isEqualTo(new Point(-1, -1));
    }
}
