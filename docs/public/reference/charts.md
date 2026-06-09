# グラフ

行データから 4 種のチャートを生成する。v1 では描画ライブラリーを使わずプレースホルダー（タイトル + 上位 5 行の JSON）を表示する。

| Java API | プロトコル `kind` | props | フロント描画（v1） |
| --- | --- | --- | --- |
| `St.lineChart(List<Map<String, Object>> data)` | `line_chart` | `data` | `<figure class="line_chart">`（プレースホルダー） |
| `St.barChart(List<Map<String, Object>> data)` | `bar_chart` | `data` | `<figure class="bar_chart">`（プレースホルダー） |
| `St.areaChart(List<Map<String, Object>> data)` | `area_chart` | `data` | `<figure class="area_chart">`（プレースホルダー） |
| `St.scatterChart(List<Map<String, Object>> data)` | `scatter_chart` | `data` | `<figure class="scatter_chart">`（プレースホルダー） |

## データ形式

- `List<Map<String, Object>>`。各 `Map` は同じキー集合を持つ
- 1 列を index / 残りを系列とみなす運用が推奨。明示的な軸設定は v1 では未対応

## 注意点

- 実描画ライブラリー（Chart.js / D3 / VegaLite 等）への置き換えは backlog として扱う
- プロトコル上の props は `data` のみで、軸 / 凡例 / 色などはフロント側のデフォルトに依存する
