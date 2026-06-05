# 制御フロー

スクリプト実行を中断 / 再実行する制御。プロトコル / フロント描画には現れない（サーバー側のみ）。

| Java API | 戻り値 | 説明 |
| --- | --- | --- |
| `St.rerun()` | なし（throws） | `ControlSignals.RerunRequested` を投げ、`ScriptRunner` が同セッションを即再実行する |
| `St.stop()` | なし（throws） | `ControlSignals.StopRequested` を投げ、現在の rerun を中断する。次の widget_event まで再実行されない |
| `St.state()` | `SessionState` | 現在のセッションの key/value state アクセサー |

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

ウィジェット ID 用の key は内部で `w_*` / `k_*` プレフィックス付きで生成される（[runtime/WidgetIds](https://github.com/t-izuno/streamlit4j/blob/main/core/src/main/java/io/streamlit4j/core/runtime/WidgetIds.java) 参照）。任意の key を `put` する場合はそれらと衝突しない名前空間を選ぶこと。

## 注意点

- `rerun` / `stop` は `RuntimeException` 派生のため、`try/catch (Exception)` で捕まえると挙動が壊れる。`ScriptRunner` まで素通しすること
- 同一 rerun 内で `rerun` を連続して投げた場合のループ防止は `ScriptRunner` の上限（5 回）で守られる
- `state()` の返値は同セッションで共有されるため、`get` した直後に別 rerun が `put` する競合に注意（v1 では `RenderContext` 単一スレッドが基本）
