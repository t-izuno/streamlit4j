# メディア

外部 URL 参照型のメディア要素。バイト直接送信は [files](./files) の `downloadButton(byte[]...)` を経由する。

| Java API | プロトコル `kind` | props | フロント描画 |
| --- | --- | --- | --- |
| `St.image(String url)` | `image` | `src` | `<img src>` |
| `St.audio(String url)` | `audio` | `src` | `<audio src controls>` |
| `St.video(String url)` | `video` | `src` | `<video src controls>` |

## 注意点

- `url` は `data:` URL もサポートする（ただし render_delta 帯域に注意）
- バイト列を直接出したい場合は `downloadButton(label, filename, bytes, contentType)` で download store へ登録すると `/download/<key>` URL が生成される。生成 URL を `image` / `audio` / `video` に渡せばインライン表示可能
- alt テキスト / caption の API は v1 では未提供（`markdown` で補う）
