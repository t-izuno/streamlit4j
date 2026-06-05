# カスタムコンポーネント作成ガイド

streamlit4j に独自の React 部品を追加し、Java スクリプトから `St.component(...)` で呼び出せるようにする手順。in-process 方式のみ提供（iframe 隔離は [ADR-0007](../adr/0007-no-iframe-components) で不採用）。

## 前提

| 項目 | 内容 |
| --- | --- |
| Java | 21 LTS |
| Node | 22+（フロントを触る場合） |
| 信頼境界 | 「内製 + vendor された第三者ソース」のみ。動的な untrusted 第三者ロードはサポート外 |

## 全体像

カスタムコンポーネント追加には **Java 側 / TS 側の 2 箇所** で「同じ名前」を登録する。

```text
   Java スクリプト                       React フロントエンド
 ─────────────────────                ──────────────────────────────
 ① CustomComponent<R> 宣言        ②  components/<Name>.tsx で
   ↓                                  CustomComponentRenderProps を
 ② St.registerComponent(spec)       受ける React コンポーネント実装
   ↓                                  ↓
 ③ St.component(spec, args, def)  ③ component-builtins.ts で
                                      registerComponent('<name>', X)
```

両者で「name 文字列」が一致していれば、`render.tsx` がレジストリーからレンダラーを引いてマウントする。

## ステップ 1: Java 側で宣言する

```java
import io.streamlit4j.core.api.St;
import io.streamlit4j.core.domain.CustomComponent;
import java.util.Map;

public final class MyApp {

  // 結果型 R は Jackson でデコード可能な任意の型
  private static final CustomComponent<Integer> RATING =
      St.registerComponent(new CustomComponent<>("star-rating", Integer.class));

  public static void run() {
    St.title("Survey");
    int rating = St.component(RATING, Map.of("label", "Rate this app", "max", 5), 0);
    St.write("Current rating: " + rating);
  }
}
```

ポイント:

- `CustomComponent<R>` は record。`name` は `[a-z][a-z0-9-]*` 推奨（小文字 + ハイフン）
- `St.registerComponent` の戻り値は spec をそのまま返す。`static final` フィールド化が一般的
- `St.component(spec, args)` は値を返す 2 引数版 / `St.component(spec, args, default)` の 3 引数版 / 表示専用 `St.component(name, args)` の 3 種

## ステップ 2: React レンダラーを書く

`frontend/src/components/<Name>.tsx` を新規追加。`CustomComponentRenderProps` を props に取り、`onChange(value)` で値を返す。

```tsx
// frontend/src/components/StarRating.tsx
import { useMemo } from 'react';
import type { CustomComponentRenderProps } from '../component-registry';

const DEFAULT_MAX = 5;

function clampMax(raw: unknown): number {
  const m = typeof raw === 'number' && Number.isFinite(raw) ? Math.floor(raw) : DEFAULT_MAX;
  return Math.max(1, Math.min(10, m));
}

function clampValue(raw: unknown, max: number): number {
  const v = typeof raw === 'number' && Number.isFinite(raw) ? Math.floor(raw) : 0;
  return Math.max(0, Math.min(max, v));
}

export function StarRating({ args, value, onChange }: CustomComponentRenderProps) {
  const label = typeof args.label === 'string' ? args.label : 'Rating';
  const max = clampMax(args.max);
  const current = clampValue(value, max);
  const stars = useMemo(() => Array.from({ length: max }, (_, i) => i + 1), [max]);

  return (
    <fieldset className="component component--star-rating">
      <legend>{label}</legend>
      {stars.map((n) => (
        <button
          key={n}
          type="button"
          aria-pressed={n <= current}
          onClick={() => onChange(n === current ? 0 : n)}
        >
          {n <= current ? '★' : '☆'}
        </button>
      ))}
    </fieldset>
  );
}
```

ポイント:

- `args` は Java 側で渡した `Map<String, Object>` の JSON シリアライズ結果。型は `Record<string, unknown>` なので runtime guard 推奨
- `value` は session state に格納されている現在値（初回は `St.component` の `defaultValue`）
- `onChange(value)` 呼び出しで `widget_event` がサーバーへ送られ、次の rerun で Java 側の `St.component` が新値を返す

## ステップ 3: フロントレジストリーに登録する

`frontend/src/component-builtins.ts` で `registerComponent` を呼ぶ。`main.tsx` が起動時に無条件 import するため、ここに足すだけで全アプリで有効になる。

```ts
import { registerComponent } from './component-registry';
import { StarRating } from './components/StarRating';

registerComponent('star-rating', StarRating);
```

複数追加するときは行を増やすだけ。`name` 文字列が Java 側 `CustomComponent.name` と一致していることを必ず確認する。

## ステップ 4: 動作確認

```sh
# Java 側ビルド
./mvnw -pl core,server,examples,cli -am verify

# フロント側テスト
cd frontend && npm test
```

サーバー起動 → ブラウザーで `St.component(...)` を含むページを開くと、登録したレンダラーがマウントされる。未登録の `name` を呼ぶと `.component--unregistered` プレースホルダーにフォールバックする（design.md §9 参照）。

## 戻り値の型変換

`R` は Jackson でデコード可能な型なら何でも良い。サーバー側 `ComponentCodec.coerce` が次の優先順で変換する。

| ステップ | 処理 |
| --- | --- |
| 1 | session state の値が既に `R` のインスタンス → そのまま |
| 2 | `JsonNode` → `treeToValue` |
| 3 | その他（Map / List 等）→ `convertValue` |
| 4 | すべて失敗 → `defaultValue` を返す |

例: record `ColorRgb(int r, int g, int b)` を返したいときも `new CustomComponent<>("color-picker", ColorRgb.class)` で OK。フロントから `onChange({ r: 255, g: 128, b: 64 })` を送るとサーバーで自動デコードされる。

## SDK の vendor について

将来、外部リポジトリーで開発したコンポーネントを共有する場合は `frontend/src/sdk/` 系の SDK を npm 公開する予定（[ADR-0009](../adr/0009-vite-for-frontend) の Vite ベースで配布）。0.1.0 時点では vendor 運用とし、利用者は本リポジトリーの `frontend/` をフォーク or サブモジュールで取り込んで `component-builtins.ts` に登録する。

## チェックリスト

- [ ] Java と TS で同じ `name` 文字列を使ったか
- [ ] `args` のキー名と型は両側で一致しているか
- [ ] `onChange` で送る値が `CustomComponent<R>` の `R` にデコード可能か
- [ ] `frontend/src/component-builtins.ts` への登録忘れはないか
- [ ] `npm test` で React レンダラーのユニットテストを最低 1 件書いたか
