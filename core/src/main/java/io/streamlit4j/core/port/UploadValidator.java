package io.streamlit4j.core.port;

import java.util.Set;

/**
 * Port for validating incoming file uploads against size and MIME type constraints.
 */
public interface UploadValidator {

    /**
     * Validation policy applied to an upload.
     *
     * @param maxSizeBytes
     *            maximum allowed payload size in bytes
     * @param allowedMimeTypes
     *            set of accepted MIME types (empty = any)
     */
    record Constraints(long maxSizeBytes, Set<String> allowedMimeTypes) {

        /**
         * Canonical constructor; normalizes a null {@code allowedMimeTypes} to an empty immutable set.
         *
         * @param maxSizeBytes
         *            maximum allowed payload size in bytes
         * @param allowedMimeTypes
         *            accepted MIME types (null treated as empty)
         */
        public Constraints(long maxSizeBytes, Set<String> allowedMimeTypes) {
            this.maxSizeBytes = maxSizeBytes;
            this.allowedMimeTypes = allowedMimeTypes == null ? Set.of() : Set.copyOf(allowedMimeTypes);
        }
    }

    /**
     * Validation outcome.
     *
     * @param accepted
     *            {@code true} when the upload passed validation
     * @param reason
     *            human-readable reason when {@code accepted=false}; empty otherwise
     */
    record Result(boolean accepted, String reason) {

        /**
         * Returns a positive validation result.
         *
         * @return accepted result with an empty reason
         */
        public static Result accept() {
            return new Result(true, "");
        }

        /**
         * Returns a negative validation result with the given reason.
         *
         * @param reason
         *            rejection reason
         *
         * @return rejected result
         */
        public static Result reject(String reason) {
            return new Result(false, reason);
        }
    }

    /**
     * Validates a single upload candidate.
     *
     * @param filename
     *            file name reported by the client
     * @param mimeType
     *            MIME type reported by the client
     * @param sizeBytes
     *            payload size in bytes
     * @param constraints
     *            validation policy
     *
     * @return acceptance or rejection result
     */
    Result validate(String filename, String mimeType, long sizeBytes, Constraints constraints);
}
