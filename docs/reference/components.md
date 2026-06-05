# カスタムコンポーネント

第一者向けインプロセス React 部品を独自に追加するための API。iframe 隔離方式は採用しない（[ADR-0007](../adr/0007-no-iframe-components) 参照）。

## 宣言と登録

```java
public record CustomComponent<R>(String name, Class<R> resultType) {
    public static CustomComponent<Void> ofVoid(String name);
}
```

| Java API | 戻り値 | 説明 |
| --- | --- | --- |
| `St.registerComponent(CustomComponent<R> spec)` | `CustomComponent<R>` | サーバー側レジストリーに登録（`ComponentRegistry`）。フルエント宣言用にそのまま spec を返す |

## 呼び出し

| Java API | プロトコル `kind` | props | 戻り値 |
| --- | --- | --- | --- |
| `St.component(CustomComponent<R> spec, Map<String, Object> args)` | `component` | `name`、`args`、`value`（オプション） | `R`（初回は `null`） |
| `St.component(CustomComponent<R> spec, Map<String, Object> args, R defaultValue)` | `component` | 同上 | `R`（初回は `defaultValue`） |
| `St.component(String name, Map<String, Object> args)` | `component` | `name`、`args` | なし（表示専用） |

## フロント側登録

`frontend/src/component-registry.ts` の `registerComponent(name, renderer)` で React レンダラーを登録し、`frontend/src/component-builtins.ts` から import すると `main.tsx` 起動時に有効化される。

レンダラーは `CustomComponentRenderProps` (`args` / `value` / `onChange`) を props として受け取る React コンポーネント。

## 値の型変換

`R` は Jackson でデコード可能な型なら何でも良い。`ComponentCodec.coerce(stored, resultType, fallback)` が次の優先順で変換する:

1. `stored` が既に `resultType` のインスタンス → そのまま
2. `stored` が `JsonNode` → `treeToValue`
3. その他 → `convertValue`（Map → record 等）
4. すべて失敗 → `fallback`

## サンプル

ビルトイン例として `star-rating`（5 つ星評価）が `frontend/src/components/StarRating.tsx` に同梱されている。
