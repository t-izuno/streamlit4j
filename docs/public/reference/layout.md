# レイアウト

子要素を入れ子で配置するコンテナー系プリミティブ。`Runnable` / `IntConsumer` の中で発行された emit がそのコンテナーの子になる。

| Java API | プロトコル `kind` | props | 子描画 |
| --- | --- | --- | --- |
| `St.columns(int count, IntConsumer body)` | `columns` | `count` | `count` 個の `column` ノードを子に持つ。各 column は `body.accept(index)` の emit |
| `St.container(Runnable body)` | `container` | なし | `body` の emit |
| `St.expander(String label, Runnable body)` | `expander` | `label` | `<details><summary>` に `body` の emit |
| `St.tabs(List<String> labels, IntConsumer body)` | `tabs` | `labels` | 各 label に対応する `tab` ノードに `body.accept(index)` の emit |
| `St.sidebar(Runnable body)` | `sidebar` | なし | フロントで左側 `<aside>` に固定マウント |
| `St.empty()` | `empty` | なし | `<div class="empty">` プレースホルダー |

## RenderContext の仕組み

- `pushFrame()` で空の子フレームをスタックに積み、`body` 実行中の emit はそのフレームに溜まる
- `popFrame()` で取り出した子要素を親ノードの `children` として `addNode`
- `columns` / `tabs` はループ内で `pushFrame` → `body.accept(i)` → `popFrame` を回す

## 注意点

- `sidebar` はフロントで一意の領域として描画される。複数回呼ぶと最後のものだけが見える挙動になる場合がある
- `expander` の開閉状態はクライアント側のローカル UI 状態（session state に保存しない）
- `empty` は将来の差分更新で「ここを後から差し替える」プレースホルダーとして使う想定
