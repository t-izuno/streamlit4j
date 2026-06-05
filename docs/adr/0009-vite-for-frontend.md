# ADR-0009: フロントエンドビルドツールに Vite を採用する

- 状態: Accepted（遡及記録）
- 日付: 2026-06-05（記録日。実採用はリポジトリ初期化時の commit `a2e9645` に遡る）
- 関連: `frontend/vite.config.ts`、`frontend/package.json`、`design.md` §8

## Context

streamlit4j の SPA（`frontend/`）をビルドするツールチェーンの候補を 2026 年 6 月時点で再評価する。

### 現状の業界スナップショット（一次情報）

- **本家 Streamlit のコアアプリ**: 依然として **Create React App（CRA）
  = webpack + react-scripts + Craco** を使用している。Vite への移行要望は
  GitHub issue [streamlit/streamlit#6588](https://github.com/streamlit/streamlit/issues/6588)
  で受理されているが、未完了
- **本家 Streamlit Components V2（新版）**: 公式推奨スタックは
  **Vite + React + TypeScript**。`docs.streamlit.io` の package-based
  components ガイドが `vite.config.ts` の最小例を提示
- **CRA**: 2023 年に React 公式が deprecated を宣言し、新規プロジェクトでは
  推奨されていない

### 候補

| 候補 | dev サーバー / HMR | 設定量 | 採用状況（2026） |
| --- | --- | --- | --- |
| Vite | esbuild + Rollup、ESM ネイティブ HMR | 小（`vite.config.ts` 1 ファイル） | React 公式の新規推奨、Streamlit Components V2 公式推奨 |
| webpack | dev-server + HMR、起動遅 | 大（plugin / loader 構成） | 本家 Streamlit コアが使用中だが Vite 移行要望が出ている |
| Parcel | zero-config | 極小 | 新規採用は減少傾向 |
| esbuild + 自前 | esbuild バンドル、自前 dev サーバー | 中〜大 | ライブラリー向けでは採用例あり |
| Rollup（単独） | dev サーバーなし | 中 | アプリ向けには不向き（Vite の内部実装に組み込まれている） |

## Decision

フロントエンドビルドツールに **Vite** を採用する（リポジトリ初期化時の決定を遡及的に ADR 化）。

主な理由:

- **本家 Streamlit V2 推奨**: Streamlit Components V2 の公式推奨スタックが
  Vite + React + TypeScript であり、本家 OSS が示す将来方向と一致する。
  これは「採用根拠の 1 つ」であって唯一ではない
- **webpack/CRA の陳腐化**: React 公式が CRA を deprecated 化（2023）、本家
  Streamlit でも webpack → Vite 移行要望（#6588）が活発。新規プロジェクトが
  webpack を選ぶ動機は弱い
- **HMR と dev サーバーの速度**: ESM ネイティブ配信で、Streamlit 互換の対話的
  アプリ開発で重要な「変更 → 反映」のレイテンシーが短い
- **設定ボリュームの少なさ**: `vite.config.ts` 1 ファイルで React プラグイン
  と TypeScript およびビルド出力先まで完結する。webpack の loader / plugin
  構成 / babel 設定の組み合わせよりメンテ対象が少ない
- **テスト基盤の統一**: Vitest が Vite の設定をそのまま流用でき、テスト用に
  別 transpiler / module 解決を維持しなくてよい
- **VitePress との親和性**: ドキュメントサイト
  [ADR-0008](./0008-vitepress-for-docs.md) が同じ Vite ベースのため、
  ビルド・依存・型定義の知識を共有できる

## Consequences

良い影響:

- 開発時の改修サイクルが短い（本リポジトリでは `vite build` が ~400ms）
- Vitest を介してテストとビルドの設定を統一でき、二重メンテを回避
- VitePress と同じ Vite メジャーラインに追従でき、依存更新が一括で済む
- 本家 Streamlit Components V2 と同じスタックなので、将来 V2 互換のコンポーネントを取り込みやすい

悪い影響:

- 本家 Streamlit コア（webpack）と一致しないため、コア側の dev tooling 改善は直接転用できない
- ESM 非対応の旧来 npm パッケージとの相性で稀に解決エラーが出る（個別 alias / `optimizeDeps` 設定で回避）
- Vite メジャーアップ（v5 → v6 等）の追従が必要

## Alternatives Considered

- **webpack**: 本家 Streamlit コアが現在使用しているが、本家自身が移行要望を出している（#6588）。新規プロジェクトで採る積極理由が見当たらない
- **Rollup（単独）**: アプリ向けの dev サーバー / HMR が薄い。Vite の内部実装としてはすでに使われている
- **Parcel**: zero-config の良さはあるが、React 18 / TypeScript 対応の追従ペースが Vite より遅く、コミュニティー規模も小さい
- **esbuild + 自前 dev サーバー**: 最速だが HMR / アセット / 静的解析を自作する負担が大きく、フレームワーク開発本体の進捗を奪う

## 参考リンク

- [streamlit/streamlit#6588 — Replace Webpack with Vite bundler](https://github.com/streamlit/streamlit/issues/6588)
- [Streamlit Docs — Package-based components (V2)](https://docs.streamlit.io/develop/concepts/custom-components/components-v2/package-based)
