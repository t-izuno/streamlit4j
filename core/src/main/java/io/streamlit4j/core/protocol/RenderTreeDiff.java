package io.streamlit4j.core.protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes the minimal {@link Patch} list to transform one render tree into another.
 */
public final class RenderTreeDiff {

    private RenderTreeDiff() {
    }

    /**
     * Diffs two render trees and returns the patch list to apply on the client.
     *
     * @param oldRoot
     *            previous render root (may be {@code null} for the first frame)
     * @param newRoot
     *            new render root
     *
     * @return ordered patch list
     */
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
        int common = Math.min(oldChildren.size(), newChildren.size());

        // 1. Replace overlapping indices that changed.
        for (int i = 0; i < common; i++) {
            if (!oldChildren.get(i).equals(newChildren.get(i))) {
                patches.add(Patch.replace("main/" + i, newChildren.get(i)));
            }
        }

        // 2. Remove trailing old children. Always remove at the same fixed
        // index (newChildren.size()) because each remove shifts later
        // elements down on the client side.
        for (int i = oldChildren.size(); i > newChildren.size(); i--) {
            patches.add(Patch.remove("main/" + newChildren.size()));
        }

        // 3. Append new trailing children.
        for (int i = oldChildren.size(); i < newChildren.size(); i++) {
            patches.add(Patch.insert("main/" + i, newChildren.get(i)));
        }

        return patches;
    }
}
