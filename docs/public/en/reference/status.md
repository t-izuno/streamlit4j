# Status & notifications

Primitives for metrics display and transient notifications. All are emitted immediately with no return value.

| Java API | Protocol `kind` | props | Frontend rendering |
| --- | --- | --- | --- |
| `St.metric(String label, Object value)` | `metric` | `label`, `value` | `.metric` card |
| `St.metric(String label, Object value, Object delta)` | `metric` | `label`, `value`, `delta` | Card (with delta badge) |
| `St.toast(String text)` | `toast` | `text` | `<div role="status">` (brief display) |
| `St.progress(double value)` | `progress` | `value` (0.0 to 1.0) | `<progress max="1">` |
| `St.spinner(String text)` | `spinner` | `text` | `<div class="spinner" role="status" aria-busy>` |
| `St.status(String text)` | `status` | `text` | `<div role="status">` |

## Notes

- `progress` expects `value` in the 0.0 to 1.0 range. The frontend renders out-of-range values as-is without clamping
- `toast` / `spinner` / `status` are treated as live regions (`role="status"`) and are announced by screen readers
- The `delta` of `metric` is passed through as a display string (automatic coloring is a future enhancement)
