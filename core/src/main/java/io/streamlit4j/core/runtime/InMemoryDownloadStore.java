package io.streamlit4j.core.runtime;

import io.streamlit4j.core.port.DownloadStore;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory {@link DownloadStore} backed by a {@link ConcurrentMap}. Suitable
 * for single-process deployments. Assets persist until the process exits.
 */
public final class InMemoryDownloadStore implements DownloadStore {

    private final ConcurrentMap<String, Asset> assets = new ConcurrentHashMap<>();

    /** Creates an empty store. */
    public InMemoryDownloadStore() {}

    @Override
    public String register(Asset asset) {
        String key = UUID.randomUUID().toString();
        assets.put(key, asset);
        return key;
    }

    @Override
    public Optional<Asset> retrieve(String key) {
        return Optional.ofNullable(assets.get(key));
    }
}
