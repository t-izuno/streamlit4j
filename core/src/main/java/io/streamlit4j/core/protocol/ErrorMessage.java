package io.streamlit4j.core.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorMessage(int v, String type, String sessionId, String message, String stackTrace)
        implements Envelope {

    public static final String TYPE = "error";

    public static ErrorMessage of(String sessionId, String message, String stackTrace) {
        return new ErrorMessage(PROTOCOL_VERSION, TYPE, sessionId, message, stackTrace);
    }
}
