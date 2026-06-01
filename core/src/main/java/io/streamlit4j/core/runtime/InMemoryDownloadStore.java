package io.streamlit4j.core.runtime;

import io.streamlit4j.core.port.DownloadStore;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryDownloadStore implements DownloadStore {

    private final ConcurrentMap<String, Asset> assets = new ConcurrentHashMap<>();

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
