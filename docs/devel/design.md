# streamlit4j 設計書

> ステータス: ドラフト v0.1
>
> 関連ドキュメント: 要件は `requirements.md`、仕様は `specification.md`、実行計画は `tasks/task.md`

本書は streamlit4j の内部設計（アーキテクチャー・アルゴリズム・技術選定）を定義する。
外部からの API サーフェスやプロトコル詳細は `specification.md` を参照する。

## 0. アーキテクチャースタイル

採用方針: **Clean Architecture（lite）** + framework-free core。

依存方向は内向き（Dependency Rule）のみを許可する。

```text
                       +-------------------+
   (adapters / impl)   |  io.streamlit4j.  |
   外部から見える層     |  server,          |
                       |  spring-boot-     |
                       |  starter, cli     |
                       +---------+---------+
                                 | depends on (inward)
                                 v
                       +---------+---------+
                       | core.bootstrap    |   <-- 合成ルート（DI のみ）
                       +---------+---------+
                                 |
                                 v
                       +---------+---------+
                       | core.application  |   <-- ユースケース
                       +---------+---------+
                                 |
                                 v
                       +---------+---------+
                       | core.port         |   <-- 境界（インターフェース）
                       +---------+---------+
                                 |
                                 v
                       +---------+---------+
                       | core.domain,      |   <-- エンティティー、DTO
                       | core.protocol     |       （peers: domain は protocol を参照可）
                       +-------------------+
```

採用したもの:

- **framework-free core**: `core` パッケージ全体が Web フレームワーク・サーブレット・DI コンテナーに依存しない
- **ports**: `core.port` に `SessionStore`, `EntrypointSource`, `Renderer` をインターフェイス定義
- **use cases**: `core.application` に `StartSession`, `ProcessWidgetEvent`
- **composition root**: `core.bootstrap.Bootstrap` のみが具象クラスを new する
- **internal infrastructure**: `core.runtime` に `ScriptRunner`, `RenderContext`, `WidgetIds`,
  `InMemorySessionStore`（ポートのデフォルト実装）

意図的に**採用しなかった**もの:

- DDD 戦術パターン（Aggregate / Repository / Domain Service）: 現状の `Session` は
  技術構造体としても十分機能するため、ドメインモデリングは過剰
- Onion Architecture の同心円構造: パッケージ階層で十分表現可能
- 抽象 Factory / Provider: 合成ルートを 1 つに絞ることで DI コンテナー不要

依存ルールの強制:

- ArchUnit の `CoreArchitectureTest` が package 間依存方向を検査
- ArchUnit の `ServerArchitectureTest` が `server` は `core.runtime` に直接依存しないことを検査
- frontend は `dependency-cruiser` がモジュール依存を検査

## 1. 全体アーキテクチャー

```text
+---------------------------+        WebSocket        +------------------------+
|        Browser            |  <-------------------->  |   streamlit4j Server   |
|  (React + TS SPA)         |    render_delta /        |   (embedded HTTP/WS)   |
|  - 要素ツリー描画          |    widget_event          |                        |
|  - イベント送信            |                          |  +------------------+  |
+---------------------------+                          |  | Session Manager  |  |
                                                       |  +--------+---------+  |
                                                       |           |            |
                                                       |  +--------v---------+  |
                                                       |  | Script Runner    |  |
                                                       |  | (virtual thread) |  |
                                                       |  |  -> User App     |  |
                                                       |  +--------+---------+  |
                                                       |           |            |
                                                       |  +--------v---------+  |
                                                       |  | Render Tree+Diff |  |
                                                       |  +------------------+  |
                                                       +------------------------+
```

## 2. ランタイムと実行モデル

### 2.1 セッション単位の実行

- セッションごとに仮想スレッド上で「スクリプトランナー」を回す
- 再実行は 1 セッション内で直列化し、実行中イベントはキューに積む
- セッション間は仮想スレッドで並行実行する

### 2.2 レンダーコンテキスト束縛

- ウィジェット呼び出し時のレンダーコンテキスト束縛は、v1 では `ThreadLocal` ベースで
  実装する
- 将来 `ScopedValue` への移行を検討する

### 2.3 ウィジェット ID 生成

