package io.streamlit4j.examples;

import io.streamlit4j.core.api.St;
import io.streamlit4j.core.domain.SessionState;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Comprehensive streamlit4j showcase used as the default CLI demo. Features a
 * left sidebar with navigation among multiple feature pages (Home / To-do /
 * Widgets / Layout / Data / About). Each page exercises a different facet of
 * the public {@code St.*} API.
 */
public final class ShowcaseDemo {

    private static final String NAV_HOME = "Home";
    private static final String NAV_TODO = "To-do list";
    private static final String NAV_WIDGETS = "Widgets";
    private static final String NAV_LAYOUT = "Layout";
    private static final String NAV_DATA = "Data";
    private static final String NAV_ABOUT = "About";

    private static final List<String> PAGES = List.of(NAV_HOME, NAV_TODO, NAV_WIDGETS, NAV_LAYOUT, NAV_DATA, NAV_ABOUT);

    private ShowcaseDemo() {}

    /** Renders the demo. Invoked once per session by the runtime. */
    public static void run() {
        String[] selected = {PAGES.get(0)};
        St.sidebar(() -> {
            St.title("streamlit4j");
            St.markdown("**JVM 上の対話型データアプリフレームワーク**");
            St.divider();
            selected[0] = St.radio("Navigation", PAGES);
            St.divider();
            St.markdown("[GitHub](https://github.com/t-izuno/streamlit4j)");
        });

        switch (selected[0]) {
            case NAV_TODO -> renderTodo();
            case NAV_WIDGETS -> renderWidgets();
            case NAV_LAYOUT -> renderLayout();
            case NAV_DATA -> renderData();
            case NAV_ABOUT -> renderAbout();
            default -> renderHome();
        }
    }

    private static void renderHome() {
        St.title("streamlit4j ショーケース");
        St.markdown(
                """
                **streamlit4j** は、Java / JVM 上で対話型データアプリを構築するための
                オープンソースフレームワークです。Streamlit (Python) のスクリプト再実行
                モデルを JVM 向けに適応し、records / sealed types / 仮想スレッドといった
                モダン Java の機能を活用します。

                左サイドバーから機能カテゴリーを選んでデモを操作してください。
                """);

        St.header("含まれるデモ");
        St.markdown(
                """
                - **To-do list** — セッション状態を使った CRUD デモ
                - **Widgets** — 入力ウィジェット（slider / textInput / selectbox / date / time など）
                - **Layout** — columns / tabs / expander / form / sidebar
                - **Data** — dataframe / metric / line/bar/area/scatter chart + cacheData
                - **About** — プロジェクト情報
                """);
    }

    @SuppressWarnings("unchecked")
    private static void renderTodo() {
        St.title("To-do list");
        St.markdown("セッション状態（`St.state()`）でリストを保持する CRUD サンプル。");

        SessionState state = St.state();
        List<Todo> todos = (List<Todo>) state.get("showcase_todos", List.class).orElse(null);
        if (todos == null) {
            todos = new ArrayList<>(List.of(
                    new Todo(UUID.randomUUID().toString(), "牛乳を買う"),
                    new Todo(UUID.randomUUID().toString(), "皿を洗う"),
                    new Todo(UUID.randomUUID().toString(), "小説を書く")));
            state.put("showcase_todos", todos);
        }
        final List<Todo> current = todos;

        St.form("new_todo", () -> {
            String text = St.textInput("新しい To-do", "");
            if (St.formSubmitButton("追加") && !text.isBlank()) {
                current.add(new Todo(UUID.randomUUID().toString(), text.trim()));
            }
        });

        if (current.isEmpty()) {
            St.markdown("_やることはありません。凧を上げに行きましょう。_");
            return;
        }

        St.header("一覧（" + current.size() + " 件）");
        List<String> doneLabels = new ArrayList<>();
        for (Todo todo : current) {
            String label = todo.text();
            boolean done = St.checkbox(label, false);
            if (done) {
                doneLabels.add(label);
            }
        }

        if (!doneLabels.isEmpty() && St.button("完了したものを削除（" + doneLabels.size() + "）")) {
            current.removeIf(t -> doneLabels.contains(t.text()));
        }
    }

