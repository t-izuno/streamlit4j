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
 * Public facade for the streamlit4j script-side API. Every entry point is a thin delegation to a package-private widget
 * category class (TextWidgets, InputWidgets, LayoutWidgets, etc.) that lives alongside this class.
 * <p>
 * The facade preserves Streamlit Python's flat surface (`st.title(...)` → {@code St.title(...)}) while keeping each
 * category of widgets in its own source file for readability and maintainability.
 */
public final class St {

    private St() {
    }

    // ----- Control flow ------------------------------------------------------

    /** Aborts the current script and schedules an immediate re-run. */
    public static void rerun() {
        ControlOps.rerun();
    }

    /** Stops the current script without scheduling a re-run. */
    public static void stop() {
        ControlOps.stop();
    }

    /**
     * Returns the per-session key/value state accessor.
     *
     * @return the active session state for the current render
     */
    public static SessionState state() {
        return ControlOps.state();
    }

    // ----- Text / document flow ---------------------------------------------

    /**
     * Renders an h1-level title.
     *
     * @param text
     *            title text to display
     */
    public static void title(String text) {
        TextWidgets.title(text);
    }

    /**
     * Renders an h2-level section header.
     *
     * @param text
     *            header text to display
     */
    public static void header(String text) {
        TextWidgets.header(text);
    }

    /**
     * Renders an h3-level subheader.
     *
     * @param text
     *            subheader text to display
     */
    public static void subheader(String text) {
        TextWidgets.subheader(text);
    }

    /**
     * Renders muted small-print text below the previous element.
     *
     * @param text
     *            caption text to display
     */
    public static void caption(String text) {
        TextWidgets.caption(text);
    }

    /**
     * Renders Markdown-formatted prose; sanitized in the frontend.
     *
     * @param body
     *            Markdown source to render
     */
    public static void markdown(String body) {
        TextWidgets.markdown(body);
    }

    /**
     * Renders the {@code toString} of {@code value} as plain text.
     *
     * @param value
     *            value to stringify and display
     */
    public static void write(Object value) {
        TextWidgets.write(value);
    }

    /**
     * Renders a fenced code block without explicit language hinting.
     *
     * @param body
     *            source code to display
     */
    public static void code(String body) {
        TextWidgets.code(body);
    }

    /**
     * Renders a fenced code block with a syntax-highlight language hint.
     *
     * @param body
     *            source code to display
     * @param language
     *            syntax-highlight language tag (e.g. {@code "java"})
     */
    public static void code(String body, String language) {
        TextWidgets.code(body, language);
    }

    /**
     * Renders a JSON literal in a code-style block.
     *
     * @param body
     *            JSON text to display
     */
    public static void json(String body) {
        TextWidgets.json(body);
    }

    /**
     * Renders a LaTeX expression (frontend MathJax/KaTeX rendering).
     *
     * @param body
     *            LaTeX source
     */
    public static void latex(String body) {
        TextWidgets.latex(body);
    }

    /**
     * Renders raw HTML; sanitized via DOMPurify on the frontend.
     *
     * @param body
     *            HTML source to render
     */
    public static void html(String body) {
        TextWidgets.html(body);
    }

    /** Renders a horizontal rule. */
    public static void divider() {
        TextWidgets.divider();
    }

    // ----- Status / notification --------------------------------------------

    /**
     * Renders a labeled metric card with a value.
     *
     * @param label
     *            metric label
     * @param value
     *            metric value (stringified)
     */
    public static void metric(String label, Object value) {
        StatusWidgets.metric(label, value);
    }

    /**
     * Renders a labeled metric card with a value and delta indicator.
     *
     * @param label
     *            metric label
     * @param value
     *            metric value
     * @param delta
     *            delta indicator value (e.g. "+3%")
     */
    public static void metric(String label, Object value, Object delta) {
        StatusWidgets.metric(label, value, delta);
    }

    /**
     * Shows a transient toast notification.
     *
     * @param text
     *            message body
     */
    public static void toast(String text) {
        StatusWidgets.toast(text);
    }

    /**
     * Renders a progress bar; {@code value} is in [0.0, 1.0].
     *
     * @param value
     *            progress fraction
     */
    public static void progress(double value) {
        StatusWidgets.progress(value);
    }

