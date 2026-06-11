package io.streamlit4j.core.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.IntNode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EnvelopeRecordsTest {

    @Test
    void fileUploadOfStampsVersionAndType() {
        FileUpload e = FileUpload.of("s-1", "w-up", "report.csv", "text/csv", "Zm9v");
        assertThat(e.v()).isEqualTo(Envelope.PROTOCOL_VERSION);
        assertThat(e.type()).isEqualTo(FileUpload.TYPE);
        assertThat(e.sessionId()).isEqualTo("s-1");
        assertThat(e.widgetId()).isEqualTo("w-up");
        assertThat(e.filename()).isEqualTo("report.csv");
        assertThat(e.mimeType()).isEqualTo("text/csv");
        assertThat(e.contentBase64()).isEqualTo("Zm9v");
    }

    @Test
    void errorMessageOfStampsVersionAndType() {
        ErrorMessage e = ErrorMessage.of("s-1", "boom", "trace");
        assertThat(e.v()).isEqualTo(Envelope.PROTOCOL_VERSION);
        assertThat(e.type()).isEqualTo(ErrorMessage.TYPE);
        assertThat(e.sessionId()).isEqualTo("s-1");
        assertThat(e.message()).isEqualTo("boom");
        assertThat(e.stackTrace()).isEqualTo("trace");
    }

    @Test
    void reloadNoticeOfStampsVersionAndType() {
        ReloadNotice e = ReloadNotice.of("s-1", "source_change:foo");
        assertThat(e.v()).isEqualTo(Envelope.PROTOCOL_VERSION);
        assertThat(e.type()).isEqualTo(ReloadNotice.TYPE);
        assertThat(e.sessionId()).isEqualTo("s-1");
        assertThat(e.reason()).isEqualTo("source_change:foo");
    }

    @Test
    void sessionInitOfStampsVersionAndType() {
        RenderNode root = RenderNode.root(List.of());
        SessionInit e = SessionInit.of("s-1", root);
        assertThat(e.v()).isEqualTo(Envelope.PROTOCOL_VERSION);
        assertThat(e.type()).isEqualTo(SessionInit.TYPE);
        assertThat(e.sessionId()).isEqualTo("s-1");
        assertThat(e.root()).isSameAs(root);
    }

    @Test
    void widgetEventOfStampsVersionAndType() {
        IntNode value = IntNode.valueOf(7);
        WidgetEvent e = WidgetEvent.of("s-1", "w-7", value);
        assertThat(e.v()).isEqualTo(Envelope.PROTOCOL_VERSION);
        assertThat(e.type()).isEqualTo(WidgetEvent.TYPE);
        assertThat(e.sessionId()).isEqualTo("s-1");
        assertThat(e.widgetId()).isEqualTo("w-7");
        assertThat(e.value()).isSameAs(value);
    }

    @Test
    void renderDeltaOfStampsVersionAndType() {
        Patch p = Patch.replace("/", RenderNode.root(List.of()));
        RenderDelta e = RenderDelta.of("s-1", 10L, List.of(p));
        assertThat(e.v()).isEqualTo(Envelope.PROTOCOL_VERSION);
        assertThat(e.type()).isEqualTo(RenderDelta.TYPE);
        assertThat(e.seq()).isEqualTo(10L);
        assertThat(e.patches()).hasSize(1);
    }

    @Test
    void renderDeltaConstructorNormalizesNullPatches() {
        RenderDelta e = new RenderDelta(1, RenderDelta.TYPE, "s-1", 1L, null);
        assertThat(e.patches()).isEmpty();
    }

    @Test
    void renderDeltaConstructorFreezesPatches() {
        java.util.ArrayList<Patch> mutable = new java.util.ArrayList<>();
        mutable.add(Patch.remove("main/0"));
        RenderDelta e = new RenderDelta(1, RenderDelta.TYPE, "s-1", 1L, mutable);
        mutable.add(Patch.remove("main/1"));
        assertThat(e.patches()).hasSize(1);
    }

    @Test
    void patchReplaceFactoryProducesCorrectOp() {
        RenderNode node = RenderNode.root(List.of());
        Patch p = Patch.replace("/path", node);
        assertThat(p.op()).isEqualTo("replace");
        assertThat(p.path()).isEqualTo("/path");
        assertThat(p.node()).isSameAs(node);
    }

    @Test
    void patchInsertFactoryProducesCorrectOp() {
        RenderNode node = RenderNode.root(List.of());
        Patch p = Patch.insert("/path", node);
        assertThat(p.op()).isEqualTo("insert");
        assertThat(p.node()).isSameAs(node);
    }

    @Test
    void patchRemoveFactoryProducesNullNode() {
        Patch p = Patch.remove("/path");
        assertThat(p.op()).isEqualTo("remove");
        assertThat(p.path()).isEqualTo("/path");
        assertThat(p.node()).isNull();
    }

    @Test
    void renderNodeNormalizesNullPropsAndChildrenToEmpty() {
        RenderNode node = new RenderNode("k", "i", null, null);
        assertThat(node.props()).isEmpty();
        assertThat(node.children()).isEmpty();
    }

    @Test
    void renderNodeFreezesPropsAndChildren() {
        java.util.HashMap<String, Object> props = new java.util.HashMap<>();
        props.put("a", 1);
        java.util.ArrayList<RenderNode> kids = new java.util.ArrayList<>();
        RenderNode node = new RenderNode("k", "i", props, kids);
        props.put("b", 2);
        kids.add(RenderNode.root(List.of()));
        assertThat(node.props()).hasSize(1);
        assertThat(node.children()).isEmpty();
    }

    @Test
    void renderNodeRequiresNonNullKindAndId() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new RenderNode(null, "i", Map.of(), List.of()))
                .isInstanceOf(NullPointerException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new RenderNode("k", null, Map.of(), List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void renderNodeRootProducesRootKind() {
        RenderNode r = RenderNode.root(List.of());
        assertThat(r.kind()).isEqualTo("root");
        assertThat(r.id()).isEqualTo("root");
    }
}
