package io.streamlit4j.core.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Patch(String op, String path, RenderNode node) {

    public static Patch replace(String path, RenderNode node) {
        return new Patch("replace", path, node);
    }

    public static Patch insert(String path, RenderNode node) {
        return new Patch("insert", path, node);
    }

    public static Patch remove(String path) {
        return new Patch("remove", path, null);
    }
}
