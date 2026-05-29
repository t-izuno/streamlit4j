package io.streamlit4j.core.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RenderNode(String kind, String id, Map<String, Object> props, List<RenderNode> children) {

    public RenderNode {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(id, "id");
        props = props == null ? Map.of() : Map.copyOf(props);
        children = children == null ? List.of() : List.copyOf(children);
    }

    public static RenderNode root(List<RenderNode> children) {
        return new RenderNode("root", "root", Map.of(), children);
    }
}
