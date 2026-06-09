# Media

External-URL-referenced media elements. Direct byte transmission goes via `downloadButton(byte[]...)` in [files](./files).

| Java API | Protocol `kind` | props | Frontend rendering |
| --- | --- | --- | --- |
| `St.image(String url)` | `image` | `src` | `<img src>` |
| `St.audio(String url)` | `audio` | `src` | `<audio src controls>` |
| `St.video(String url)` | `video` | `src` | `<video src controls>` |

## Notes

- `url` also supports `data:` URLs (but be mindful of render_delta bandwidth)
- To emit a byte array directly, register it with the download store via `downloadButton(label, filename, bytes, contentType)`, which generates a `/download/<key>` URL. Passing the generated URL to `image` / `audio` / `video` enables inline display
- alt text / caption APIs are not provided in v1 (supplement with `markdown`)
