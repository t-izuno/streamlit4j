# 表形式データ

行データを {@code Map<String, Object>} のリストとして渡す。

| Java API | プロトコル `kind` | props | フロント描画 |
| --- | --- | --- | --- |
| `St.dataframe(List<Map<String, Object>> rows)` | `dataframe` | `rows` | `<table class="dataframe">`（将来 ソート / フィルター対応） |
| `St.table(List<Map<String, Object>> rows) ` | `table` | `rows` | `<table class="table">` |
| `St.dataEditor(List<Map<String, Object>> rows)` | `data_editor` | `rows` | `<table class="data_editor">`（将来 編集 → widget_event） |

## 行データの規約

- 各行の `Map` は **同じキー集合** を持つことを推奨。フロントは最初の行のキーから列順を決定する
- 値はプリミティブ / 文字列 / 日時 / `null` を想定。複雑なオブジェクトは Jackson 経由で JSON シリアライズされる

## 注意点

- `dataEditor` の編集確定 → サーバー反映は v1 では未実装。`data_editor` ノード自体は描画されるが、編集された値は session state へ送信されない
- 大量データ表示時はサーバー側 / クライアント側ともにフィルタリング・ページングを呼び出し側で行うこと（v1 でのオプション透過渡しは未対応）
