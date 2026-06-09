# Tabular data

Pass row data as a list of {@code Map<String, Object>}.

| Java API | Protocol `kind` | props | Frontend rendering |
| --- | --- | --- | --- |
| `St.dataframe(List<Map<String, Object>> rows)` | `dataframe` | `rows` | `<table class="dataframe">` (sorting / filtering planned for the future) |
| `St.table(List<Map<String, Object>> rows) ` | `table` | `rows` | `<table class="table">` |
| `St.dataEditor(List<Map<String, Object>> rows)` | `data_editor` | `rows` | `<table class="data_editor">` (editing -> widget_event planned for the future) |

## Row data conventions

- Each row's `Map` is recommended to have the **same set of keys**. The frontend determines column order from the keys of the first row
- Values are expected to be primitives / strings / date-times / `null`. Complex objects are JSON-serialized via Jackson

## Notes

- Commit of `dataEditor` edits -> server propagation is not implemented in v1. The `data_editor` node itself is rendered, but edited values are not sent to session state
- When displaying large amounts of data, the caller must perform filtering and paging on both the server and client side (transparent option passing is not supported in v1)
