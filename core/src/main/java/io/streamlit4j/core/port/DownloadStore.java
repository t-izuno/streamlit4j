package io.streamlit4j.core.port;

import java.util.Optional;

public interface DownloadStore {

    record Asset(String filename, String contentType, byte[] bytes) {}

    String register(Asset asset);

    Optional<Asset> retrieve(String key);
}
