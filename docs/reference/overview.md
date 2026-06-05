# Reference

streamlit4j の公開 API の各要素を、Java 側シグネチャー / WebSocket プロトコル / フロントエンド描画の 3 点で網羅する。

公開ファサードは [`io.streamlit4j.core.api.St`](https://github.com/t-izuno/streamlit4j/blob/main/core/src/main/java/io/streamlit4j/core/api/St.java) に集約され、内部は要素カテゴリー別の package-private クラス群（`TextWidgets` / `InputWidgets` ほか）に委譲される。

## カテゴリー一覧

| カテゴリー | ページ | 主な要素 |
| --- | --- | --- |
| テキストとドキュメントフロー | [text](./text) | title / header / markdown / write / code / latex / html / divider |
| ステータスと通知 | [status](./status) | metric / toast / progress / spinner / status |
| 表形式データ | [data](./data) | dataframe / table / data_editor |
| メディア | [media](./media) | image / audio / video |
| グラフ | [charts](./charts) | line / bar / area / scatter |
| 入力ウィジェット | [inputs](./inputs) | slider / text_input / selectbox / button / date / time / picker など |
| ファイル | [files](./files) | file_uploader / download_button / download_csv / download_json |
| レイアウト | [layout](./layout) | columns / container / expander / tabs / sidebar / empty |
| フォーム | [forms](./forms) | form / form_submit_button |
| キャッシュ | [cache](./cache) | cacheData / cacheResource |
| マルチページ | [pages](./pages) | pages |
| カスタムコンポーネント | [components](./components) | registerComponent / component |
| 制御フロー | [control](./control) | rerun / stop / state |

## 補助情報

- プロトコルの基本（envelope / render_delta / widget_event）は [`specification.md`](../specification) 2〜3 章を参照
- アーキテクチャー判断は [`docs/adr/`](../adr/) を参照
- 完全な Javadoc は `./mvnw -P release package` で各モジュールの `*-javadoc.jar` を生成
