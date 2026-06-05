package io.streamlit4j.core.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Single keyed-diff operation applied to the previous render tree on the client.
 *
 * @param op {@code "replace"} / {@code "insert"} / {@code "remove"}
 * @param path JSON-pointer-like path to the target node
 * @param node replacement / insertion node; {@code null} for {@code remove}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Patch(String op, String path, RenderNode node) {

    /**
     * Creates a {@code replace} patch.
     *
     * @param path target node path
     * @param node replacement node
     * @return replace patch
     */
    public static Patch replace(String path, RenderNode node) {
        return new Patch("replace", path, node);
    }

    /**
     * Creates an {@code insert} patch.
     *
     * @param path insertion target path
     * @param node node to insert
     * @return insert patch
     */
    public static Patch insert(String path, RenderNode node) {
        return new Patch("insert", path, node);
    }

    /**
     * Creates a {@code remove} patch.
     *
     * @param path path of the node to remove
     * @return remove patch
     */
    public static Patch remove(String path) {
        return new Patch("remove", path, null);
    }
}
