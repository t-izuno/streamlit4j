package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.ordered;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;

import java.util.Map;

/** Text and document-flow primitives. */
final class TextWidgets {

    private static final String PROP_TEXT = "text";
    private static final String PROP_BODY = "body";
    private static final String KIND_CODE = "code";

    private TextWidgets() {
    }

    static void title(String text) {
        emit("title", widgetId("title", text), Map.of(PROP_TEXT, text));
    }

    static void header(String text) {
        emit("header", widgetId("header", text), Map.of(PROP_TEXT, text));
    }

    static void subheader(String text) {
        emit("subheader", widgetId("subheader", text), Map.of(PROP_TEXT, text));
    }

    static void caption(String text) {
        emit("caption", widgetId("caption", text), Map.of(PROP_TEXT, text));
    }

    static void markdown(String body) {
        emit("markdown", widgetId("markdown", body), Map.of(PROP_BODY, body));
    }

    static void write(Object value) {
        emit("write", widgetId("write", value), Map.of("value", String.valueOf(value)));
    }

    static void code(String body) {
        emit(KIND_CODE, widgetId(KIND_CODE, body), Map.of(PROP_BODY, body));
    }

    static void code(String body, String language) {
        emit(KIND_CODE, widgetId(KIND_CODE, body, language), ordered(PROP_BODY, body, "language", language));
    }

    static void json(String body) {
        emit("json", widgetId("json", body), Map.of(PROP_BODY, body));
    }

    static void latex(String body) {
        emit("latex", widgetId("latex", body), Map.of(PROP_BODY, body));
    }

    static void html(String body) {
        emit("html", widgetId("html", body), Map.of(PROP_BODY, body));
    }

    static void divider() {
        emit("divider", widgetId("divider"), Map.of());
    }
}
