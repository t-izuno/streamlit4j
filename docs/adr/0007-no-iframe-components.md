# ADR-0007: カスタムコンポーネントは iframe 隔離を採らず in-process のみとする

- 状態: Accepted
- 日付: 2026-06-05
- 関連: `docs/design.md` §9、`docs/tasks/task.md` TASK-101〜106 / TASK-108

## Context

カスタムコンポーネントの実装方式に次の 2 案があった。

1. **iframe 隔離**: `<iframe sandbox="allow-scripts">` でホストし、`postMessage` でブリッジ
2. **in-process**: ホストの React ツリーに直接マウント

iframe 案は初期実装を一度試みた（TASK-101〜106）が、本決定により取り消した。

## Decision

**in-process 方式のみ**を提供する。iframe 隔離方式は採用しない。

主な理由:

- **本家動向との整合**: Streamlit V2 Components が iframe を廃止しホスト直マウントへ移行している。後発フレームワークとして同じ反省（隔離のコスト > 効果）を繰り返さない
- **sandbox の機能制約**: `allow-scripts` 単独 sandbox では opaque origin になり、
  外部 API 呼び出しを伴うコンポーネントの大半が機能しない。
  `allow-same-origin` を併用すると HTML 仕様上の明示的非推奨（sandbox 実質無効化）になる
- **運用負荷の不釣り合い**: `postMessage` 境界検証 / payload size /
  CSP `frame-src` 管理 / opaque origin の特殊扱い等を維持するコストが、
  得られる隔離価値（=ホスト Cookie / localStorage の保護）に見合わない
- **DX とパフォーマンス**: iframe 越しの同期点が消えるため、レンダリングが軽量で開発時のホットリロードも素直に効く
- **代替経路の存在**: 第三者コンポーネントが必要なら npm 依存と同様に vendor して in-process として配布する運用が成立する。動的な untrusted 第三者ロードは将来必要になった時点で別 ADR で再評価

## Consequences

良い影響:

- 本家 Streamlit V2 Components の方向性（iframe 廃止 / ホスト直マウント）と整合
- `allow-scripts` 単独 sandbox の opaque origin 制約から解放される（同一オリジン解放なしでは多くのライブラリーが機能しない）
- `postMessage` 境界検証 / payload size / CSP `frame-src` 管理などの運用コストを負わずに済む
- パフォーマンス: iframe 越しの同期点が消え、レンダリングが軽量

悪い影響:

- 第三者コードを `core` バンドルに混ぜないという信頼境界が必要。レジストリに登録されていない名前は `unregistered placeholder` にフォールバック
- 完全な untrusted 第三者コンポーネントを動的にロードしたいユースケースはサポート外。`npm` 依存と同様に vendor して in-process として配布する運用とする

## Alternatives Considered

- **iframe + `allow-scripts` のみ**: opaque origin で機能制約が大きい
- **iframe + `allow-scripts allow-same-origin`**: 実質サンドボックス無効化（HTML 仕様で明示的に非推奨）。Streamlit V1 が採用していたが V2 で廃止
- **両対応**: 二系統メンテナンスの負荷が初期リリースに見合わない
- **将来必要になったら別 ADR で再評価**: 本決定では追加せず、ユースケースが具体化した時点で改めて検討
