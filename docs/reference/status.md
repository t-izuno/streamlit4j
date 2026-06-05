# ステータスと通知

メトリクス表示と一時的な通知系プリミティブ。すべて即時 emit、戻り値なし。

| Java API | プロトコル `kind` | props | フロント描画 |
| --- | --- | --- | --- |
| `St.metric(String label, Object value)` | `metric` | `label`、`value` | カード `.metric` |
| `St.metric(String label, Object value, Object delta)` | `metric` | `label`、`value`、`delta` | カード（delta バッジ付き） |
| `St.toast(String text)` | `toast` | `text` | `<div role="status">`（短時間表示） |
| `St.progress(double value)` | `progress` | `value`（0.0〜1.0） | `<progress max="1">` |
| `St.spinner(String text)` | `spinner` | `text` | `<div class="spinner" role="status" aria-busy>` |
| `St.status(String text)` | `status` | `text` | `<div role="status">` |

## 注意点

- `progress` の `value` は 0.0〜1.0 を想定。範囲外でもフロントはクランプせずそのまま描画する
- `toast` / `spinner` / `status` はライブリージョン（`role="status"`）として扱われ、スクリーンリーダーで読み上げられる
- `metric` の delta は表示用文字列としてそのまま渡す（自動色付けは将来）
