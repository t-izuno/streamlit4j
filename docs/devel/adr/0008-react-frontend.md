# ADR-0008: フロント UI フレームワークに React 18 を採用する

- 状態: Accepted（遡及記録）
- 日付: 2026-06-05
- 関連: `frontend/package.json`（`"react": "^18.3.1"`）、`design.md` §8

## Context

streamlit4j SPA は WebSocket で受け取るレンダーツリー（`kind` / `id` / `props` / `children`）を逐次描画する。UI フレームワークの候補を 2026 年 6 月時点で再評価する。

### 候補

| 候補 | レンダリングモデル | エコシステム規模 | streamlit4j の要件への適合 |
| --- | --- | --- | --- |
| React 18 | 仮想 DOM + 並行レンダリング | 最大（コンポーネントライブラリー / 型定義 / ツール） | レンダーツリーを props で渡すモデルが素直 |
| Vue 3 | リアクティブ + 仮想 DOM | 大（ただし React より小） | テンプレート構文が外部スキーマと相性しづらい |
| Svelte 5 | コンパイル時にバニラ JS 化 | 中（拡大中） | runtime が薄く魅力的だが、外部から動的に渡されるツリー描画はリアクティビティーの組み立てが煩雑 |
| Lit | Web Components / lit-html | 小 | Shadow DOM がデフォルトでスタイル隔離が強い半面、本家 Streamlit を含む既存エコシステムから乖離 |

### 周辺事実（一次情報）

- 本家 Streamlit のコアアプリは **React + webpack（CRA）**。webpack → Vite
  の移行要望は [streamlit/streamlit#6588](https://github.com/streamlit/streamlit/issues/6588)
- 本家 Streamlit Components V2（新版）の公式推奨スタックは
  **React 18.3 + TypeScript 5.8 + Vite 6.4**。出典は
  [Streamlit Docs — package-based components](https://docs.streamlit.io/develop/concepts/custom-components/components-v2/package-based)
- 2026 年時点で React の利用率は約 40%（Strapi 調べ）、依然首位

## Decision

フロント UI フレームワークに **React 18**（`react@18.3.1`）を採用する。

主な理由:

- **本家 Streamlit との API 互換性確保が容易**: Streamlit Components V2 の公式推奨が React 18 で、将来 V2 互換のコンポーネントを取り込む / vendor する際に手戻りが小さい
- **エコシステムの厚さ**: dataframe / chart / 仮想スクロール / アクセシビリティーなど streamlit4j で必要になりうる UI 部品が React ライブラリーとして揃っている。Lit / Svelte ではエコシステム規模が劣る
- **動的レンダーツリー描画との相性**: `props.children` ベースの再帰描画が、サーバーから流れてくる任意ツリーの描画と素直にマッピングする。リアクティブモデル（Vue / Svelte）はテンプレート静的解析が前提のため動的ツリーには工夫が必要
- **TypeScript 統合の成熟度**: 型定義 / ESLint プラグイン / IDE サポートのいずれも React + TS が最厚
- **テスト基盤の整備**: React Testing Library + Vitest（[ADR-0009](./0009-vite-for-frontend.md)）でユニットテストを書く構成が業界標準

## Consequences

良い影響:

- 本家 Streamlit のコンポーネント実装を参考にしやすい（API / フック / ライフサイクルが概ね共通）
- 仮想 DOM のオーバーヘッドはあるが、React 18 の concurrent rendering により実用上のレイテンシーは十分
- 部品ライブラリーの選択肢が広く、独自実装を避けられる

悪い影響:

- ランタイムサイズが Svelte / Lit より大きい（gzip 後 ~45KB の React + ReactDOM）。バンドルサイズに敏感な利用者には不利
- 仮想 DOM のメンタルモデル習得コストが Vue より高い

## Alternatives Considered

- **Vue 3**: 学習曲線がゆるやかで魅力的だが、本家 Streamlit との互換性経路が薄く、共有可能なコンポーネント資産が少ない
- **Svelte 5**: バンドルサイズの利点はあるが、エコシステム規模 / 本家との互換性で React に劣る
- **Lit（Web Components）**: ランタイム極小で標準寄りだが、エコシステム規模が小さく、本家 Streamlit V2 が React 推奨であることと整合しない
