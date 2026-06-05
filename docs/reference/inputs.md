# 入力ウィジェット

ユーザー操作で値が変わるウィジェット。戻り値は session state から復元された現在値。初回 render では `defaultValue`、以降は `widget_event` で更新された値が返る。

| Java API | プロトコル `kind` | props | 戻り値 |
| --- | --- | --- | --- |
| `St.slider(String label, int min, int max, int defaultValue)` | `slider` | `label`、`min`、`max`、`value` | `int` |
| `St.textInput(String label, String defaultValue)` | `text_input` | `label`、`value` | `String` |
| `St.numberInput(String label, double defaultValue)` | `number_input` | `label`、`value` | `double` |
| `St.textArea(String label, String defaultValue)` | `text_area` | `label`、`value` | `String` |
| `St.selectbox(String label, List<String> options)` | `selectbox` | `label`、`options`、`value` | `String` |
| `St.multiselect(String label, List<String> options)` | `multiselect` | `label`、`options`、`value` | `List<String>` |
| `St.checkbox(String label)` | `checkbox` | `label`、`value` | `boolean`（既定 false） |
| `St.checkbox(String label, boolean defaultValue)` | `checkbox` | `label`、`value` | `boolean` |
| `St.radio(String label, List<String> options)` | `radio` | `label`、`options`、`value` | `String` |
| `St.button(String label)` | `button` | `label` | `boolean`（クリック直後の rerun のみ true） |
| `St.dateInput(String label, LocalDate defaultValue)` | `date_input` | `label`、`value` (ISO) | `LocalDate` |
| `St.timeInput(String label, LocalTime defaultValue)` | `time_input` | `label`、`value` (ISO) | `LocalTime` |
| `St.colorPicker(String label, String defaultValue)` | `color_picker` | `label`、`value` (`#rrggbb`) | `String` |
| `St.selectSlider(String label, List<String> options, String defaultValue)` | `select_slider` | `label`、`options`、`value` | `String` |

## 共通の挙動

- 各ウィジェットの id は呼び出し位置（class:method:line）+ kind + 引数ハッシュで `WidgetIds` が決定する
- session state には `id -> value` で永続化され、次の rerun で再現される
- `button` だけは「クリック直後に true → state に false を上書き」のワンショット動作

## 注意点

- `radio` / `selectbox` の `options` が空リストのときは戻り値が `null` になる
- `numberInput` は `double` 固定。整数値が欲しい場合はキャストするか `slider` を使う
- `dateInput` / `timeInput` の `value` はワイヤ上は ISO 文字列、Java 側で `LocalDate` / `LocalTime` に復号される
