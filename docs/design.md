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
- **ports**: `core.port` に `SessionStore`, `EntrypointSource`, `Renderer` をインターフェース定義
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
- ノードは `kind` / `id` / `props` / `children` を持つ不変表現とする
  （ノードのプロトコル仕様は `specification.md` 3 章を参照）

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

## 9. カスタムコンポーネントのセキュリティー方針

### 9-1. インプロセス component

- 同梱バンドルに登録される first-party 部品。レジストリに登録された名前を信頼する
- 第三者コードを混ぜないため特別な隔離は行わず、通常の React レンダリングで実行
- Java 側 `St.registerComponent(spec)` と TS 側 `registerComponent(name, renderer)` の
  両方で登録されていない限り unregistered プレースホルダーへ落とす

### 9-2. iframe 隔離 component

- 第三者 component は `<iframe sandbox="allow-scripts" src={...}>` でホストする
- `allow-scripts` 単独のため、iframe のオリジンは opaque (`'null'`) となり、
  親ドキュメントの Cookie / localStorage には到達できない
- 親→iframe の `postMessage` は `targetOrigin` を iframe `src` の origin に固定する。
  ただし opaque origin (`'null'`) のときは `'*'` で対応する（iframe 自体は親が生成・
  保持する `contentWindow` で一意に特定できる）
- iframe→親の受信は次の 3 段階で検証する:
  1. `event.source === iframeRef.current.contentWindow`（他ウィンドウからの偽装拒否）
  2. `event.origin === 'null'` または `event.origin === expectedOrigin`
     （sandbox 緩和時の origin 検証）
  3. `data.name` が当該 component の name と一致（メッセージの誤配送防止）

### 9-3. CSP 方針

- 推奨される本番 CSP ヘッダー / `<meta http-equiv="Content-Security-Policy">`:
  - `default-src 'self'`
  - `script-src 'self'`（インラインスクリプト不可）
  - `style-src 'self' 'unsafe-inline'`（CSS-in-JS / Vite の挙動許容）
  - `connect-src 'self' ws: wss:`（WebSocket 接続）
  - `frame-src <iframe component の origin リスト>`
- SPA バンドル自体はインラインスクリプトを含まないため nonce は不要
- 将来サーバーレンダリングや動的 HTML を生成する場合に備え、サーバー側で
  リクエストごとに 256bit nonce を発行し、`<script nonce=...>` と
  `script-src 'nonce-<value>'` の双方に注入する戦略を採用する
- nonce の保持はリクエストスコープ。セッションを跨いだ再利用は禁止する
- 本書は方針の文書化に留め、CSP ヘッダーの自動付与は組み込み側（Spring Security
  または埋め込みサーバー）の設定責務とする

## 10. 未決事項（要決定）

以下は ADR として確定し、`tasks/task.md` の TASK-109 で扱う。

1. **終端 API の命名**: ビルダーの終端を `show()` / `render()` / `use()` の
   どれに統一するか
2. **通信プロトコル**: v1 から MessagePack を採用するか、JSON 先行で後から
   差し替えるか
3. **キャッシュ指定方式**: アノテーション主体かラッパー関数主体か
4. **GraalVM 対応の時期**: v1 に含めるか v1.x に回すか（reflection 設定の負荷）
5. **ライセンス**: 確定済み（TASK-110）。**MIT** を採用。利用者側の義務を最小化し、
   サブライセンスや他ライセンスとの組み合わせを容易にすることを優先。Apache-2.0 は
   特許明示権で勝るが、本プロジェクトでは特許防衛優先度が低いため不採用
6. **マルチページの既定方式**: 規約ベースと明示登録のどちらを既定とするか
