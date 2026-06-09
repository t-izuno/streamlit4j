# Control flow

Control to halt / rerun script execution. Does not appear in the protocol / front-end rendering (server-side only).

| Java API | Return value | Description |
| --- | --- | --- |
| `St.rerun()` | none (throws) | Throws `ControlSignals.RerunRequested`; `ScriptRunner` immediately reruns the same session |
| `St.stop()` | none (throws) | Throws `ControlSignals.StopRequested` to halt the current rerun. Will not rerun until the next widget_event |
| `St.state()` | `SessionState` | Key/value state accessor for the current session |

## SessionState

```java
public final class SessionState {
    public <T> Optional<T> get(String key, Class<T> type);
    public <T> T getOrDefault(String key, Class<T> type, T fallback);
    public void put(String key, Object value);
    public boolean contains(String key);
    public void remove(String key);
    public int size();
}
```

Keys for widget IDs are generated internally with `w_*` / `k_*` prefixes (see [runtime/WidgetIds](https://github.com/t-izuno/streamlit4j/blob/main/core/src/main/java/io/streamlit4j/core/runtime/WidgetIds.java)). When `put`ting arbitrary keys, choose a namespace that does not collide with them.

## Notes

- `rerun` / `stop` derive from `RuntimeException`, so catching them with `try/catch (Exception)` breaks the behavior. Let them pass through to `ScriptRunner`
- Loop prevention for throwing `rerun` repeatedly within the same rerun is guarded by `ScriptRunner`'s upper limit (5 times)
- The return value of `state()` is shared across the same session, so beware of races where another rerun `put`s right after a `get` (in v1, a single-threaded `RenderContext` is the baseline)