- 自動ウィジェット ID は `StackWalker` による呼び出し位置 + 引数ハッシュで導出する
- `key` 明示時はそれを最優先する

### 2.4 ウィジェット呼び出しの内部動作

- ウィジェット呼び出しはレンダーコンテキストに要素を登録する
- セッション状態から現在値を返す
- ウィジェット値は ID をキーに再実行間で保持する

## 3. レンダーツリーと差分計算

- 各再実行は「キー付き要素ノードのツリー」を生成する
- 直前ツリーとの keyed diff で最小パッチ列を作る
- 生成したパッチ列を `render_delta` で送信する
- ノードは `kind` / `id` / `props` / `children` を持つ不変表現とする。
  ノードのプロトコル仕様は `specification.md` 3 章を参照

## 4. フロントエンド設計

### 4.1 採用案

- React + TypeScript の SPA を Vite でビルドする
- ビルド成果物を静的アセットとして jar に同梱する

### 4.2 採用根拠

- 豊富なウィジェット描画
- Streamlit 由来のカスタム component 期待値との整合
- エコシステムの厚さ

### 4.3 代替案と保留理由

- Lit（Web Components）による軽量・フレームワーク非依存路線
- 埋め込み時のフットプリントを下げられるが、リッチ部品の実装コストが上がる
- v1 では React/TS を採用し、Web Components 路線は将来の最適化オプションとして保留

## 5. 統合レイヤー設計

### 5.1 モジュール分担

- `core`: サーブレットや特定 Web フレームワークに依存しない純粋な実行エンジン
- `server`: 軽量 HTTP/WS サーバー（組み込み Jetty / Undertow 等）で `core` を公開
- `spring-boot-starter`: auto-configuration で指定パスにマウントし、ホストの
  Security / Session に委譲する
- `cli`: `server` をラップし、JBang で単一ファイルを起動する

### 5.2 サーバー選定

- 組み込み Jetty または Undertow から選定する
- 軽量・埋め込み容易を基準とする

## 6. プロトコル設計方針

- v1 はデバッグ性とツール親和性を優先し JSON エンベロープを採用
- 将来 MessagePack / Protobuf によるバイナリ最適化を差し替え可能にする
- メッセージ種別はバージョン付きエンベロープで明示する
- 具体的なエンベロープ構造とメッセージ種別は `specification.md` 2 章参照

## 7. 開発体験設計

- ソース変更を検知し、ブラウザー自動リロードを発火する開発ループ
- エラー時はスタックトレース付きで画面表示
- CLI コマンド: `streamlit4j run App.java`

## 8. 技術選定と根拠

| 領域 | 推奨 | 根拠 | 代替案 |
| --- | --- | --- | --- |
| JDK | 21 LTS | 仮想スレッド・records・sealed・pattern matching | 25 LTS 待ち |
| 並行 | 仮想スレッド | セッション単位の安価な並行実行 | プラットフォームスレッドプール |
| 通信 | WebSocket + JSON | 双方向・デバッグ容易・ツール親和 | Protobuf / MessagePack |
| サーバー | 組み込み Jetty/Undertow | 軽量・埋め込み容易 | Netty 直叩き |
| フロント | React + TypeScript | 部品の厚さ・component 整合 | Lit（Web Components） |
| フロントビルド | Vite | 高速・SPA 標準 | webpack |
| ビルド | Maven | OSS 標準・Maven Central 公開との整合・エコシステム成熟 | Gradle (Kotlin DSL) |
| 配布 | Maven Central + JBang | OSS 標準 + CLI 配布の容易さ | GitHub Packages |
| 起動最適化 | GraalVM native image（中期） | 高速起動・省メモリーの差別化 | CDS / AppCDS |

## 9. カスタムコンポーネントの方針

- カスタムコンポーネントは「インプロセス」方式のみ提供する。同梱バンドルに登録される
  first-party 部品はレジストリに登録された名前を信頼し、通常の React レンダリングで実行する
- Java 側 `St.registerComponent(spec)` と TS 側 `registerComponent(name, renderer)` の
  両方で登録されていない限り unregistered プレースホルダーへ落とす
