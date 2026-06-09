# フォーム

複数の入力ウィジェットの値変更を「Submit ボタンが押されるまで」バッファリングする仕組み。

| Java API | プロトコル `kind` | props | 戻り値 |
| --- | --- | --- | --- |
| `St.form(String key, Runnable body)` | `form` | `key` | なし |
| `St.formSubmitButton(String label)` | `form_submit_button` | `label` | `boolean`（送信直後の rerun のみ true） |

## 動作モデル

- `St.form(key, body)` は `RenderContext` のフォーム抑制フラグを立てて `body` を実行する
- フォーム内の入力ウィジェットは widget_event を即時発火せず、ローカル UI 状態だけ更新する（フロント実装）
- `formSubmitButton` クリックで、フォーム内の全 widget_event がまとめてサーバーに送られ、rerun が走る

## 注意点

- `key` はセッション内で一意（フォーム ID として `"k_" + key`）。重複するとフロント描画が重なる
- フォーム外の widget は通常どおり即時 rerun を起こす
- フォーム内で `button` を使うと、`formSubmitButton` とは別の即時 rerun が走るため通常は `formSubmitButton` だけを置く
