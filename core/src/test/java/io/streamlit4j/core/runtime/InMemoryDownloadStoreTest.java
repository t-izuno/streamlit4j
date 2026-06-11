package io.streamlit4j.core.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.port.DownloadStore;
import org.junit.jupiter.api.Test;

class InMemoryDownloadStoreTest {

    @Test
    void registerThenRetrieveReturnsAsset() {
        InMemoryDownloadStore store = new InMemoryDownloadStore();
        DownloadStore.Asset asset = new DownloadStore.Asset("a.txt", "text/plain", new byte[] { 1, 2, 3 });

        String key = store.register(asset);
        assertThat(store.retrieve(key)).contains(asset);
    }

    @Test
    void retrieveReturnsEmptyForUnknownKey() {
        InMemoryDownloadStore store = new InMemoryDownloadStore();
        assertThat(store.retrieve("nope")).isEmpty();
    }

    @Test
    void registerProducesDistinctKeys() {
        InMemoryDownloadStore store = new InMemoryDownloadStore();
        DownloadStore.Asset a = new DownloadStore.Asset("a", "text/plain", new byte[0]);
        DownloadStore.Asset b = new DownloadStore.Asset("b", "text/plain", new byte[0]);
        assertThat(store.register(a)).isNotEqualTo(store.register(b));
    }

    @Test
    void assetRecordExposesComponents() {
        byte[] bytes = new byte[] { 9, 8 };
        DownloadStore.Asset asset = new DownloadStore.Asset("file.bin", "application/octet-stream", bytes);
        assertThat(asset.filename()).isEqualTo("file.bin");
        assertThat(asset.contentType()).isEqualTo("application/octet-stream");
        assertThat(asset.bytes()).isSameAs(bytes);
    }
}
