# Charts

Generate four kinds of charts from row data. In v1, no charting library is used; a placeholder (title + top 5 rows of JSON) is displayed instead.

| Java API | Protocol `kind` | props | Frontend rendering (v1) |
| --- | --- | --- | --- |
| `St.lineChart(List<Map<String, Object>> data)` | `line_chart` | `data` | `<figure class="line_chart">` (placeholder) |
| `St.barChart(List<Map<String, Object>> data)` | `bar_chart` | `data` | `<figure class="bar_chart">` (placeholder) |
| `St.areaChart(List<Map<String, Object>> data)` | `area_chart` | `data` | `<figure class="area_chart">` (placeholder) |
| `St.scatterChart(List<Map<String, Object>> data)` | `scatter_chart` | `data` | `<figure class="scatter_chart">` (placeholder) |

## Data format

- `List<Map<String, Object>>`. Each `Map` has the same set of keys
- The recommended convention is to treat one column as the index and the rest as series. Explicit axis configuration is not supported in v1

## Notes

- Replacing the placeholder with an actual charting library (Chart.js / D3 / VegaLite, etc.) is tracked as backlog
- The only prop on the protocol is `data`; axes / legend / colors depend on frontend defaults
