package io.streamlit4j.core.api;

import static io.streamlit4j.core.api.WidgetSupport.emit;
import static io.streamlit4j.core.api.WidgetSupport.ordered;
import static io.streamlit4j.core.api.WidgetSupport.readStored;
import static io.streamlit4j.core.api.WidgetSupport.widgetId;

import io.streamlit4j.core.port.DownloadStore;
import io.streamlit4j.core.runtime.DownloadAccess;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** File upload and download widgets. */
final class FileWidgets {

    private FileWidgets() {
    }

    static String fileUploader(String label) {
        String id = widgetId("file_uploader", label);
        String value = readStored(id, String.class, "");
        emit("file_uploader", id, ordered("label", label, "value", value));
        return value;
    }

    static boolean downloadButton(String label, String url) {
        String id = widgetId("download_button", label, url);
        emit("download_button", id, ordered("label", label, "url", url));
        return false;
    }

    static boolean downloadButton(String label, String filename, byte[] bytes, String contentType) {
        String key = DownloadAccess.store().register(new DownloadStore.Asset(filename, contentType, bytes));
        return downloadButton(label, "/download/" + key);
    }

    static boolean downloadCsv(String label, String filename, List<Map<String, Object>> rows) {
        return downloadButton(label, filename, toCsvBytes(rows), "text/csv");
    }

    static boolean downloadJson(String label, String filename, String json) {
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
}
