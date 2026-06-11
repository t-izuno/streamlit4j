package io.streamlit4j.core.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RenderTreeDiffTest {

    @Test
    void nullOldRootProducesRootReplace() {
        RenderNode newRoot = RenderNode.root(List.of(node("title", "w_t", Map.of("text", "Hi"))));

        List<Patch> patches = RenderTreeDiff.diff(null, newRoot);

        assertThat(patches).hasSize(1);
        assertThat(patches.get(0).op()).isEqualTo("replace");
        assertThat(patches.get(0).path()).isEqualTo("/");
    }

    @Test
    void identicalTreesProduceNoPatches() {
        RenderNode root = RenderNode.root(List.of(node("title", "w_t", Map.of("text", "Hi"))));

        assertThat(RenderTreeDiff.diff(root, root)).isEmpty();
    }

    @Test
    void changedChildEmitsReplacePatchAtKeyedPath() {
        RenderNode oldRoot = RenderNode.root(List.of(node("slider", "w_s", Map.of("value", 1))));
        RenderNode newRoot = RenderNode.root(List.of(node("slider", "w_s", Map.of("value", 2))));

        List<Patch> patches = RenderTreeDiff.diff(oldRoot, newRoot);

        assertThat(patches).hasSize(1);
        assertThat(patches.get(0).op()).isEqualTo("replace");
        assertThat(patches.get(0).path()).isEqualTo("main/0");
        assertThat(patches.get(0).node().props()).containsEntry("value", 2);
    }

    @Test
    void newChildProducesInsertPatch() {
        RenderNode oldRoot = RenderNode.root(List.of(node("title", "w_t", Map.of("text", "Hi"))));
        RenderNode newRoot = RenderNode.root(
                List.of(node("title", "w_t", Map.of("text", "Hi")), node("markdown", "w_m", Map.of("body", "x"))));

        List<Patch> patches = RenderTreeDiff.diff(oldRoot, newRoot);

        assertThat(patches).hasSize(1);
        assertThat(patches.get(0).op()).isEqualTo("insert");
        assertThat(patches.get(0).path()).isEqualTo("main/1");
    }

    @Test
    void removedChildProducesRemovePatch() {
        RenderNode oldRoot = RenderNode.root(
                List.of(node("title", "w_t", Map.of("text", "Hi")), node("markdown", "w_m", Map.of("body", "x"))));
        RenderNode newRoot = RenderNode.root(List.of(node("title", "w_t", Map.of("text", "Hi"))));

        List<Patch> patches = RenderTreeDiff.diff(oldRoot, newRoot);

        assertThat(patches).hasSize(1);
        assertThat(patches.get(0).op()).isEqualTo("remove");
        assertThat(patches.get(0).path()).isEqualTo("main/1");
    }

    @Test
    void removesMultipleTrailingChildrenAtFixedTargetIndex() {
        // Regression: when many trailing children are removed, every patch
        // must target the same fixed index so the client's sequential splice
        // doesn't leave later removes pointing at shifted positions.
        RenderNode oldRoot = RenderNode.root(List.of(node("title", "w_a", Map.of()), node("markdown", "w_b", Map.of()),
                node("markdown", "w_c", Map.of()), node("button", "w_d", Map.of())));
        RenderNode newRoot = RenderNode.root(List.of(node("title", "w_a", Map.of())));

        List<Patch> patches = RenderTreeDiff.diff(oldRoot, newRoot);

        assertThat(patches).hasSize(3);
        assertThat(patches).allMatch(p -> p.op().equals("remove") && p.path().equals("main/1"));
    }

    @Test
    void replacesShrinksAndInsertsInOneDiff() {
        RenderNode oldRoot = RenderNode
                .root(List.of(node("title", "w_a", Map.of("text", "A")), node("markdown", "w_b", Map.of("body", "B"))));
        RenderNode newRoot = RenderNode.root(List.of(node("title", "w_a", Map.of("text", "A2")),
                node("markdown", "w_b", Map.of("body", "B")), node("button", "w_c", Map.of("label", "Go"))));

        List<Patch> patches = RenderTreeDiff.diff(oldRoot, newRoot);

        assertThat(patches).hasSize(2);
        assertThat(patches.get(0).op()).isEqualTo("replace");
        assertThat(patches.get(0).path()).isEqualTo("main/0");
        assertThat(patches.get(1).op()).isEqualTo("insert");
        assertThat(patches.get(1).path()).isEqualTo("main/2");
    }

    private static RenderNode node(String kind, String id, Map<String, Object> props) {
        return new RenderNode(kind, id, props, List.of());
    }
}
