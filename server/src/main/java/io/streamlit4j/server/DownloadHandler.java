package io.streamlit4j.server;

import io.streamlit4j.core.port.DownloadStore;
import java.nio.ByteBuffer;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 * Jetty handler that serves bytes registered in a {@link DownloadStore} under
 * the {@code /download/<key>} URL prefix.
 */
public final class DownloadHandler extends Handler.Abstract {

    private static final String PREFIX = "/download/";

    private final DownloadStore store;

    /**
     * Creates a handler backed by the given store.
     *
     * @param store download store to serve assets from
     */
    public DownloadHandler(DownloadStore store) {
        this.store = store;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        String path = Request.getPathInContext(request);
        if (path == null || !path.startsWith(PREFIX)) {
            return false;
        }
        String key = path.substring(PREFIX.length());
        return store.retrieve(key)
                .map(asset -> {
                    response.setStatus(HttpStatus.OK_200);
                    response.getHeaders().put(HttpHeader.CONTENT_TYPE, asset.contentType());
                    response.getHeaders().put(HttpHeader.CONTENT_LENGTH, asset.bytes().length);
                    response.getHeaders()
                            .put(HttpHeader.CONTENT_DISPOSITION, "attachment; filename=\"" + asset.filename() + "\"");
                    response.write(true, ByteBuffer.wrap(asset.bytes()), callback);
                    return true;
                })
                .orElseGet(() -> {
                    Response.writeError(request, response, callback, HttpStatus.NOT_FOUND_404);
                    return true;
                });
    }
}