    private static void renderWidgets() {
        St.title("入力ウィジェット");
        St.markdown("streamlit4j が提供する主要な入力ウィジェットの一覧。");

        St.header("テキスト・数値");
        String name = St.textInput("お名前", "Ada");
        double amount = St.numberInput("金額", 1000.0);
        St.write("こんにちは、" + name + "さん（金額 " + amount + " 円）");

        St.header("選択");
        String role = St.selectbox("役割", List.of("エンジニア", "デザイナー", "マネージャー"));
        String env = St.radio("環境", List.of("dev", "staging", "prod"));
        boolean notify = St.checkbox("完了時に通知する", true);
        St.write("role=" + role + " env=" + env + " notify=" + notify);

        St.header("日時・色");
        LocalDate date = St.dateInput("日付", LocalDate.now());
        LocalTime time = St.timeInput("時刻", LocalTime.of(9, 0));
        String color = St.colorPicker("アクセントカラー", "#4f46e5");
        St.write("date=" + date + " time=" + time + " color=" + color);

        St.header("スライダー");
        int year = St.slider("年", 2020, 2030, 2026);
        St.metric("選択中の年", year);

        if (St.button("送信")) {
            St.toast("送信完了 @ " + LocalTime.now());
        }
    }

    private static void renderLayout() {
        St.title("レイアウトプリミティブ");
        St.markdown("columns / tabs / expander / form など。");

        St.header("Columns（3 カラム）");
        St.columns(3, index -> {
            St.subheader("カラム " + (index + 1));
            St.metric("値", (index + 1) * 100);
        });

        St.header("Tabs");
        St.tabs(List.of("概要", "詳細", "ログ"), index -> {
            switch (index) {
                case 0 -> St.markdown("サマリー的な内容をここに。");
                case 1 -> St.markdown("詳細な説明本文をここに。");
                default -> St.code("2026-06-01 12:00:00 INFO  started\n", "log");
            }
        });

        St.header("Expander");
        St.expander("診断情報を表示", () -> {
            St.json("{\"status\":\"ok\",\"latencyMs\":42}");
        });

        St.header("Form");
        St.form("layout_form", () -> {
            String user = St.textInput("ユーザー名", "");
            if (St.formSubmitButton("Sign in")) {
                St.toast("Signed in as " + (user.isBlank() ? "(anonymous)" : user));
            }
        });
    }

    private static void renderData() {
        St.title("データ表示");
        St.markdown("テーブル / メトリクス / グラフを `St.cacheData` 経由で表示。");

        List<Map<String, Object>> sales =
                St.cacheData("showcase_sales", Duration.ofMinutes(5), ShowcaseDemo::loadSales);

        St.header("メトリクス");
        double total = sales.stream()
                .mapToDouble(row -> ((Number) row.get("amount")).doubleValue())
                .sum();
        St.columns(2, index -> {
            if (index == 0) {
                St.metric("総売上", String.format("¥%,.0f", total));
            } else {
                St.metric("件数", sales.size());
            }
        });

        St.header("テーブル");
        St.dataframe(sales);

        St.header("グラフ");
        St.tabs(List.of("Line", "Bar", "Area", "Scatter"), index -> {
            switch (index) {
                case 0 -> St.lineChart(sales);
                case 1 -> St.barChart(sales);
                case 2 -> St.areaChart(sales);
                default -> St.scatterChart(sales);
            }
        });
    }

    private static void renderAbout() {
        St.title("About streamlit4j");
        St.markdown(
                """
                **streamlit4j** はコミュニティーによる独立した OSS（MIT License）です。
                Snowflake, Inc. や Streamlit プロジェクトとは提携 / 公認 / スポンサー関係はありません。

                ## ライセンス

                MIT License で公開。

                ## ソースコード

                <https://github.com/t-izuno/streamlit4j>

                ## ドキュメント

                公開ドキュメントサイトは `cd docs/public && npm run docs:dev` でローカル起動できます。
                """);

        St.header("ランタイム情報");
        St.columns(2, index -> {
            if (index == 0) {
                St.metric("Java", System.getProperty("java.version"));
            } else {
                St.metric("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
            }
        });
    }

    private static List<Map<String, Object>> loadSales() {
        String[] regions = {"東京", "大阪", "名古屋", "福岡"};
        List<Map<String, Object>> rows = new ArrayList<>();
        long seed = 42L;
        for (int month = 1; month <= 12; month++) {
            for (String region : regions) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("month", month);
                row.put("region", region);
                seed = (seed * 1103515245L + 12345L) & 0x7fffffffL;
                row.put("amount", 100_000L + (seed % 900_000L));
                rows.add(row);
            }
        }
        return rows;
    }

    private record Todo(String id, String text) {}
}
