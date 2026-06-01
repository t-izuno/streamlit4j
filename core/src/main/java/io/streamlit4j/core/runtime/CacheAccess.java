package io.streamlit4j.core.runtime;

import io.streamlit4j.core.port.CacheStore;

public final class CacheAccess {

    private static volatile CacheStore dataCache = new InMemoryCacheStore();
    private static volatile CacheStore resourceCache = new InMemoryCacheStore();

    private CacheAccess() {}

    public static CacheStore dataCache() {
        return dataCache;
    }

    public static CacheStore resourceCache() {
        return resourceCache;
    }

    public static void useDataCache(CacheStore store) {
        dataCache = store;
    }

    public static void useResourceCache(CacheStore store) {
        resourceCache = store;
    }
}
