package io.streamlit4j.core.runtime;

import io.streamlit4j.core.port.UploadValidator;

public final class DefaultUploadValidator implements UploadValidator {

    @Override
    public Result validate(String filename, String mimeType, long sizeBytes, Constraints constraints) {
        if (sizeBytes > constraints.maxSizeBytes()) {
            return Result.reject("file exceeds max size: " + sizeBytes + " > " + constraints.maxSizeBytes());
        }
        if (!constraints.allowedMimeTypes().isEmpty()
                && !constraints.allowedMimeTypes().contains(mimeType)) {
            return Result.reject("mime type not allowed: " + mimeType);
        }
        return Result.accept();
    }
}
