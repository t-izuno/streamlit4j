package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;

import java.util.List;
import java.util.Map;

/** Tabular data display widgets (dataframe, table, data editor). */
final class DataWidgets {

    private DataWidgets() {
    }

    static void dataframe(List<Map<String, Object>> rows) {
        emit("dataframe", widgetId("dataframe", rows), Map.of("rows", rows));
    }

    static void table(List<Map<String, Object>> rows) {
        emit("table", widgetId("table", rows), Map.of("rows", rows));
    }

    static void dataEditor(List<Map<String, Object>> rows) {
        emit("data_editor", widgetId("data_editor", rows), Map.of("rows", rows));
    }
}
