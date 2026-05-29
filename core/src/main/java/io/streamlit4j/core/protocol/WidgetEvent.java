package io.streamlit4j.core.protocol;

import com.fasterxml.jackson.databind.JsonNode;

public record WidgetEvent(int v, String type, String sessionId, String widgetId, JsonNode value) implements Envelope {

    public static final String TYPE = "widget_event";

    public static WidgetEvent of(String sessionId, String widgetId, JsonNode value) {
        return new WidgetEvent(PROTOCOL_VERSION, TYPE, sessionId, widgetId, value);
    }
}
