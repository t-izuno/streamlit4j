package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.ordered;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;

import java.util.Map;

/** Text and document-flow primitives. */
final class TextWidgets {

    private TextWidgets() {}

    static void title(String text) {
        emit("title", widgetId("title", text), Map.of("text", text));
    }

    static void header(String text) {
        emit("header", widgetId("header", text), Map.of("text", text));
    }

    static void subheader(String text) {
        emit("subheader", widgetId("subheader", text), Map.of("text", text));
    }

    static void caption(String text) {
        emit("caption", widgetId("caption", text), Map.of("text", text));
    }

    static void markdown(String body) {
        emit("markdown", widgetId("markdown", body), Map.of("body", body));
    }

    static void write(Object value) {
        emit("write", widgetId("write", value), Map.of("value", String.valueOf(value)));
    }

    static void code(String body) {
        emit("code", widgetId("code", body), Map.of("body", body));
    }

    static void code(String body, String language) {
        emit("code", widgetId("code", body, language), ordered("body", body, "language", language));
    }

    static void json(String body) {
        emit("json", widgetId("json", body), Map.of("body", body));
    }

    static void latex(String body) {
        emit("latex", widgetId("latex", body), Map.of("body", body));
    }

    static void html(String body) {
        emit("html", widgetId("html", body), Map.of("body", body));
    }

    static void divider() {
        emit("divider", widgetId("divider"), Map.of());
    }
}
