package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.ordered;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;

import java.util.Map;

/** Status / notification widgets (metric, toast, progress, spinner, status). */
final class StatusWidgets {

    private StatusWidgets() {}

    static void metric(String label, Object value) {
        emit("metric", widgetId("metric", label), ordered("label", label, "value", value));
    }

    static void metric(String label, Object value, Object delta) {
        emit("metric", widgetId("metric", label), ordered("label", label, "value", value, "delta", delta));
    }

    static void toast(String text) {
        emit("toast", widgetId("toast", text), Map.of("text", text));
    }

    static void progress(double value) {
        emit("progress", widgetId("progress"), Map.of("value", value));
    }

    static void spinner(String text) {
        emit("spinner", widgetId("spinner", text), Map.of("text", text));
    }

    static void status(String text) {
        emit("status", widgetId("status", text), Map.of("text", text));
    }
}
