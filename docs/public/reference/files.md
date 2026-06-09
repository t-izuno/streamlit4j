# ファイル

アップロードとダウンロード。サーバー側はバイト列を `DownloadStore` に登録し、フロントは `/download/<key>` で取得する。

## ファイルアップロード

| Java API | プロトコル `kind` | props | 戻り値 |
| --- | --- | --- | --- |
| `St.fileUploader(String label)` | `file_uploader` | `label`、`value` | `String`（アップロード後の download store キー / 未アップロード時は空文字列） |

- フロントは `<input type="file">` を表示し、選択されたファイルを base64 化して `file_upload` envelope で送る
- サーバーは `ProtocolEndpoint` で base64 をデコードし、`session.updateWidget(widgetId, UploadedFile)` でセッション state に格納

## ダウンロード

| Java API | プロトコル `kind` | props | 戻り値 |
| --- | --- | --- | --- |
| `St.downloadButton(String label, String url)` | `download_button` | `label`、`url` | `boolean`（常に `false` — リンク要素のため） |
| `St.downloadButton(String label, String filename, byte[] bytes, String contentType)` | `download_button` | `label`、`url`（自動生成） | 同上 |
| `St.downloadCsv(String label, String filename, List<Map<String, Object>> rows)` | `download_button` | `label`、`url` | 同上 |
| `St.downloadJson(String label, String filename, String json)` | `download_button` | `label`、`url` | 同上 |

- バイト列版は内部で `DownloadAccess.store().register(Asset(filename, contentType, bytes))` を呼び、`/download/<UUID>` URL を生成
- フロント描画は `<a class="download-button" href={url} download>`

## 注意点

- `downloadButton` の戻り値はクリック通知ではない（リンク自体は通常の HTTP GET）。クリック検出が必要なら `button` を併用する
- 大きなバイト列を扱う場合、in-memory の `InMemoryDownloadStore` はプロセス内に保持される。永続化やサイズ制限はカスタム `DownloadStore` 実装で対処する
- `fileUploader` のサイズ / MIME 制約は `UploadValidator`（`Streamlit4jProperties` 経由で設定可能）に従う
