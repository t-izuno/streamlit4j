package io.streamlit4j.core.port;

import java.util.Optional;

/**
 * Port that stores download payloads keyed by an opaque token. Used by
 * {@code St.downloadButton(byte[]...)} to expose downloadable bytes to the
 * frontend via a stable URL.
 */
public interface DownloadStore {

    /**
     * Single downloadable asset.
     *
     * @param filename suggested filename when the client saves the asset
     * @param contentType MIME type for the response
     * @param bytes raw payload to serve
     */
    record Asset(String filename, String contentType, byte[] bytes) {}

    /**
     * Stores the asset and returns the opaque key the frontend uses to fetch it.
     *
     * @param asset asset to register
     * @return key suitable for embedding in a download URL
     */
    String register(Asset asset);

    /**
     * Looks up an asset previously registered via {@link #register(Asset)}.
     *
     * @param key opaque key
     * @return present optional when the asset is still available
     */
    Optional<Asset> retrieve(String key);
}
