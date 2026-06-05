package io.streamlit4j.core.protocol;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Client → server envelope carrying a single widget value change.
 *
 * @param v protocol version
 * @param type envelope type discriminator ({@value #TYPE})
 * @param sessionId owning session id
 * @param widgetId emitting widget id
 * @param value new value as a JSON node
 */
public record WidgetEvent(int v, String type, String sessionId, String widgetId, JsonNode value) implements Envelope {

    /** Envelope type discriminator. */
    public static final String TYPE = "widget_event";

    /**
     * Convenience factory that stamps the current protocol version and type tag.
     *
     * @param sessionId owning session id
     * @param widgetId emitting widget id
     * @param value new value
     * @return a populated {@code WidgetEvent}
     */
    public static WidgetEvent of(String sessionId, String widgetId, JsonNode value) {
        return new WidgetEvent(PROTOCOL_VERSION, TYPE, sessionId, widgetId, value);
    }
}
