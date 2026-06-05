# テキストとドキュメントフロー

文書フローを構成する見出し / 本文 / コードブロック / 区切り線などのプリミティブ。すべて即時 emit、戻り値なし。

| Java API | プロトコル `kind` | props | フロント描画 |
| --- | --- | --- | --- |
| `St.title(String text)` | `title` | `text` | `<h1>` |
| `St.header(String text)` | `header` | `text` | `<h2>` |
| `St.subheader(String text)` | `subheader` | `text` | `<h3>` |
| `St.caption(String text)` | `caption` | `text` | `<small>`（muted） |
| `St.markdown(String body)` | `markdown` | `body` | DOMPurify で sanitize した HTML |
| `St.write(Object value)` | `write` | `value`（String 化済み） | プレーンテキスト |
| `St.code(String body)` | `code` | `body` | `<pre><code>` |
| `St.code(String body, String language)` | `code` | `body`、`language` | `<pre data-language>` |
| `St.json(String body)` | `json` | `body` | `<pre class="json">` |
| `St.latex(String body)` | `latex` | `body` | `<span class="latex">`（将来 MathJax/KaTeX） |
| `St.html(String body)` | `html` | `body` | DOMPurify で sanitize した HTML |
| `St.divider()` | `divider` | なし | `<hr>` |

## 注意点

- `markdown` と `html` はフロント側で DOMPurify を通すため、`<script>` 等は除去される
- `write` は `Object#toString` の結果を文字列として送る。複雑なオブジェクトを表示したい場合は `dataframe` / `table` / `json` を使う
- `code` の 2 引数版は `language` が `<pre>` の `data-language` 属性として渡る（シンタックスハイライトは将来対応）
