package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;

import java.util.List;
import java.util.Map;

/** Chart widgets (line / bar / area / scatter). */
final class ChartWidgets {

    private static final String PROP_DATA = "data";

    private ChartWidgets() {
    }

    static void lineChart(List<Map<String, Object>> data) {
        emit("line_chart", widgetId("line_chart", data), Map.of(PROP_DATA, data));
    }

    static void barChart(List<Map<String, Object>> data) {
        emit("bar_chart", widgetId("bar_chart", data), Map.of(PROP_DATA, data));
    }

    static void areaChart(List<Map<String, Object>> data) {
        emit("area_chart", widgetId("area_chart", data), Map.of(PROP_DATA, data));
    }

    static void scatterChart(List<Map<String, Object>> data) {
        emit("scatter_chart", widgetId("scatter_chart", data), Map.of(PROP_DATA, data));
    }
}
