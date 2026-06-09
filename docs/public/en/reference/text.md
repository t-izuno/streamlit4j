# Text & document flow

Primitives that compose the document flow: heading / body text / code block / divider, etc. All are emitted immediately with no return value.

| Java API | Protocol `kind` | props | Frontend rendering |
| --- | --- | --- | --- |
| `St.title(String text)` | `title` | `text` | `<h1>` |
| `St.header(String text)` | `header` | `text` | `<h2>` |
| `St.subheader(String text)` | `subheader` | `text` | `<h3>` |
| `St.caption(String text)` | `caption` | `text` | `<small>` (muted) |
| `St.markdown(String body)` | `markdown` | `body` | HTML sanitized via DOMPurify |
| `St.write(Object value)` | `write` | `value` (stringified) | Plain text |
| `St.code(String body)` | `code` | `body` | `<pre><code>` |
| `St.code(String body, String language)` | `code` | `body`, `language` | `<pre data-language>` |
| `St.json(String body)` | `json` | `body` | `<pre class="json">` |
| `St.latex(String body)` | `latex` | `body` | `<span class="latex">` (MathJax/KaTeX in the future) |
| `St.html(String body)` | `html` | `body` | HTML sanitized via DOMPurify |
| `St.divider()` | `divider` | none | `<hr>` |

## Notes

- `markdown` and `html` are passed through DOMPurify on the frontend, so `<script>` and similar tags are stripped
- `write` sends the result of `Object#toString` as a string. To display complex objects, use `dataframe` / `table` / `json`
- For the two-argument form of `code`, `language` is passed as the `data-language` attribute on `<pre>` (syntax highlighting is planned for the future)
