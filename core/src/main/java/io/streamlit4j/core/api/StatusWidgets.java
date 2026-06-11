package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.ordered;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;

import java.util.Map;

/** Status / notification widgets (metric, toast, progress, spinner, status). */
final class StatusWidgets {

    private static final String KIND_METRIC = "metric";

    private StatusWidgets() {
    }

    static void metric(String label, Object value) {
        emit(KIND_METRIC, widgetId(KIND_METRIC, label), ordered("label", label, "value", value));
    }

    static void metric(String label, Object value, Object delta) {
        emit(KIND_METRIC, widgetId(KIND_METRIC, label), ordered("label", label, "value", value, "delta", delta));
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
