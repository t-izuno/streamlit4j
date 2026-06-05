# マルチページ

明示登録方式のページナビゲーター。規約ベース自動登録は採用しない（[ADR-0005](../internal/adr/0005-explicit-page-registration) 参照）。

| Java API | プロトコル `kind` | props | 戻り値 |
| --- | --- | --- | --- |
| `St.pages(List<Page> pages)` | `pages` | `pages`（`[{name, path}, ...]`）、`current` | なし |

`Page` は次の record:

```java
public record Page(String name, String path, Runnable body) {}
```

## 動作モデル

1. `St.pages(...)` は session state `"__page__"` を読み取って現在のパスを決める
   - 未設定ならリスト先頭がデフォルト
2. `pages` ノードを emit（フロントは `<nav class="pages">` を描画）
3. 該当ページの `body.run()` を呼んで、続く emit を本文として積む

フロント側のページ切替は `widget_event(widgetId="__page__", value=<path>)` を送ることで実現する。

## 注意点

- ページの並び順は `List` 順序がそのまま反映される
- pushState ベースのクリーン URL は未対応（フロントは `window.location.hash` で `__page__` を同期）
- `pages` を呼ばないアプリは単一ページ。トップレベルで直接 widget を呼ぶだけで動く
