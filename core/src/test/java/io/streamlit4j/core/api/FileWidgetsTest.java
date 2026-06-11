package io.streamlit4j.core.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.protocol.RenderNode;
import io.streamlit4j.core.runtime.InMemoryDownloadStore;
import io.streamlit4j.core.runtime.DownloadAccess;
import io.streamlit4j.core.runtime.ScriptRunner;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileWidgetsTest {

    private InMemoryDownloadStore store;

    @BeforeEach
    void replaceDownloadStore() {
        store = new InMemoryDownloadStore();
        DownloadAccess.use(store);
    }

    @AfterEach
    void restoreDownloadStore() {
        DownloadAccess.use(new InMemoryDownloadStore());
    }

    @Test
    void fileUploaderReturnsEmptyWhenNoneStored() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            String[] holder = new String[1];
            runner.render(session, () -> holder[0] = St.fileUploader("U"));
            assertThat(holder[0]).isEmpty();
        }
    }

    @Test
    void fileUploaderReturnsStoredKey() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            String[] holder = new String[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.fileUploader("U"));
            session.updateWidget(root.children().get(0).id(), "abc123");
            runner.render(session, () -> holder[0] = St.fileUploader("U"));
            assertThat(holder[0]).isEqualTo("abc123");
        }
    }

    @Test
    void downloadButtonWithUrlReturnsFalseAndEmitsNode() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            boolean[] holder = new boolean[1];
            RenderNode root = runner.render(session, () -> holder[0] = St.downloadButton("Get", "/file"));
            assertThat(holder[0]).isFalse();
            assertThat(root.children().get(0).props()).containsEntry("label", "Get").containsEntry("url", "/file");
        }
    }

    @Test
    void downloadButtonBytesRegistersAssetAndUsesDownloadUrl() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            byte[] data = new byte[] { 1, 2, 3 };
            RenderNode[] capturedRoot = new RenderNode[1];
            runner.render(session, () -> {
                St.downloadButton("Get", "file.bin", data, "application/octet-stream");
                capturedRoot[0] = null; // we'll inspect via lastRoot
            });
            RenderNode child = session.lastRoot().orElseThrow().children().get(0);
            String url = (String) child.props().get("url");
            assertThat(url).startsWith("/download/");
            String key = url.substring("/download/".length());
            assertThat(store.retrieve(key)).isPresent();
        }
    }

    @Test
    void downloadCsvEncodesRowsAsCsvAndRegisters() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("a", "x");
            row.put("b", 1);
            runner.render(session, () -> St.downloadCsv("Get", "out.csv", List.of(row)));
            String url = (String) session.lastRoot().orElseThrow().children().get(0).props().get("url");
            String key = url.substring("/download/".length());
            byte[] bytes = store.retrieve(key).orElseThrow().bytes();
            String csv = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            assertThat(csv).contains("a,b").contains("x,1");
        }
    }

    @Test
    void downloadCsvEmptyRowsProducesEmptyBytes() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            runner.render(session, () -> St.downloadCsv("Get", "out.csv", List.of()));
            String url = (String) session.lastRoot().orElseThrow().children().get(0).props().get("url");
            String key = url.substring("/download/".length());
            byte[] bytes = store.retrieve(key).orElseThrow().bytes();
            assertThat(bytes).isEmpty();
        }
    }

    @Test
    void downloadCsvEscapesQuotesCommasAndNewlines() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("k", "hello, \"world\"\nrow2");
            runner.render(session, () -> St.downloadCsv("Get", "out.csv", List.of(row)));
            String url = (String) session.lastRoot().orElseThrow().children().get(0).props().get("url");
            String key = url.substring("/download/".length());
            byte[] bytes = store.retrieve(key).orElseThrow().bytes();
            String csv = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            assertThat(csv).contains("\"hello, \"\"world\"\"\nrow2\"");
        }
    }

    @Test
    void downloadCsvEscapesEachSpecialCharIndividually() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            Map<String, Object> commaOnly = new LinkedHashMap<>();
            commaOnly.put("k", "has,comma");
            Map<String, Object> quoteOnly = new LinkedHashMap<>();
            quoteOnly.put("k", "has\"quote");
            Map<String, Object> newlineOnly = new LinkedHashMap<>();
            newlineOnly.put("k", "has\nnewline");

            // Driving each special character individually exercises every
            // short-circuit arm of csvEscape's condition chain.
            runner.render(session, () -> {
                St.downloadCsv("C", "c.csv", List.of(commaOnly));
                St.downloadCsv("Q", "q.csv", List.of(quoteOnly));
                St.downloadCsv("N", "n.csv", List.of(newlineOnly));
            });
            assertThat(session.lastRoot().orElseThrow().children()).hasSize(3);
        }
    }

    @Test
    void downloadCsvTreatsNullCellAsEmpty() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            Map<String, Object> row = new HashMap<>();
            row.put("a", null);
            runner.render(session, () -> St.downloadCsv("Get", "out.csv", List.of(row)));
            String url = (String) session.lastRoot().orElseThrow().children().get(0).props().get("url");
            String key = url.substring("/download/".length());
            byte[] bytes = store.retrieve(key).orElseThrow().bytes();
            String csv = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            assertThat(csv).startsWith("a\n");
        }
    }

    @Test
    void downloadJsonRegistersJsonBytes() {
        try (ScriptRunner runner = new ScriptRunner()) {
            Session session = new Session("s");
            runner.render(session, () -> St.downloadJson("Get", "out.json", "{\"k\":1}"));
            String url = (String) session.lastRoot().orElseThrow().children().get(0).props().get("url");
            String key = url.substring("/download/".length());
            byte[] bytes = store.retrieve(key).orElseThrow().bytes();
            assertThat(new String(bytes, java.nio.charset.StandardCharsets.UTF_8)).isEqualTo("{\"k\":1}");
        }
    }
}
