package io.streamlit4j.core;

import io.streamlit4j.core.domain.CustomComponent;
import io.streamlit4j.core.domain.Page;
import io.streamlit4j.core.domain.SessionState;
import io.streamlit4j.core.port.DownloadStore;
import io.streamlit4j.core.protocol.ComponentCodec;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.ComponentRegistryAccess;
import io.streamlit4j.core.runtime.ControlSignals;
import io.streamlit4j.core.runtime.DownloadAccess;
import io.streamlit4j.core.runtime.RenderContext;
import io.streamlit4j.core.runtime.WidgetIds;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public final class St {

    private St() {}

    public static void rerun() {
        throw new ControlSignals.RerunRequested();
    }

    public static void stop() {
        throw new ControlSignals.StopRequested();
    }

    public static SessionState state() {
        return new SessionState(RenderContext.current().sessionState());
    }

    public static void title(String text) {
        emit("title", widgetId("title", text), Map.of("text", text));
    }

    public static void header(String text) {
        emit("header", widgetId("header", text), Map.of("text", text));
    }

    public static void subheader(String text) {
        emit("subheader", widgetId("subheader", text), Map.of("text", text));
    }

    public static void caption(String text) {
        emit("caption", widgetId("caption", text), Map.of("text", text));
    }

    public static void markdown(String body) {
        emit("markdown", widgetId("markdown", body), Map.of("body", body));
    }

    public static void write(Object value) {
        emit("write", widgetId("write", value), Map.of("value", String.valueOf(value)));
    }

    public static void code(String body) {
        emit("code", widgetId("code", body), Map.of("body", body));
    }

    public static void code(String body, String language) {
        emit("code", widgetId("code", body, language), ordered("body", body, "language", language));
    }

    public static void json(String body) {
        emit("json", widgetId("json", body), Map.of("body", body));
    }

    public static void latex(String body) {
        emit("latex", widgetId("latex", body), Map.of("body", body));
    }

    public static void html(String body) {
        emit("html", widgetId("html", body), Map.of("body", body));
    }

    public static void divider() {
        emit("divider", widgetId("divider"), Map.of());
    }

    public static void metric(String label, Object value) {
        emit("metric", widgetId("metric", label), ordered("label", label, "value", String.valueOf(value)));
    }

    public static void metric(String label, Object value, Object delta) {
        emit(
                "metric",
                widgetId("metric", label, value),
                ordered("label", label, "value", String.valueOf(value), "delta", String.valueOf(delta)));
    }

    public static void dataframe(List<Map<String, Object>> rows) {
        emit("dataframe", widgetId("dataframe", rows.size()), Map.of("rows", rows));
    }

    public static void table(List<Map<String, Object>> rows) {
        emit("table", widgetId("table", rows.size()), Map.of("rows", rows));
    }

    public static void dataEditor(List<Map<String, Object>> rows) {
        emit("data_editor", widgetId("data_editor", rows.size()), Map.of("rows", rows));
    }

    public static void image(String url) {
        emit("image", widgetId("image", url), Map.of("src", url));
    }

    public static void audio(String url) {
        emit("audio", widgetId("audio", url), Map.of("src", url));
    }

    public static void video(String url) {
        emit("video", widgetId("video", url), Map.of("src", url));
    }

    public static void toast(String text) {
        emit("toast", widgetId("toast", text), Map.of("text", text));
    }

    public static void progress(double value) {
        emit("progress", widgetId("progress", value), Map.of("value", value));
    }

    public static void spinner(String text) {
        emit("spinner", widgetId("spinner", text), Map.of("text", text));
    }

    public static void status(String text) {
        emit("status", widgetId("status", text), Map.of("text", text));
    }

    public static void lineChart(List<Map<String, Object>> data) {
        emit("line_chart", widgetId("line_chart"), Map.of("data", data));
    }

    public static void barChart(List<Map<String, Object>> data) {
        emit("bar_chart", widgetId("bar_chart"), Map.of("data", data));
    }

    public static void areaChart(List<Map<String, Object>> data) {
        emit("area_chart", widgetId("area_chart"), Map.of("data", data));
    }

    public static void scatterChart(List<Map<String, Object>> data) {
        emit("scatter_chart", widgetId("scatter_chart"), Map.of("data", data));
    }

    public static int slider(String label, int min, int max, int defaultValue) {
        String id = widgetId("slider", label, min, max);
        int value = readStoredInt(id, defaultValue);
        emit("slider", id, ordered("label", label, "min", min, "max", max, "value", value));
        return value;
    }

    public static String textInput(String label, String defaultValue) {
        String id = widgetId("text_input", label);
        String value = readStored(id, String.class, defaultValue);
        emit("text_input", id, ordered("label", label, "value", value));
        return value;
    }

    public static double numberInput(String label, double defaultValue) {
        String id = widgetId("number_input", label);
        double value = readStoredDouble(id, defaultValue);
        emit("number_input", id, ordered("label", label, "value", value));
        return value;
    }

    public static String textArea(String label, String defaultValue) {
        String id = widgetId("text_area", label);
        String value = readStored(id, String.class, defaultValue);
        emit("text_area", id, ordered("label", label, "value", value));
        return value;
    }

    public static String selectbox(String label, List<String> options) {
        String id = widgetId("selectbox", label, options);
        String value = readStored(id, String.class, options.isEmpty() ? null : options.get(0));
        emit("selectbox", id, ordered("label", label, "options", options, "value", value));
        return value;
    }

    @SuppressWarnings("unchecked")
    public static List<String> multiselect(String label, List<String> options) {
        String id = widgetId("multiselect", label, options);
        Object stored = RenderContext.current().sessionState().get(id);
        List<String> value = stored instanceof List<?> list ? (List<String>) list : List.of();
        emit("multiselect", id, ordered("label", label, "options", options, "value", value));
        return value;
    }

    public static boolean checkbox(String label) {
        return checkbox(label, false);
    }

    public static boolean checkbox(String label, boolean defaultValue) {
        String id = widgetId("checkbox", label);
        boolean value = readStored(id, Boolean.class, defaultValue);
        emit("checkbox", id, ordered("label", label, "value", value));
        return value;
    }

    public static String radio(String label, List<String> options) {
        String id = widgetId("radio", label, options);
        String value = readStored(id, String.class, options.isEmpty() ? null : options.get(0));
        emit("radio", id, ordered("label", label, "options", options, "value", value));
        return value;
    }

    public static boolean button(String label) {
        String id = widgetId("button", label);
        boolean clicked = readStored(id, Boolean.class, false);
        if (clicked) {
            RenderContext.current().sessionState().put(id, false);
        }
        emit("button", id, ordered("label", label));
        return clicked;
    }

    public static LocalDate dateInput(String label, LocalDate defaultValue) {
        String id = widgetId("date_input", label);
        Object stored = RenderContext.current().sessionState().get(id);
        LocalDate value = stored instanceof String s ? LocalDate.parse(s) : defaultValue;
        emit("date_input", id, ordered("label", label, "value", value.toString()));
        return value;
    }

    public static LocalTime timeInput(String label, LocalTime defaultValue) {
        String id = widgetId("time_input", label);
        Object stored = RenderContext.current().sessionState().get(id);
        LocalTime value = stored instanceof String s ? LocalTime.parse(s) : defaultValue;
        emit("time_input", id, ordered("label", label, "value", value.toString()));
        return value;
    }

    public static String colorPicker(String label, String defaultValue) {
        String id = widgetId("color_picker", label);
        String value = readStored(id, String.class, defaultValue);
        emit("color_picker", id, ordered("label", label, "value", value));
        return value;
    }

    public static String selectSlider(String label, List<String> options, String defaultValue) {
        String id = widgetId("select_slider", label);
        String value = readStored(id, String.class, defaultValue);
        emit("select_slider", id, ordered("label", label, "options", options, "value", value));
        return value;
    }

    public static String fileUploader(String label) {
        String id = widgetId("file_uploader", label);
        String value = readStored(id, String.class, "");
        emit("file_uploader", id, ordered("label", label, "value", value));
        return value;
    }

    public static boolean downloadButton(String label, String url) {
        String id = widgetId("download_button", label, url);
        emit("download_button", id, ordered("label", label, "url", url));
        return false;
    }

    public static boolean downloadButton(String label, String filename, byte[] bytes, String contentType) {
        String key = DownloadAccess.store().register(new DownloadStore.Asset(filename, contentType, bytes));
        return downloadButton(label, "/download/" + key);
    }

    public static boolean downloadCsv(String label, String filename, List<Map<String, Object>> rows) {
        return downloadButton(label, filename, toCsvBytes(rows), "text/csv");
    }

    public static boolean downloadJson(String label, String filename, String json) {
        return downloadButton(label, filename, json.getBytes(StandardCharsets.UTF_8), "application/json");
    }

    private static byte[] toCsvBytes(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return new byte[0];
        }
        List<String> columns = new ArrayList<>(rows.get(0).keySet());
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", columns)).append('\n');
        for (Map<String, Object> row : rows) {
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                Object v = row.get(columns.get(i));
                sb.append(csvEscape(String.valueOf(v == null ? "" : v)));
            }
            sb.append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String csvEscape(String s) {
        if (s.indexOf(',') < 0 && s.indexOf('"') < 0 && s.indexOf('\n') < 0) {
            return s;
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    public static void columns(int count, IntConsumer body) {
        RenderContext ctx = RenderContext.current();
        String id = widgetId("columns", count);
        List<RenderNode> columnNodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ctx.pushFrame();
            body.accept(i);
            columnNodes.add(new RenderNode("column", id + "_" + i, Map.of("index", i), ctx.popFrame()));
        }
        ctx.addNode(new RenderNode("columns", id, Map.of("count", count), columnNodes));
    }

    public static void container(Runnable body) {
        wrapContainer("container", widgetId("container"), Map.of(), body);
    }

    public static void expander(String label, Runnable body) {
        wrapContainer("expander", widgetId("expander", label), Map.of("label", label), body);
    }

    public static void tabs(List<String> labels, IntConsumer body) {
        RenderContext ctx = RenderContext.current();
        String id = widgetId("tabs", labels);
        List<RenderNode> tabNodes = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            ctx.pushFrame();
            body.accept(i);
            tabNodes.add(new RenderNode("tab", id + "_" + i, Map.of("label", labels.get(i)), ctx.popFrame()));
        }
        ctx.addNode(new RenderNode("tabs", id, Map.of("labels", labels), tabNodes));
    }

    public static void sidebar(Runnable body) {
        wrapContainer("sidebar", widgetId("sidebar"), Map.of(), body);
    }

    public static void empty() {
        emit("empty", widgetId("empty"), Map.of());
    }

    public static void form(String key, Runnable body) {
        RenderContext ctx = RenderContext.current();
        String id = "k_" + key;
        ctx.pushFrame();
        boolean prev = ctx.isFormSuppressed();
        ctx.setFormSuppressed(true);
        try {
            body.run();
        } finally {
            ctx.setFormSuppressed(prev);
        }
        List<RenderNode> children = ctx.popFrame();
        ctx.addNode(new RenderNode("form", id, Map.of("key", key), children));
    }

    public static boolean formSubmitButton(String label) {
        String id = widgetId("form_submit_button", label);
        boolean clicked = readStored(id, Boolean.class, false);
        if (clicked) {
            RenderContext.current().sessionState().put(id, false);
        }
        emit("form_submit_button", id, ordered("label", label));
        return clicked;
    }

    public static <T> T cacheData(String key, Duration ttl, Supplier<T> loader) {
        return io.streamlit4j.core.runtime.CacheAccess.dataCache().getOrLoad(key, ttl, loader);
    }

    public static <T> T cacheResource(String key, Supplier<T> loader) {
        return io.streamlit4j.core.runtime.CacheAccess.resourceCache().getOrLoad(key, Duration.ofDays(365), loader);
    }

    public static void pages(List<Page> pages) {
        if (pages.isEmpty()) {
            return;
        }
        RenderContext ctx = RenderContext.current();
        Object stored = ctx.sessionState().get("__page__");
        String currentPath = stored instanceof String s ? s : pages.get(0).path();
        Page current = pages.stream()
                .filter(p -> p.path().equals(currentPath))
                .findFirst()
                .orElse(pages.get(0));
        List<Map<String, Object>> pageList = new ArrayList<>();
        for (Page p : pages) {
            pageList.add(Map.of("name", p.name(), "path", p.path()));
        }
        emit("pages", widgetId("pages"), ordered("pages", pageList, "current", current.path()));
        current.body().run();
    }

    private static void wrapContainer(String kind, String id, Map<String, Object> props, Runnable body) {
        RenderContext ctx = RenderContext.current();
        ctx.pushFrame();
        body.run();
        List<RenderNode> children = ctx.popFrame();
        ctx.addNode(new RenderNode(kind, id, props, children));
    }

    /**
     * Registers a custom component as an in-process component — i.e. one whose React
     * renderer is expected to ship in the bundled frontend (see TASK-100). The same
     * {@code spec} can be invoked later via {@link #component(CustomComponent, Map)}.
     * Returns the spec for fluent declarations.
     */
    public static <R> CustomComponent<R> registerComponent(CustomComponent<R> spec) {
        ComponentRegistryAccess.registry().register(spec);
        return spec;
    }

    /**
     * Invokes a custom component declared via {@link CustomComponent}. Returns the value
     * the component yields through widget events, or {@code null} on the first render
     * before the user has interacted. Use the 3-arg overload to supply a default.
     */
    public static <R> R component(CustomComponent<R> spec, Map<String, Object> args) {
        return component(spec, args, null);
    }

    public static <R> R component(CustomComponent<R> spec, Map<String, Object> args, R defaultValue) {
        String id = widgetId("component", spec.name(), args);
        Object stored = RenderContext.current().sessionState().get(id);
        R value = ComponentCodec.coerce(stored, spec.resultType(), defaultValue);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name", spec.name());
        props.put("args", args);
        if (value != null) {
            props.put("value", value);
        }
        emit("component", id, props);
        return value;
    }

    /** Convenience for display-only components that don't yield a value. */
    public static void component(String name, Map<String, Object> args) {
        String id = widgetId("component", name, args);
        emit("component", id, ordered("name", name, "args", args));
    }

    /**
     * Hosts a third-party custom component in a sandboxed iframe at {@code iframeSrc}.
     * The frontend renders the iframe with the streamlit4j sandbox profile and bridges
     * widget events via {@code postMessage}. Returns the value the iframe yields, or
     * the supplied default before any interaction.
     *
     * <p>Origin validation and CSP are layered on top by TASK-102; argument and value
     * boundary checks are added by TASK-103.
     */
    public static <R> R iframeComponent(
            CustomComponent<R> spec, String iframeSrc, Map<String, Object> args, R defaultValue) {
        if (iframeSrc == null || iframeSrc.isBlank()) {
            throw new IllegalArgumentException("iframeSrc must not be blank");
        }
        String id = widgetId("component", spec.name(), iframeSrc, args);
        Object stored = RenderContext.current().sessionState().get(id);
        R value = ComponentCodec.coerce(stored, spec.resultType(), defaultValue);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name", spec.name());
        props.put("iframeSrc", iframeSrc);
        props.put("args", args);
        if (value != null) {
            props.put("value", value);
        }
        emit("component", id, props);
        return value;
    }

    public static <R> R iframeComponent(CustomComponent<R> spec, String iframeSrc, Map<String, Object> args) {
        return iframeComponent(spec, iframeSrc, args, null);
    }

    /** Display-only iframe component variant for components that don't yield a value. */
    public static void iframeComponent(String name, String iframeSrc, Map<String, Object> args) {
        if (iframeSrc == null || iframeSrc.isBlank()) {
            throw new IllegalArgumentException("iframeSrc must not be blank");
        }
        String id = widgetId("component", name, iframeSrc, args);
        emit("component", id, ordered("name", name, "iframeSrc", iframeSrc, "args", args));
    }

    private static void emit(String kind, String id, Map<String, Object> props) {
        RenderContext.current().addNode(new RenderNode(kind, id, props, List.of()));
    }

    private static String widgetId(String kind, Object... discriminator) {
        return WidgetIds.generate(kind, null, discriminator);
    }

    private static <T> T readStored(String id, Class<T> type, T fallback) {
        Object stored = RenderContext.current().sessionState().get(id);
        if (type.isInstance(stored)) {
            return type.cast(stored);
        }
        return fallback;
    }

    private static int readStoredInt(String id, int fallback) {
        Object stored = RenderContext.current().sessionState().get(id);
        if (stored instanceof Number n) {
            return n.intValue();
        }
        return fallback;
    }

    private static double readStoredDouble(String id, double fallback) {
        Object stored = RenderContext.current().sessionState().get(id);
        if (stored instanceof Number n) {
            return n.doubleValue();
        }
        return fallback;
    }

    private static Map<String, Object> ordered(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }
}
