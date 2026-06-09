package io.streamlit4j.examples;

import io.streamlit4j.core.api.St;
import io.streamlit4j.server.Streamlit4jServer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates data display surfaces: dataframe / table / charts / metric /
 * data cache. Uses a small synthetic dataset to keep the example self-contained.
 * Runnable as {@code java -cp <classpath> io.streamlit4j.examples.DataDemo [port]}.
 */
public final class DataDemo {

    private static final int DEFAULT_PORT = 8501;

    private DataDemo() {}

    /** Renders the demo. Invoked once per session by the runtime. */
    public static void run() {
        St.title("Data display");
        St.markdown("Tables, charts, and **cached** lookups in one page.");

        List<Map<String, Object>> sales = St.cacheData("sales-2026", Duration.ofMinutes(5), DataDemo::loadSales);

        St.header("Metrics");
        double total = sales.stream()
                .mapToDouble(row -> ((Number) row.get("amount")).doubleValue())
                .sum();
        St.columns(2, index -> {
            if (index == 0) {
                St.metric("Total revenue", String.format("¥%,.0f", total));
            } else {
                St.metric("Rows", sales.size());
            }
        });

        St.header("Dataframe");
        St.dataframe(sales);

        St.header("Charts");
        St.tabs(List.of("Line", "Bar", "Area", "Scatter"), index -> {
            switch (index) {
                case 0 -> St.lineChart(sales);
                case 1 -> St.barChart(sales);
                case 2 -> St.areaChart(sales);
                default -> St.scatterChart(sales);
            }
        });
    }

    /**
     * Boots an embedded server that serves this demo on the given port.
     *
     * @param args optional single positional argument: the listen port (default {@value #DEFAULT_PORT})
     * @throws Exception when the server fails to start
     */
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        try (Streamlit4jServer server = new Streamlit4jServer(port, () -> DataDemo::run)) {
            server.start();
            Thread.currentThread().join();
        }
    }

    private static List<Map<String, Object>> loadSales() {
        // 実運用では DB やファイルから読む。ここではキャッシュ動作確認のための合成データ。
        String[] regions = {"Tokyo", "Osaka", "Nagoya", "Fukuoka"};
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            for (String region : regions) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("month", month);
                row.put("region", region);
                row.put("amount", 100_000 + (long) (Math.random() * 900_000));
                rows.add(row);
            }
        }
        return rows;
    }
}
