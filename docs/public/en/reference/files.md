# Files

Upload and download. The server side registers byte arrays into `DownloadStore`, and the frontend fetches them via `/download/<key>`.

## File upload

| Java API | Protocol `kind` | props | Return value |
| --- | --- | --- | --- |
| `St.fileUploader(String label)` | `file_uploader` | `label`, `value` | `String` (download store key after upload / empty string when not uploaded) |

- The frontend displays an `<input type="file">`, base64-encodes the selected file, and sends it via a `file_upload` envelope
- The server decodes the base64 in `ProtocolEndpoint` and stores it in session state via `session.updateWidget(widgetId, UploadedFile)`

## Download

| Java API | Protocol `kind` | props | Return value |
| --- | --- | --- | --- |
| `St.downloadButton(String label, String url)` | `download_button` | `label`, `url` | `boolean` (always `false` — since it is a link element) |
| `St.downloadButton(String label, String filename, byte[] bytes, String contentType)` | `download_button` | `label`, `url` (auto-generated) | Same as above |
| `St.downloadCsv(String label, String filename, List<Map<String, Object>> rows)` | `download_button` | `label`, `url` | Same as above |
| `St.downloadJson(String label, String filename, String json)` | `download_button` | `label`, `url` | Same as above |

- The byte array variants internally call `DownloadAccess.store().register(Asset(filename, contentType, bytes))` and generate a `/download/<UUID>` URL
- Frontend rendering is `<a class="download-button" href={url} download>`

## Notes

- The return value of `downloadButton` is not a click notification (the link itself is a regular HTTP GET). If click detection is needed, combine it with `button`
- When handling large byte arrays, the in-memory `InMemoryDownloadStore` is kept within the process. For persistence or size limits, handle them with a custom `DownloadStore` implementation
- Size / MIME constraints for `fileUploader` follow `UploadValidator` (configurable via `Streamlit4jProperties`)
