package io.streamlit4j.core.protocol;

import java.util.ArrayList;
import java.util.List;

public final class RenderTreeDiff {

    private RenderTreeDiff() {}

    public static List<Patch> diff(RenderNode oldRoot, RenderNode newRoot) {
        if (oldRoot == null) {
            return List.of(Patch.replace("/", newRoot));
        }
        if (oldRoot.equals(newRoot)) {
            return List.of();
        }
        List<RenderNode> oldChildren = oldRoot.children();
        List<RenderNode> newChildren = newRoot.children();
        List<Patch> patches = new ArrayList<>();
        int max = Math.max(oldChildren.size(), newChildren.size());
        for (int i = 0; i < max; i++) {
            String path = "main/" + i;
            if (i >= newChildren.size()) {
                patches.add(Patch.remove(path));
            } else if (i >= oldChildren.size()) {
                patches.add(Patch.insert(path, newChildren.get(i)));
            } else if (!oldChildren.get(i).equals(newChildren.get(i))) {
                patches.add(Patch.replace(path, newChildren.get(i)));
            }
        }
        return patches;
    }
}
