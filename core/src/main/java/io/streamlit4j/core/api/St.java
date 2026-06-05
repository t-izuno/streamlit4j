package io.streamlit4j.core.api;

import io.streamlit4j.core.domain.CustomComponent;
import io.streamlit4j.core.domain.Page;
import io.streamlit4j.core.domain.SessionState;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Public facade for the streamlit4j script-side API. Every entry point is a
 * thin delegation to a package-private widget category class (TextWidgets,
 * InputWidgets, LayoutWidgets, etc.) that lives alongside this class.
 *
 * <p>The facade preserves Streamlit Python's flat surface (`st.title(...)` →
 * {@code St.title(...)}) while keeping each category of widgets in its own
 * source file for readability and maintainability.
 */
public final class St {

    private St() {}

    // ----- Control flow ------------------------------------------------------

    /** Aborts the current script and schedules an immediate re-run. */
    public static void rerun() {
        ControlOps.rerun();
    }

    /** Stops the current script without scheduling a re-run. */
    public static void stop() {
        ControlOps.stop();
    }

    /** Returns the per-session key/value state accessor. */
    public static SessionState state() {
        return ControlOps.state();
    }

    // ----- Text / document flow ---------------------------------------------

    public static void title(String text) {
        TextWidgets.title(text);
    }

    public static void header(String text) {
        TextWidgets.header(text);
    }

    public static void subheader(String text) {
        TextWidgets.subheader(text);
    }

    public static void caption(String text) {
        TextWidgets.caption(text);
    }

    public static void markdown(String body) {
        TextWidgets.markdown(body);
    }

    public static void write(Object value) {
        TextWidgets.write(value);
    }

    public static void code(String body) {
        TextWidgets.code(body);
    }

    public static void code(String body, String language) {
        TextWidgets.code(body, language);
    }

    public static void json(String body) {
        TextWidgets.json(body);
    }

    public static void latex(String body) {
        TextWidgets.latex(body);
    }

    public static void html(String body) {
        TextWidgets.html(body);
    }

    public static void divider() {
        TextWidgets.divider();
    }

    // ----- Status / notification --------------------------------------------

    public static void metric(String label, Object value) {
        StatusWidgets.metric(label, value);
    }

    public static void metric(String label, Object value, Object delta) {
        StatusWidgets.metric(label, value, delta);
    }

    public static void toast(String text) {
        StatusWidgets.toast(text);
    }

    public static void progress(double value) {
        StatusWidgets.progress(value);
    }

    public static void spinner(String text) {
        StatusWidgets.spinner(text);
    }

    public static void status(String text) {
        StatusWidgets.status(text);
    }

    // ----- Data display ------------------------------------------------------

    public static void dataframe(List<Map<String, Object>> rows) {
        DataWidgets.dataframe(rows);
    }

    public static void table(List<Map<String, Object>> rows) {
        DataWidgets.table(rows);
    }

    public static void dataEditor(List<Map<String, Object>> rows) {
        DataWidgets.dataEditor(rows);
    }

    // ----- Media -------------------------------------------------------------

    public static void image(String url) {
        MediaWidgets.image(url);
    }

    public static void audio(String url) {
        MediaWidgets.audio(url);
    }

    public static void video(String url) {
        MediaWidgets.video(url);
    }

    // ----- Charts ------------------------------------------------------------

    public static void lineChart(List<Map<String, Object>> data) {
        ChartWidgets.lineChart(data);
    }

    public static void barChart(List<Map<String, Object>> data) {
        ChartWidgets.barChart(data);
    }

    public static void areaChart(List<Map<String, Object>> data) {
        ChartWidgets.areaChart(data);
    }

    public static void scatterChart(List<Map<String, Object>> data) {
        ChartWidgets.scatterChart(data);
    }

    // ----- Input widgets -----------------------------------------------------

    public static int slider(String label, int min, int max, int defaultValue) {
        return InputWidgets.slider(label, min, max, defaultValue);
    }

    public static String textInput(String label, String defaultValue) {
        return InputWidgets.textInput(label, defaultValue);
    }

