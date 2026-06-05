package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.ordered;
import static io.streamlit4j.core.api.WidgetSupport.readStored;
import static io.streamlit4j.core.api.WidgetSupport.readStoredDouble;
import static io.streamlit4j.core.api.WidgetSupport.readStoredInt;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;

import io.streamlit4j.core.runtime.RenderContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Interactive input widgets (text / number / choice / picker / button). */
final class InputWidgets {

    private static final String PROP_LABEL = "label";
    private static final String PROP_VALUE = "value";
    private static final String PROP_OPTIONS = "options";

    private InputWidgets() {}

    static int slider(String label, int min, int max, int defaultValue) {
        String id = widgetId("slider", label, min, max);
        int value = readStoredInt(id, defaultValue);
        emit("slider", id, ordered(PROP_LABEL, label, "min", min, "max", max, PROP_VALUE, value));
        return value;
    }

    static String textInput(String label, String defaultValue) {
        String id = widgetId("text_input", label);
        String value = readStored(id, String.class, defaultValue);
        emit("text_input", id, ordered(PROP_LABEL, label, PROP_VALUE, value));
        return value;
    }

    static double numberInput(String label, double defaultValue) {
        String id = widgetId("number_input", label);
        double value = readStoredDouble(id, defaultValue);
        emit("number_input", id, ordered(PROP_LABEL, label, PROP_VALUE, value));
        return value;
    }

    static String textArea(String label, String defaultValue) {
        String id = widgetId("text_area", label);
        String value = readStored(id, String.class, defaultValue);
        emit("text_area", id, ordered(PROP_LABEL, label, PROP_VALUE, value));
        return value;
    }

    static String selectbox(String label, List<String> options) {
        String id = widgetId("selectbox", label, options);
        String value = readStored(id, String.class, options.isEmpty() ? null : options.get(0));
        emit("selectbox", id, ordered(PROP_LABEL, label, PROP_OPTIONS, options, PROP_VALUE, value));
        return value;
    }

    @SuppressWarnings("unchecked")
    static List<String> multiselect(String label, List<String> options) {
        String id = widgetId("multiselect", label, options);
        Object stored = RenderContext.current().sessionState().get(id);
        List<String> value = stored instanceof List<?> list ? (List<String>) list : List.of();
        emit("multiselect", id, ordered(PROP_LABEL, label, PROP_OPTIONS, options, PROP_VALUE, value));
        return value;
    }

    static boolean checkbox(String label) {
        return checkbox(label, false);
    }

    static boolean checkbox(String label, boolean defaultValue) {
        String id = widgetId("checkbox", label);
        boolean value = readStored(id, Boolean.class, defaultValue);
        emit("checkbox", id, ordered(PROP_LABEL, label, PROP_VALUE, value));
        return value;
    }

    static String radio(String label, List<String> options) {
        String id = widgetId("radio", label, options);
        String value = readStored(id, String.class, options.isEmpty() ? null : options.get(0));
        emit("radio", id, ordered(PROP_LABEL, label, PROP_OPTIONS, options, PROP_VALUE, value));
        return value;
    }

    static boolean button(String label) {
        String id = widgetId("button", label);
        boolean clicked = readStored(id, Boolean.class, false);
        if (clicked) {
            RenderContext.current().sessionState().put(id, false);
        }
        emit("button", id, ordered(PROP_LABEL, label));
        return clicked;
    }

    static LocalDate dateInput(String label, LocalDate defaultValue) {
        String id = widgetId("date_input", label);
        Object stored = RenderContext.current().sessionState().get(id);
        LocalDate value = stored instanceof String s ? LocalDate.parse(s) : defaultValue;
        emit("date_input", id, ordered(PROP_LABEL, label, PROP_VALUE, value.toString()));
        return value;
    }

    static LocalTime timeInput(String label, LocalTime defaultValue) {
        String id = widgetId("time_input", label);
        Object stored = RenderContext.current().sessionState().get(id);
        LocalTime value = stored instanceof String s ? LocalTime.parse(s) : defaultValue;
        emit("time_input", id, ordered(PROP_LABEL, label, PROP_VALUE, value.toString()));
        return value;
    }

    static String colorPicker(String label, String defaultValue) {
        String id = widgetId("color_picker", label);
        String value = readStored(id, String.class, defaultValue);
        emit("color_picker", id, ordered(PROP_LABEL, label, PROP_VALUE, value));
        return value;
    }

    static String selectSlider(String label, List<String> options, String defaultValue) {
        String id = widgetId("select_slider", label);
        String value = readStored(id, String.class, defaultValue);
        emit("select_slider", id, ordered(PROP_LABEL, label, PROP_OPTIONS, options, PROP_VALUE, value));
        return value;
    }
}
