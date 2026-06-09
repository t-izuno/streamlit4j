# Forms

A mechanism that buffers value changes from multiple input widgets until the Submit button is pressed.

| Java API | Protocol `kind` | props | Return value |
| --- | --- | --- | --- |
| `St.form(String key, Runnable body)` | `form` | `key` | None |
| `St.formSubmitButton(String label)` | `form_submit_button` | `label` | `boolean` (true only on the rerun immediately after submission) |

## Behavior model

- `St.form(key, body)` sets the form suppression flag on `RenderContext` and executes `body`
- Input widgets inside the form do not fire widget_event immediately; they only update local UI state (front-end implementation)
- When `formSubmitButton` is clicked, all widget_event entries within the form are sent to the server together, and a rerun is triggered

## Notes

- `key` must be unique within the session (the form ID is `"k_" + key`). Duplicates cause overlapping front-end rendering
- Widgets outside the form trigger immediate reruns as usual
- Using `button` inside a form triggers an immediate rerun separate from `formSubmitButton`, so normally only `formSubmitButton` should be placed there