    public static double numberInput(String label, double defaultValue) {
        return InputWidgets.numberInput(label, defaultValue);
    }

    public static String textArea(String label, String defaultValue) {
        return InputWidgets.textArea(label, defaultValue);
    }

    public static String selectbox(String label, List<String> options) {
        return InputWidgets.selectbox(label, options);
    }

    public static List<String> multiselect(String label, List<String> options) {
        return InputWidgets.multiselect(label, options);
    }

    public static boolean checkbox(String label) {
        return InputWidgets.checkbox(label);
    }

    public static boolean checkbox(String label, boolean defaultValue) {
        return InputWidgets.checkbox(label, defaultValue);
    }

    public static String radio(String label, List<String> options) {
        return InputWidgets.radio(label, options);
    }

    public static boolean button(String label) {
        return InputWidgets.button(label);
    }

    public static LocalDate dateInput(String label, LocalDate defaultValue) {
        return InputWidgets.dateInput(label, defaultValue);
    }

    public static LocalTime timeInput(String label, LocalTime defaultValue) {
        return InputWidgets.timeInput(label, defaultValue);
    }

    public static String colorPicker(String label, String defaultValue) {
        return InputWidgets.colorPicker(label, defaultValue);
    }

    public static String selectSlider(String label, List<String> options, String defaultValue) {
        return InputWidgets.selectSlider(label, options, defaultValue);
    }

    // ----- File upload / download -------------------------------------------

    public static String fileUploader(String label) {
        return FileWidgets.fileUploader(label);
    }

    public static boolean downloadButton(String label, String url) {
        return FileWidgets.downloadButton(label, url);
    }

    public static boolean downloadButton(String label, String filename, byte[] bytes, String contentType) {
        return FileWidgets.downloadButton(label, filename, bytes, contentType);
    }

    public static boolean downloadCsv(String label, String filename, List<Map<String, Object>> rows) {
        return FileWidgets.downloadCsv(label, filename, rows);
    }

    public static boolean downloadJson(String label, String filename, String json) {
        return FileWidgets.downloadJson(label, filename, json);
    }

    // ----- Layout ------------------------------------------------------------

    public static void columns(int count, IntConsumer body) {
        LayoutWidgets.columns(count, body);
    }

    public static void container(Runnable body) {
        LayoutWidgets.container(body);
    }

    public static void expander(String label, Runnable body) {
        LayoutWidgets.expander(label, body);
    }

    public static void tabs(List<String> labels, IntConsumer body) {
        LayoutWidgets.tabs(labels, body);
    }

    public static void sidebar(Runnable body) {
        LayoutWidgets.sidebar(body);
    }

    public static void empty() {
        LayoutWidgets.empty();
    }

    // ----- Forms -------------------------------------------------------------

    public static void form(String key, Runnable body) {
        FormWidgets.form(key, body);
    }

    public static boolean formSubmitButton(String label) {
        return FormWidgets.formSubmitButton(label);
    }

    // ----- Caching -----------------------------------------------------------

    public static <T> T cacheData(String key, Duration ttl, Supplier<T> loader) {
        return CacheOps.cacheData(key, ttl, loader);
    }

    public static <T> T cacheResource(String key, Supplier<T> loader) {
        return CacheOps.cacheResource(key, loader);
    }

    // ----- Multi-page --------------------------------------------------------

    public static void pages(List<Page> pages) {
        PageNav.pages(pages);
    }

    // ----- Custom components -------------------------------------------------

    public static <R> CustomComponent<R> registerComponent(CustomComponent<R> spec) {
        return ComponentApi.registerComponent(spec);
    }

    public static <R> R component(CustomComponent<R> spec, Map<String, Object> args) {
        return ComponentApi.component(spec, args, null);
    }

    public static <R> R component(CustomComponent<R> spec, Map<String, Object> args, R defaultValue) {
        return ComponentApi.component(spec, args, defaultValue);
    }

    public static void component(String name, Map<String, Object> args) {
        ComponentApi.component(name, args);
    }
}
