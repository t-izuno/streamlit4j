package io.streamlit4j.core.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.node.IntNode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CodecTest {

    @Test
    void roundTripsSessionInit() {
        RenderNode root = RenderNode.root(List.of(new RenderNode("title", "w_t", Map.of("text", "Hello"), List.of())));
        SessionInit original = SessionInit.of("s-1", root);

        String json = Codec.encode(original);
        Envelope decoded = Codec.decode(json);

        assertThat(decoded).isInstanceOf(SessionInit.class);
        SessionInit roundTripped = (SessionInit) decoded;
        assertThat(roundTripped.sessionId()).isEqualTo("s-1");
        assertThat(roundTripped.root().children()).hasSize(1);
        assertThat(roundTripped.root().children().get(0).props().get("text")).isEqualTo("Hello");
    }

    @Test
    void roundTripsRenderDelta() {
        Patch patch = Patch.replace("/", RenderNode.root(List.of()));
        RenderDelta original = RenderDelta.of("s-1", 42L, List.of(patch));

        String json = Codec.encode(original);
        Envelope decoded = Codec.decode(json);

        assertThat(decoded).isInstanceOf(RenderDelta.class);
        RenderDelta roundTripped = (RenderDelta) decoded;
        assertThat(roundTripped.seq()).isEqualTo(42L);
        assertThat(roundTripped.patches()).hasSize(1);
        assertThat(roundTripped.patches().get(0).op()).isEqualTo("replace");
    }

    @Test
    void roundTripsWidgetEvent() {
        WidgetEvent original = WidgetEvent.of("s-1", "w_year", IntNode.valueOf(2026));

        String json = Codec.encode(original);
        Envelope decoded = Codec.decode(json);

        assertThat(decoded).isInstanceOf(WidgetEvent.class);
        WidgetEvent roundTripped = (WidgetEvent) decoded;
        assertThat(roundTripped.widgetId()).isEqualTo("w_year");
        assertThat(roundTripped.value().asInt()).isEqualTo(2026);
    }

    @Test
    void rejectsUnknownType() {
        String json = "{\"v\":1,\"type\":\"unknown\",\"sessionId\":\"s-1\"}";
        assertThatThrownBy(() -> Codec.decode(json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown envelope type");
    }

    @Test
    void rejectsMissingType() {
        String json = "{\"v\":1,\"sessionId\":\"s-1\"}";
        assertThatThrownBy(() -> Codec.decode(json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing 'type'");
    }
}
