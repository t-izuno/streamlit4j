package io.streamlit4j.core.runtime;

import io.streamlit4j.core.port.DownloadStore;

public final class DownloadAccess {

    private static volatile DownloadStore store = new InMemoryDownloadStore();

    private DownloadAccess() {}

    public static DownloadStore store() {
        return store;
    }

    public static void use(DownloadStore replacement) {
        store = replacement;
    }
}