- iframe 隔離方式は採用しない。理由は次の 2 点:
  - **本家 Streamlit の動向**: V2 Components 以降は iframe を廃止し、ホストアプリへの
    直接マウントへ移行している。隔離より統合の方向で標準化が進んでいる
  - **DX とパフォーマンスの不利**: iframe 越しの postMessage 連携は同期点が増え、
    境界検証 / payload size / CSP `frame-src` 管理など運用コストが大きい一方、
    `allow-scripts` 単独の sandbox では同一オリジン解放が無くカスタム要件によっては
    実現できないケースが多い
- 第三者コンポーネントを取り込む必要が出た場合は npm 依存と同じ扱いとし、
  ソースを vendor したうえで in-process として配布する運用を想定する

## 10. 決定済み事項

### 10-1. アーキテクチャー判断（ADR）

アーキテクチャー境界・非機能特性・外部依存・セキュリティーモデルに影響する判断は `docs/devel/adr/` 配下に ADR として記録する。

| ID | タイトル |
| --- | --- |
| [ADR-0002](./adr/0002-json-over-messagepack.md) | プロトコルは JSON（Jackson）を採用する |
| [ADR-0004](./adr/0004-graalvm-deferred.md) | GraalVM ネイティブ対応は v1.x 以降へ繰り延べる |
| [ADR-0005](./adr/0005-explicit-page-registration.md) | マルチページは規約ベースではなく明示登録を既定とする |
| [ADR-0006](./adr/0006-mit-license.md) | ライセンスは MIT とする |
| [ADR-0007](./adr/0007-no-iframe-components.md) | カスタムコンポーネントは iframe 隔離を採らず in-process のみとする |

### 10-2. API 設計指針（実装レベル）

ADR で扱うほどではないが API シグネチャーとして固定する選択。

- **終端 API**: `St.title("...")` の静的メソッドを呼んだ時点で `RenderContext`
  へ即時 emit する。ビルダー方式（`.show()` / `.render()` / `.use()`）は
  採用しない。Streamlit Python の DX を踏襲し、中間オブジェクト生成を避ける
- **キャッシュ指定**: `St.cacheData(key, ttl, Supplier<T>)` /
  `St.cacheResource(key, Supplier<T>)` のラッパー関数で提供する。
  アノテーション（`@StCache` 等）は採用しない。AOP / バイトコード変換に
  依存せず、`core` モジュールを IoC コンテナーから独立させる目的。
  [ADR-0004](./adr/0004-graalvm-deferred.md) の GraalVM 整合性とも親和する

## 11. 視覚アイデンティティー

### 11-1. カラーパレット

ブランド主軸は teal を採用し、Streamlit の赤系（`#FF4B4B`）とは明確に区別する。データ表示と相性が良く、ダーク背景でも視認性を維持できる色相を選定。

| Token | Light | Dark | 用途 |
| --- | --- | --- | --- |
| `--color-bg` | `#ffffff` | `#0f1a1f` | 背景 |
| `--color-fg` | `#0f172a` | `#f1f5f9` | 本文 |
| `--color-accent` | `#0d9488` | `#2dd4bf` | プライマリーアクション・リンク・ロゴ |
| `--color-accent-strong` | `#0f766e` | `#5eead4` | ホバー / 強調 |
| `--color-muted` | `#64748b` | `#94a3b8` | キャプション・サブテキスト |
| `--color-border` | `#e2e8f0` | `#1e293b` | 境界線 |

定義は `frontend/src/styles.css` と `docs/.vitepress/theme/brand.css` の 2 箇所で同色相を共有する。

### 11-2. ロゴ

ロゴは Streamlit の swirl 模様を流用せず、横方向に並ぶ 3 本のバーで「並列データストリーム」を抽象化したシンボルマークを採用する。

- 正方形ベース（64x64 viewBox）の角丸（rx=12）背景
- 上下に細い短バー、中央に太い長バー（中央寄せ）を白で描画
- ライト用は teal `#0d9488` 背景、ダーク用は `#2dd4bf` 背景に `#0f1a1f` のバー

ファイル一覧:

- `docs/public/streamlit4j-logo.svg`（ライト用）
- `docs/public/streamlit4j-logo-dark.svg`（ダーク用）
- `docs/public/favicon.svg`
- `frontend/public/` にも同ファイルを配置

SVG は本リポジトリの MIT ライセンス下で配布する（商用利用可）。
