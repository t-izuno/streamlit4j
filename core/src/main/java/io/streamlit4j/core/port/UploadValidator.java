package io.streamlit4j.core.port;

import java.util.Set;

public interface UploadValidator {

    record Constraints(long maxSizeBytes, Set<String> allowedMimeTypes) {

        public Constraints {
            allowedMimeTypes = allowedMimeTypes == null ? Set.of() : Set.copyOf(allowedMimeTypes);
        }
    }

    record Result(boolean accepted, String reason) {

        public static Result accept() {
            return new Result(true, "");
        }

        public static Result reject(String reason) {
            return new Result(false, reason);
        }
    }

    Result validate(String filename, String mimeType, long sizeBytes, Constraints constraints);
}
