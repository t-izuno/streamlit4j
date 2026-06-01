package io.streamlit4j.examples;

import io.streamlit4j.core.St;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates data display surfaces: dataframe / table / charts / metric /
 * data cache. Uses a small synthetic dataset to keep the example self-contained.
 */
public final class DataDemo {

    private DataDemo() {}

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
