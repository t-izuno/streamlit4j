package io.streamlit4j.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.api.St;
import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.ScriptRunner;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CatalogCoverageTest {

    @Test
    void atLeastFortyElementKindsAreEmittedByPublicApi() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s-catalog");
            RenderNode root = runner.render(session, CatalogCoverageTest::exerciseAllElements);

            Set<String> kinds = new LinkedHashSet<>();
            collectKinds(root, kinds);

            assertThat(kinds).as("v1 catalog target is 40 main elements (requirements.md §5.2)")
                    .hasSizeGreaterThanOrEqualTo(40);
            assertThat(kinds).contains("title", "header", "subheader", "caption", "markdown", "write", "code", "json",
                    "latex", "html", "divider", "metric", "dataframe", "table", "data_editor", "image", "audio",
                    "video", "toast", "progress", "spinner", "status", "line_chart", "bar_chart", "area_chart",
                    "scatter_chart", "slider", "text_input", "number_input", "text_area", "selectbox", "multiselect",
                    "checkbox", "radio", "button", "date_input", "time_input", "color_picker", "select_slider",
                    "file_uploader", "download_button", "columns", "container", "expander", "tabs", "sidebar", "empty",
                    "form", "form_submit_button");
        }
    }

    private static void exerciseAllElements() {
        St.title("t");
        St.header("h");
        St.subheader("s");
        St.caption("c");
        St.markdown("m");
        St.write("w");
        St.code("code");
        St.json("{}");
        St.latex("x=1");
        St.html("<i>hi</i>");
        St.divider();
        St.metric("M", 1);
        St.dataframe(List.of(Map.of("a", 1)));
        St.table(List.of(Map.of("a", 1)));
        St.dataEditor(List.of(Map.of("a", 1)));
        St.image("/img");
        St.audio("/aud");
        St.video("/vid");
        St.toast("toast");
        St.progress(0.5);
        St.spinner("loading");
        St.status("ok");
        St.lineChart(List.of(Map.of("x", 1, "y", 2)));
        St.barChart(List.of(Map.of("x", 1, "y", 2)));
        St.areaChart(List.of(Map.of("x", 1, "y", 2)));
        St.scatterChart(List.of(Map.of("x", 1, "y", 2)));
        St.slider("Year", 2018, 2026, 2025);
        St.textInput("Name", "");
        St.numberInput("Amount", 0.0);
        St.textArea("Bio", "");
        St.selectbox("Region", List.of("East", "West"));
        St.multiselect("Tags", List.of("a", "b"));
        St.checkbox("Active");
        St.radio("Mode", List.of("light", "dark"));
        St.button("Go");
        St.dateInput("Date", LocalDate.now());
        St.timeInput("Time", LocalTime.NOON);
        St.colorPicker("Color", "#ff0000");
        St.selectSlider("Size", List.of("S", "M", "L"), "M");
        St.fileUploader("Upload");
        St.downloadButton("Download", "/file");
        St.columns(2, i -> St.title("col-" + i));
        St.container(() -> St.title("inside container"));
        St.expander("More", () -> St.markdown("hidden"));
        St.tabs(List.of("A", "B"), i -> St.title("tab-" + i));
        St.sidebar(() -> St.title("side"));
        St.empty();
        St.form("f", () -> St.formSubmitButton("Submit"));
    }

    private static void collectKinds(RenderNode node, Set<String> out) {
        out.add(node.kind());
        for (RenderNode child : node.children()) {
            collectKinds(child, out);
        }
    }
}