    /**
     * Renders an indeterminate spinner with a caption.
     *
     * @param text
     *            caption shown next to the spinner
     */
    public static void spinner(String text) {
        StatusWidgets.spinner(text);
    }

    /**
     * Renders a status banner (info / running).
     *
     * @param text
     *            status text
     */
    public static void status(String text) {
        StatusWidgets.status(text);
    }

    // ----- Data display ------------------------------------------------------

    /**
     * Renders rows as an interactive dataframe-style table.
     *
     * @param rows
     *            row data; each map represents one row with column → value
     */
    public static void dataframe(List<Map<String, Object>> rows) {
        DataWidgets.dataframe(rows);
    }

    /**
     * Renders rows as a static HTML table.
     *
     * @param rows
     *            row data
     */
    public static void table(List<Map<String, Object>> rows) {
        DataWidgets.table(rows);
    }

    /**
     * Renders an editable data grid (round-trip changes via session state).
     *
     * @param rows
     *            initial row data
     */
    public static void dataEditor(List<Map<String, Object>> rows) {
        DataWidgets.dataEditor(rows);
    }

    // ----- Media -------------------------------------------------------------

    /**
     * Renders an image referenced by URL or download key.
     *
     * @param url
     *            image URL
     */
    public static void image(String url) {
        MediaWidgets.image(url);
    }

    /**
     * Renders an audio player.
     *
     * @param url
     *            audio source URL
     */
    public static void audio(String url) {
        MediaWidgets.audio(url);
    }

    /**
     * Renders a video player.
     *
     * @param url
     *            video source URL
     */
    public static void video(String url) {
        MediaWidgets.video(url);
    }

    // ----- Charts ------------------------------------------------------------

    /**
     * Renders a line chart from row data.
     *
     * @param data
     *            plotted rows
     */
    public static void lineChart(List<Map<String, Object>> data) {
        ChartWidgets.lineChart(data);
    }

    /**
     * Renders a bar chart from row data.
     *
     * @param data
     *            plotted rows
     */
    public static void barChart(List<Map<String, Object>> data) {
        ChartWidgets.barChart(data);
    }

    /**
     * Renders a filled area chart from row data.
     *
     * @param data
     *            plotted rows
     */
    public static void areaChart(List<Map<String, Object>> data) {
        ChartWidgets.areaChart(data);
    }

    /**
     * Renders a scatter chart from row data.
     *
     * @param data
     *            plotted rows
     */
    public static void scatterChart(List<Map<String, Object>> data) {
        ChartWidgets.scatterChart(data);
    }

    // ----- Input widgets -----------------------------------------------------

    /**
     * Integer slider.
     *
     * @param label
     *            widget label
     * @param min
     *            minimum value (inclusive)
     * @param max
     *            maximum value (inclusive)
     * @param defaultValue
     *            value before user interaction
     *
     * @return current value
     */
    public static int slider(String label, int min, int max, int defaultValue) {
        return InputWidgets.slider(label, min, max, defaultValue);
    }

    /**
     * Single-line text input.
     *
     * @param label
     *            widget label
     * @param defaultValue
     *            initial value
     *
     * @return current value
     */
    public static String textInput(String label, String defaultValue) {
        return InputWidgets.textInput(label, defaultValue);
    }

    /**
     * Numeric input returning {@code double}.
     *
     * @param label
     *            widget label
     * @param defaultValue
     *            initial value
     *
     * @return current value
     */
    public static double numberInput(String label, double defaultValue) {
        return InputWidgets.numberInput(label, defaultValue);
    }

    /**
     * Multi-line text input.
     *
     * @param label
     *            widget label
     * @param defaultValue
     *            initial value
     *
     * @return current value
     */
    public static String textArea(String label, String defaultValue) {
        return InputWidgets.textArea(label, defaultValue);
    }

    /**
     * Drop-down select.
     *
     * @param label
     *            widget label
     * @param options
     *            selectable options (defaults to the first)
     *
     * @return chosen option
     */
    public static String selectbox(String label, List<String> options) {
        return InputWidgets.selectbox(label, options);
    }

    /**
     * Multi-select chip input.
     *
     * @param label
     *            widget label
     * @param options
     *            selectable options
     *
     * @return list of chosen options
     */
    public static List<String> multiselect(String label, List<String> options) {
        return InputWidgets.multiselect(label, options);
    }

