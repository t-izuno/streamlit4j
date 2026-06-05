package io.streamlit4j.core.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable representation of a single rendered element in the render tree.
 *
 * @param kind element type tag (e.g. {@code "title"}, {@code "slider"})
 * @param id stable widget identifier
 * @param props element-specific properties (immutable copy)
 * @param children child nodes (immutable copy)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RenderNode(String kind, String id, Map<String, Object> props, List<RenderNode> children) {

    /**
     * Validates required fields and freezes the props / children collections.
     */
    public RenderNode {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(id, "id");
        props = props == null ? Map.of() : Map.copyOf(props);
        children = children == null ? List.of() : List.copyOf(children);
    }

    /**
     * Builds a {@code root} node wrapping the given top-level children.
     *
     * @param children top-level child nodes
     * @return root node
     */
    public static RenderNode root(List<RenderNode> children) {
        return new RenderNode("root", "root", Map.of(), children);
    }
}
