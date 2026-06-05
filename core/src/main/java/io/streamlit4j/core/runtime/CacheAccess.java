package io.streamlit4j.core.runtime;

import io.streamlit4j.core.port.CacheStore;

/**
 * Process-wide access points for the data and resource cache stores.
 * Allows host applications (Spring Boot starter, CLI) to swap implementations
 * without changing the public {@code St.cacheData} / {@code St.cacheResource} API.
 */
public final class CacheAccess {

    private static volatile CacheStore dataCache = new InMemoryCacheStore();
    private static volatile CacheStore resourceCache = new InMemoryCacheStore();

    private CacheAccess() {}

    /**
     * Returns the active data-cache store.
     *
     * @return data cache store
     */
    public static CacheStore dataCache() {
        return dataCache;
    }

    /**
     * Returns the active resource-cache store.
     *
     * @return resource cache store
     */
    public static CacheStore resourceCache() {
        return resourceCache;
    }

    /**
     * Replaces the active data-cache store.
     *
     * @param store new store implementation
     */
    public static void useDataCache(CacheStore store) {
        dataCache = store;
    }

    /**
     * Replaces the active resource-cache store.
     *
     * @param store new store implementation
     */
    public static void useResourceCache(CacheStore store) {
        resourceCache = store;
    }
}