    /**
     * Boolean checkbox defaulting to {@code false}.
     *
     * @param label
     *            widget label
     *
     * @return current value
     */
    public static boolean checkbox(String label) {
        return InputWidgets.checkbox(label);
    }

    /**
     * Boolean checkbox with a configurable default.
     *
     * @param label
     *            widget label
     * @param defaultValue
     *            initial value
     *
     * @return current value
     */
    public static boolean checkbox(String label, boolean defaultValue) {
        return InputWidgets.checkbox(label, defaultValue);
    }

    /**
     * Radio-button group.
     *
     * @param label
     *            widget label
     * @param options
     *            selectable options
     *
     * @return chosen option
     */
    public static String radio(String label, List<String> options) {
        return InputWidgets.radio(label, options);
    }

    /**
     * Push button; returns {@code true} on the rerun triggered by the click.
     *
     * @param label
     *            widget label
     *
     * @return {@code true} when the user just clicked the button
     */
    public static boolean button(String label) {
        return InputWidgets.button(label);
    }

    /**
     * Date picker.
     *
     * @param label
     *            widget label
     * @param defaultValue
     *            initial date
     *
     * @return chosen date
     */
    public static LocalDate dateInput(String label, LocalDate defaultValue) {
        return InputWidgets.dateInput(label, defaultValue);
    }

    /**
     * Time picker.
     *
     * @param label
     *            widget label
     * @param defaultValue
     *            initial time
     *
     * @return chosen time
     */
    public static LocalTime timeInput(String label, LocalTime defaultValue) {
        return InputWidgets.timeInput(label, defaultValue);
    }

    /**
     * Color picker.
     *
     * @param label
     *            widget label
     * @param defaultValue
     *            initial color (CSS hex)
     *
     * @return chosen color as a CSS hex string
     */
    public static String colorPicker(String label, String defaultValue) {
        return InputWidgets.colorPicker(label, defaultValue);
    }

    /**
     * Discrete-option slider.
     *
     * @param label
     *            widget label
     * @param options
     *            ordered options
     * @param defaultValue
     *            initial option
     *
     * @return chosen option
     */
    public static String selectSlider(String label, List<String> options, String defaultValue) {
        return InputWidgets.selectSlider(label, options, defaultValue);
    }

    // ----- File upload / download -------------------------------------------

    /**
     * File upload input.
     *
     * @param label
     *            widget label
     *
     * @return uploaded file key (download store reference), or empty string before upload
     */
    public static String fileUploader(String label) {
        return FileWidgets.fileUploader(label);
    }

    /**
     * Renders a download link pointing at an existing URL.
     *
     * @param label
     *            button label
     * @param url
     *            URL to download
     *
     * @return always {@code false} (download links are anchor elements, not buttons)
     */
    public static boolean downloadButton(String label, String url) {
        return FileWidgets.downloadButton(label, url);
    }

    /**
     * Registers the given bytes in the download store and renders a download link.
     *
     * @param label
     *            button label
     * @param filename
     *            suggested filename
     * @param bytes
     *            raw payload
     * @param contentType
     *            MIME type
     *
     * @return always {@code false}
     */
    public static boolean downloadButton(String label, String filename, byte[] bytes, String contentType) {
        return FileWidgets.downloadButton(label, filename, bytes, contentType);
    }

    /**
     * Serializes rows to CSV and renders a download link.
     *
     * @param label
     *            button label
     * @param filename
     *            suggested filename
     * @param rows
     *            row data
     *
     * @return always {@code false}
     */
    public static boolean downloadCsv(String label, String filename, List<Map<String, Object>> rows) {
        return FileWidgets.downloadCsv(label, filename, rows);
    }

    /**
     * Encodes the JSON string in UTF-8 and renders a download link.
     *
     * @param label
     *            button label
     * @param filename
     *            suggested filename
     * @param json
     *            JSON text
     *
     * @return always {@code false}
     */
    public static boolean downloadJson(String label, String filename, String json) {
        return FileWidgets.downloadJson(label, filename, json);
    }

    // ----- Layout ------------------------------------------------------------

    /**
     * Splits the current frame into {@code count} columns.
     *
     * @param count
     *            number of columns
     * @param body
     *            invoked once per column index
     */
    public static void columns(int count, IntConsumer body) {
        LayoutWidgets.columns(count, body);
    }

