package io.streamlit4j.core.runtime;

import io.streamlit4j.core.port.DownloadStore;

/**
 * Process-wide access point for the active {@link DownloadStore}. Allows host
 * applications (Spring Boot starter, CLI) to swap implementations without
 * changing the {@code St.downloadButton(byte[]...)} API.
 */
public final class DownloadAccess {

    private static volatile DownloadStore store = new InMemoryDownloadStore();

    private DownloadAccess() {}

    /**
     * Returns the active download store.
     *
     * @return current store
     */
    public static DownloadStore store() {
        return store;
    }

    /**
     * Replaces the active download store.
     *
     * @param replacement new store implementation
     */
    public static void use(DownloadStore replacement) {
        store = replacement;
    }
}
