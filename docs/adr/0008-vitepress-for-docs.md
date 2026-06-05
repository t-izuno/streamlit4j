# ADR-0008: ドキュメントサイトに VitePress を採用する

- 状態: Accepted
- 日付: 2026-06-05
- 関連: `docs/.vitepress/config.ts`、`docs/package.json`

## Context

公式ドキュメントサイトの SSG（Static Site Generator）候補は次が代表的。

1. **VitePress**: Vue/Vite ベース。Markdown 中心、設定最小、Vite の HMR を利用したライブプレビュー
2. **Docusaurus**: Meta 製。React ベース、プラグインエコシステム豊富、Algolia 等の検索統合容易
3. **MkDocs (Material)**: Python ベース、テーマが成熟、Markdown のみで完結
4. **mdBook**: Rust 製、軽量、テンプレート自由度低

streamlit4j は Java 製サーバー + Vite ベースの React フロントエンドという二層構造。ドキュメントは API リファレンス（Javadoc 自動生成）と利用ガイド（手書き Markdown）の併存が前提。

## Decision

ドキュメントサイトの SSG として **VitePress** を採用する。

主な理由:

- **フロント側技術スタックとの一致**: SPA 本体が Vite + TypeScript で
  構築されている（[ADR-0009](./0009-vite-for-frontend.md)）ため、
  ドキュメントも同じ Vite ベースであれば、ビルド設定 / Node 依存
  / TypeScript 設定の知識を共有できる
- **Markdown ファーストの編集体験**: 設計書 / 仕様書 / ADR / リファレンスがいずれも Markdown 主体で、Vue コンポーネントを書かなくても 95% の用途を満たせる
- **ビルド速度と HMR**: 数百ページ規模でもビルド < 数秒、開発時の HMR が高速で、執筆フィードバックループが短い
- **設定ボリュームの少なさ**: `config.ts` 1 ファイルでサイドバー / ナビ / テーマ / 検索（local）を完結できる。Docusaurus に比べて初期セットアップが軽い
- **依存規模**: `vitepress` 単体で完結。Docusaurus の React + 多数プラグインに比べてフロント依存ツリーが浅く、メンテ負荷が低い
- **検索機能**: ローカル検索を組み込みで提供。初期版で外部 SaaS（Algolia DocSearch 等）を契約しなくてよい
- **テーマ拡張性**: `theme/index.ts` と CSS 変数（`--vp-c-brand-*`）の上書きでブランドカラー（[ADR-0005 と独立した視覚アイデンティティー方針、design.md §11]）を素直に反映できる

## Consequences

良い影響:

- フロントエンドエンジニアがそのままドキュメント保守に入れる（言語 / ビルドツール / TS 設定が同じ）
- 開発時に `vitepress dev` の HMR で執筆 → プレビューがリアルタイム
- 軽量。CI でのビルド時間がリリース速度に影響しない
- VitePress 本体の更新追従が容易（Vite のメジャーアップに引きずられにくい）

悪い影響:

- Vue ベースのため、複雑な動的コンポーネントを書く場合は Vue の知識が要る（本リポジトリは Markdown 主体のため影響は小さい）
- プラグインエコシステムは Docusaurus に比べて小さい。検索 / 国際化 / バージョニング等で凝った要件が出た場合、自前実装の余地が増える
- Algolia DocSearch などの SaaS 統合は別途設定が必要（必要になった時点で導入）

## Alternatives Considered

- **Docusaurus**: 機能豊富で多言語化 / バージョニングが標準だが、初期セットアップが重く React 依存（フロントの Vite/React と別系統の React 設定が必要になる場合あり）。本プロジェクトのドキュメント規模では過剰
- **MkDocs (Material)**: テーマ品質は高いが Python ランタイムを CI に追加する必要があり、フロント/Java 主体のプロジェクトに新しい言語層を持ち込みたくない
- **mdBook**: 軽量で速いが、テーマカスタマイズの自由度が低くブランドカラー / ロゴ反映が難しい。検索 / 多言語化の標準サポートも弱い
- **Jekyll / Hugo**: 機能は十分だが Ruby / Go ランタイムが追加で必要。VitePress に対する優位がない