    /**
     * Groups children into a single container element.
     *
     * @param body
     *            child body
     */
    public static void container(Runnable body) {
        LayoutWidgets.container(body);
    }

    /**
     * Renders a collapsible expander with a label and child content.
     *
     * @param label
     *            expander label
     * @param body
     *            child body
     */
    public static void expander(String label, Runnable body) {
        LayoutWidgets.expander(label, body);
    }

    /**
     * Renders a tab strip; {@code body} is invoked once per tab index.
     *
     * @param labels
     *            tab labels
     * @param body
     *            invoked once per tab index
     */
    public static void tabs(List<String> labels, IntConsumer body) {
        LayoutWidgets.tabs(labels, body);
    }

    /**
     * Renders a sidebar container that mounts to the left rail.
     *
     * @param body
     *            child body
     */
    public static void sidebar(Runnable body) {
        LayoutWidgets.sidebar(body);
    }

    /** Emits an empty placeholder node, useful for layout slots. */
    public static void empty() {
        LayoutWidgets.empty();
    }

    // ----- Forms -------------------------------------------------------------

    /**
     * Buffers inner widget events until the submit button fires.
     *
     * @param key
     *            stable key identifying the form within the session
     * @param body
     *            child body
     */
    public static void form(String key, Runnable body) {
        FormWidgets.form(key, body);
    }

    /**
     * Submit button for a {@link #form}; returns {@code true} on click.
     *
     * @param label
     *            button label
     *
     * @return {@code true} when the form was just submitted
     */
    public static boolean formSubmitButton(String label) {
        return FormWidgets.formSubmitButton(label);
    }

    // ----- Caching -----------------------------------------------------------

    /**
     * Caches the result of {@code loader} under {@code key} for {@code ttl}.
     *
     * @param <T>
     *            cached value type
     * @param key
     *            cache key
     * @param ttl
     *            entry lifetime
     * @param loader
     *            invoked on miss
     *
     * @return cached or freshly loaded value
     */
    public static <T> T cacheData(String key, Duration ttl, Supplier<T> loader) {
        return CacheOps.cacheData(key, ttl, loader);
    }

    /**
     * Caches a long-lived resource (effectively permanent within a process).
     *
     * @param <T>
     *            cached value type
     * @param key
     *            cache key
     * @param loader
     *            invoked on miss
     *
     * @return cached or freshly loaded value
     */
    public static <T> T cacheResource(String key, Supplier<T> loader) {
        return CacheOps.cacheResource(key, loader);
    }

    // ----- Multi-page --------------------------------------------------------

    /**
     * Renders the multi-page navigator and invokes the body of the current page.
     *
     * @param pages
     *            explicit page declarations
     */
    public static void pages(List<Page> pages) {
        PageNav.pages(pages);
    }

    // ----- Custom components -------------------------------------------------

    /**
     * Registers a custom component spec for later invocation.
     *
     * @param <R>
     *            declared result type
     * @param spec
     *            component declaration
     *
     * @return the same spec, for fluent chaining
     */
    public static <R> CustomComponent<R> registerComponent(CustomComponent<R> spec) {
        return ComponentApi.registerComponent(spec);
    }

    /**
     * Invokes a custom component without a default; returns {@code null} on first render.
     *
     * @param <R>
     *            declared result type
     * @param spec
     *            component declaration
     * @param args
     *            arguments to pass to the renderer
     *
     * @return value yielded by the component, or {@code null} on first render
     */
    public static <R> R component(CustomComponent<R> spec, Map<String, Object> args) {
        return ComponentApi.component(spec, args, null);
    }

    /**
     * Invokes a custom component, returning {@code defaultValue} on the first render.
     *
     * @param <R>
     *            declared result type
     * @param spec
     *            component declaration
     * @param args
     *            arguments to pass to the renderer
     * @param defaultValue
     *            value returned before user interaction
     *
     * @return value yielded by the component
     */
    public static <R> R component(CustomComponent<R> spec, Map<String, Object> args, R defaultValue) {
        return ComponentApi.component(spec, args, defaultValue);
    }

    /**
     * Invokes a display-only custom component (no return value).
     *
     * @param name
     *            component name
     * @param args
     *            arguments to pass to the renderer
     */
    public static void component(String name, Map<String, Object> args) {
        ComponentApi.component(name, args);
    }
}
