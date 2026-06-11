package io.streamlit4j.core.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.port.CacheStore;
import io.streamlit4j.core.port.ComponentRegistry;
import io.streamlit4j.core.port.DownloadStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AccessSingletonsTest {

    private final CacheStore originalDataCache = CacheAccess.dataCache();
    private final CacheStore originalResourceCache = CacheAccess.resourceCache();
    private final DownloadStore originalDownload = DownloadAccess.store();
    private final ComponentRegistry originalRegistry = ComponentRegistryAccess.registry();

    @AfterEach
    void restore() {
        CacheAccess.useDataCache(originalDataCache);
        CacheAccess.useResourceCache(originalResourceCache);
        DownloadAccess.use(originalDownload);
        ComponentRegistryAccess.use(originalRegistry);
    }

    @Test
    void cacheAccessDefaultsAreInMemoryStores() {
        assertThat(CacheAccess.dataCache()).isInstanceOf(InMemoryCacheStore.class);
        assertThat(CacheAccess.resourceCache()).isInstanceOf(InMemoryCacheStore.class);
    }

    @Test
    void cacheAccessCanSwapDataAndResourceStoresIndependently() {
        CacheStore data = new InMemoryCacheStore();
        CacheStore resource = new InMemoryCacheStore();
        CacheAccess.useDataCache(data);
        CacheAccess.useResourceCache(resource);
        assertThat(CacheAccess.dataCache()).isSameAs(data);
        assertThat(CacheAccess.resourceCache()).isSameAs(resource);
    }

    @Test
    void downloadAccessDefaultsToInMemoryStore() {
        assertThat(DownloadAccess.store()).isInstanceOf(InMemoryDownloadStore.class);
    }

    @Test
    void downloadAccessCanReplaceStore() {
        DownloadStore replacement = new InMemoryDownloadStore();
        DownloadAccess.use(replacement);
        assertThat(DownloadAccess.store()).isSameAs(replacement);
    }

    @Test
    void componentRegistryAccessDefaultsToInMemoryRegistry() {
        assertThat(ComponentRegistryAccess.registry()).isInstanceOf(InMemoryComponentRegistry.class);
    }

    @Test
    void componentRegistryAccessCanReplaceRegistry() {
        ComponentRegistry replacement = new InMemoryComponentRegistry();
        ComponentRegistryAccess.use(replacement);
        assertThat(ComponentRegistryAccess.registry()).isSameAs(replacement);
    }
}
