# Input widgets

Widgets whose values change through user interaction. The return value is the current value restored from session state. On the initial render, `defaultValue` is returned; thereafter, the value updated via `widget_event` is returned.

| Java API | Protocol `kind` | props | Return value |
| --- | --- | --- | --- |
| `St.slider(String label, int min, int max, int defaultValue)` | `slider` | `label`, `min`, `max`, `value` | `int` |
| `St.textInput(String label, String defaultValue)` | `text_input` | `label`, `value` | `String` |
| `St.numberInput(String label, double defaultValue)` | `number_input` | `label`, `value` | `double` |
| `St.textArea(String label, String defaultValue)` | `text_area` | `label`, `value` | `String` |
| `St.selectbox(String label, List<String> options)` | `selectbox` | `label`, `options`, `value` | `String` |
| `St.multiselect(String label, List<String> options)` | `multiselect` | `label`, `options`, `value` | `List<String>` |
| `St.checkbox(String label)` | `checkbox` | `label`, `value` | `boolean` (defaults to false) |
| `St.checkbox(String label, boolean defaultValue)` | `checkbox` | `label`, `value` | `boolean` |
| `St.radio(String label, List<String> options)` | `radio` | `label`, `options`, `value` | `String` |
| `St.button(String label)` | `button` | `label` | `boolean` (true only on the rerun immediately following the click) |
| `St.dateInput(String label, LocalDate defaultValue)` | `date_input` | `label`, `value` (ISO) | `LocalDate` |
| `St.timeInput(String label, LocalTime defaultValue)` | `time_input` | `label`, `value` (ISO) | `LocalTime` |
| `St.colorPicker(String label, String defaultValue)` | `color_picker` | `label`, `value` (`#rrggbb`) | `String` |
| `St.selectSlider(String label, List<String> options, String defaultValue)` | `select_slider` | `label`, `options`, `value` | `String` |

## Common behavior

- Each widget's id is determined by `WidgetIds` from the call site (class:method:line) + kind + an argument hash
- Values are persisted in session state as `id -> value` and reproduced on the next rerun
- Only `button` has one-shot behavior: "true immediately after the click, then overwritten with false in state"

## Notes

- When the `options` of `radio` / `selectbox` is an empty list, the return value is `null`
- `numberInput` is fixed to `double`. If you need an integer value, cast it or use `slider`
- For `dateInput` / `timeInput`, `value` is an ISO string on the wire and is decoded into `LocalDate` / `LocalTime` on the Java side
