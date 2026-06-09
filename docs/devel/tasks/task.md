# TASKS

マイルストーン: M1
ゴール: docs/devel/requirements.md の v1 スコープ全体（実行エンジン・コア・互換拡充・統合・公開）を満たし streamlit4j 0.1.0 を Maven Central に公開する

## ワークフロールール

- タスク着手時にステータスを 🚧 に更新する
- タスク完了時にステータスを ✅ に更新する
- DependsOn のタスクがすべて ✅ でないタスクには着手しない

## ステータス表記ルール

| Status | 意味 |
| ---- | ----- |
| ⏳ | 未着手、TODO |
| 🚧 | 作業中、IN_PROGRESS |
| 🧪 | 確認待ち、REVIEW |
| ✅ | 完了、DONE |
| 🚫 | 中止、CANCELLED |

## タスク一覧

| ID | Status | Summary | DependsOn |
| ---- | ---- | ---- | ---- |
| TASK-001 | ✅ | Maven でマルチモジュール骨格を構築する | - |
| TASK-002 | ✅ | JDK 21 ツールチェーンと共通ビルド規約を設定する | TASK-001 |
| TASK-003 | ✅ | JUnit 5 ベースの Java 単体テスト基盤を構築する | TASK-002 |
| TASK-004 | ✅ | Spotless / Checkstyle / ArchUnit で Java 静的解析・アーキテクチャー検査を設定する | TASK-002 |
| TASK-005 | ✅ | JaCoCo でカバレッジ計測とレポート出力を構築する | TASK-003 |
| TASK-006 | ✅ | Vitest と Testing Library でフロント単体テスト基盤を構築する | TASK-001 |
| TASK-007 | ✅ | ESLint / Prettier / dependency-cruiser でフロント静的解析・整形・依存検査を設定する | TASK-001 |
| TASK-008 | ✅ | Playwright で E2E テスト基盤を構築する | TASK-006 |
| TASK-009 | ✅ | GitHub Actions で push/PR の CI パイプラインを構築する | TASK-003,TASK-004,TASK-005,TASK-006,TASK-007 |
| TASK-010 | ✅ | OWASP Dependency-Check で脆弱性スキャンを CI に組み込む | TASK-009 |
| TASK-011 | ✅ | WebSocket JSON エンベロープのスキーマを定義する | TASK-002 |
| TASK-012 | ✅ | レンダーノードの不変データモデルを実装する | TASK-002 |
| TASK-013 | ✅ | session_init / render_delta / widget_event の DTO を実装する | TASK-011,TASK-012 |
| TASK-014 | ✅ | JSON シリアライザー/デシリアライザーを実装する | TASK-013 |
| TASK-015 | ✅ | 仮想スレッドで動くスクリプトランナーを実装する | TASK-002 |
| TASK-016 | ✅ | ThreadLocal ベースのレンダーコンテキスト束縛を実装する | TASK-012,TASK-015 |
| TASK-017 | ✅ | 呼び出し位置由来の決定的ウィジェット ID 生成を実装する | TASK-016 |
| TASK-018 | ✅ | St.title / St.markdown / St.write の表示系 API を実装する | TASK-016 |
| TASK-019 | ✅ | St.slider 入力系 API と値返却ロジックを実装する | TASK-017 |
| TASK-020 | ✅ | 単一セッションのライフサイクル管理を実装する | TASK-015 |
| TASK-021 | ✅ | 組み込み HTTP/WS サーバの起動と接続受付を実装する | TASK-014,TASK-020 |
| TASK-022 | ✅ | widget_event 受信から再実行までの直列処理を実装する | TASK-019,TASK-021 |
| TASK-023 | ✅ | React + Vite + TypeScript のフロント雛形を構築する | TASK-007,TASK-011 |
| TASK-024 | ✅ | WebSocket クライアントとメッセージ受発信を実装する | TASK-013,TASK-023 |
| TASK-025 | ✅ | title / markdown / write のフロント描画を実装する | TASK-024 |
| TASK-026 | ✅ | slider のフロント描画と値変更イベント送信を実装する | TASK-024 |
| TASK-027 | ✅ | フロント差分適用と全置換フォールバックを実装する | TASK-025,TASK-026 |
| TASK-028 | ✅ | PoC サンプル App を作成し E2E 動作を確認する | TASK-022,TASK-027,TASK-008 |
| TASK-029 | ✅ | セッションマネージャーと生成/タイムアウト/破棄を実装する | TASK-020 |
| TASK-030 | ✅ | セッションライフサイクルリスナー API を実装する | TASK-029 |
| TASK-031 | ✅ | 仮想スレッドによるセッション間並行実行を実装する | TASK-029 |
| TASK-032 | ✅ | セッション内直列再実行とイベントキューを実装する | TASK-031 |
| TASK-033 | ✅ | SessionState の Key-Value ストアを実装する | TASK-029 |
| TASK-034 | ✅ | SessionState の型付きアクセサー API を実装する | TASK-033 |
| TASK-035 | ✅ | ウィジェット値を ID キーで再実行間に保持する仕組みを実装する | TASK-033 |
| TASK-036 | ✅ | St.rerun / St.stop の制御 API を実装する | TASK-032 |
| TASK-037 | ✅ | レンダーツリーの keyed diff アルゴリズムを実装する | TASK-012 |
| TASK-038 | ✅ | render_delta の op/path/node パッチ生成を実装する | TASK-037 |
| TASK-039 | ✅ | フロント差分適用ロジックを keyed diff 対応に拡張する | TASK-027,TASK-038 |
| TASK-040 | ✅ | text_input / number_input ウィジェットを実装する | TASK-035 |
| TASK-041 | ✅ | selectbox / multiselect ウィジェットを実装する | TASK-035 |
| TASK-042 | ✅ | checkbox / radio / button ウィジェットを実装する | TASK-035 |
| TASK-043 | ✅ | date_input ウィジェットを実装する | TASK-035 |
| TASK-044 | ✅ | header / metric の表示系要素を実装する | TASK-018 |
| TASK-045 | ✅ | dataframe / table 表示要素を実装する | TASK-018 |
| TASK-046 | ✅ | json / code 表示要素を実装する | TASK-018 |
| TASK-047 | ✅ | columns / container レイアウト要素を実装する | TASK-016 |
| TASK-048 | ✅ | expander / tabs レイアウト要素を実装する | TASK-047 |
| TASK-049 | ✅ | sidebar レイアウト領域を実装する | TASK-047 |
| TASK-050 | ✅ | empty 相当のプレースホルダー API を実装する | TASK-047 |
| TASK-051 | ✅ | ファイルアップロードのストリーミング受信を実装する | TASK-032 |
| TASK-052 | ✅ | file_uploader ウィジェットを実装する | TASK-051 |
| TASK-053 | ✅ | アップロードのサイズ上限と MIME 検証を実装する | TASK-051 |
| TASK-054 | ✅ | error メッセージ送信とフロントのスタックトレース表示を実装する | TASK-038 |
| TASK-055 | ✅ | 構造化ログ（セッション ID・再実行 seq・所要時間）を実装する | TASK-032 |
| TASK-056 | ✅ | アクティブセッション数等のメトリクスを実装する | TASK-029 |
| TASK-057 | ✅ | ソース変更検知とブラウザ自動リロード機構を実装する | TASK-038 |
| TASK-058 | ✅ | CLI モジュールと streamlit4j run コマンドを実装する | TASK-057 |
| TASK-059 | ✅ | JBang による単一 .java ファイル起動を実装する | TASK-058 |
| TASK-060 | ✅ | フロントで全要素ノードの描画コンポーネントを整備する | TASK-040,TASK-041,TASK-042,TASK-043,TASK-044,TASK-045,TASK-046,TASK-048,TASK-049 |
| TASK-061 | ✅ | ページ宣言の規約ベース自動探索機構を実装する | TASK-016 |
| TASK-062 | ✅ | St.pages による明示登録 API を実装する | TASK-016 |
| TASK-063 | ✅ | ページ間ナビゲーション UI を実装する | TASK-061,TASK-062 |
| TASK-064 | ✅ | URL ディープリンクとルーティング同期を実装する | TASK-063 |
| TASK-065 | ✅ | form コンテナーと再実行抑制機構を実装する | TASK-032 |
| TASK-066 | ✅ | form_submit_button と一括値確定を実装する | TASK-065 |
| TASK-067 | ✅ | データキャッシュのアノテーション指定を実装する | TASK-016 |
| TASK-068 | ✅ | データキャッシュのラッパー関数指定を実装する | TASK-067 |
| TASK-069 | ✅ | 引数ハッシュによるキャッシュキー生成を実装する | TASK-067 |
| TASK-070 | ✅ | データキャッシュの TTL と無効化を実装する | TASK-069 |
| TASK-071 | ✅ | リソースキャッシュとスレッドセーフ共有を実装する | TASK-067 |
| TASK-072 | ✅ | line_chart チャート要素を実装する | TASK-018 |
| TASK-073 | ✅ | bar_chart チャート要素を実装する | TASK-018 |
| TASK-074 | ✅ | area_chart チャート要素を実装する | TASK-018 |
| TASK-075 | ✅ | scatter_chart チャート要素を実装する | TASK-018 |
| TASK-076 | ✅ | テーマ切替機構（ライト/ダーク）を実装する | TASK-023 |
| TASK-077 | ✅ | カスタムカラーパレット適用を実装する | TASK-076 |
| TASK-078 | ✅ | アクセシビリティ（コントラスト/フォーカス）を整備する | TASK-076 |
| TASK-079 | ✅ | キーボード操作対応をフロント全体に適用する | TASK-078 |
| TASK-080 | ✅ | download_button とファイル配信エンドポイントを実装する | TASK-021 |
| TASK-081 | ✅ | CSV/画像/PDF 生成物の配信パイプラインを実装する | TASK-080 |
| TASK-082 | ✅ | toast 通知要素を実装する | TASK-018 |
| TASK-083 | ✅ | progress / spinner / status 要素を実装する | TASK-018 |
| TASK-084 | ✅ | image / audio / video 表示要素を実装する | TASK-018 |
| TASK-085 | ✅ | divider / caption / subheader 補助要素を実装する | TASK-018 |
| TASK-086 | ✅ | color_picker / time_input / select_slider 入力要素を実装する | TASK-035 |
| TASK-087 | ✅ | text_area / data_editor 追加入力要素を実装する | TASK-035 |
| TASK-088 | ✅ | latex / html 表示要素を実装する | TASK-018 |
| TASK-089 | ✅ | 主要 40 要素のカタログ整合性を検証する | TASK-082,TASK-083,TASK-084,TASK-085,TASK-086,TASK-087,TASK-088 |
| TASK-090 | ✅ | core / server の境界を整理し Clean Architecture (lite) スタイルを採用する | TASK-021 |
| TASK-091 | ✅ | core に domain / port / application / runtime / bootstrap 層を確立し ArchUnit で依存方向を強制する | TASK-090 |
| TASK-092 | ✅ | spring-boot-starter モジュールを新設する | TASK-091 |
| TASK-093 | ✅ | auto-configuration で指定パスにマウントする | TASK-092 |
| TASK-094 | ✅ | Spring Security への認証委譲アダプターを実装する | TASK-093 |
| TASK-095 | ✅ | Spring Session への委譲アダプターを実装する | TASK-093 |
| TASK-096 | ✅ | 埋め込みパス配下のリソース提供を実装する | TASK-093 |
| TASK-097 | ✅ | カスタムコンポーネント宣言用の型安全 API を実装する | TASK-016 |
| TASK-098 | ✅ | コンポーネントの引数/戻り値シリアライザーを実装する | TASK-097 |
| TASK-099 | ✅ | インプロセス component の登録機構を実装する | TASK-097 |
| TASK-100 | ✅ | 同梱バンドルへの React 部品登録パイプラインを構築する | TASK-099 |
| TASK-101 | 🚫 | iframe 隔離 component のホスト機構を実装する | TASK-097 |
| TASK-102 | 🚫 | iframe sandbox 属性と CSP を適用する | TASK-101 |
| TASK-103 | 🚫 | iframe component の値検証と境界チェックを実装する | TASK-101 |
| TASK-104 | 🚫 | フロント TS SDK の値受け渡し API を実装する | TASK-098 |
| TASK-105 | 🚫 | フロント TS SDK の再描画通知ブリッジを実装する | TASK-104 |
| TASK-106 | 🚫 | streamlit4j component create 雛形生成コマンドを実装する | TASK-104 |
| TASK-107 | ✅ | Spring Boot 埋め込みサンプルアプリを作成する | TASK-094,TASK-095,TASK-096 |
| TASK-108 | ✅ | カスタムコンポーネントサンプル（インプロセス）を作成する | TASK-100 |
| TASK-109 | ✅ | 未決事項（終端 API 名/プロトコル等）を ADR で最終決定する | - |
| TASK-110 | ✅ | MIT ライセンスを全モジュールに適用する | - |
| TASK-111 | 🚫 | NOTICE と third-party ライセンス一覧を整備する | TASK-110 |
| TASK-112 | ✅ | 独立 OSS 旨の disclaimer を README 冒頭に明記する | - |
| TASK-113 | ✅ | 独立 OSS 旨の disclaimer を公式ドキュメント冒頭に明記する | TASK-115 |
| TASK-114 | ✅ | 独自ロゴと独自カラーパレットを策定しテーマに反映する | TASK-077 |
| TASK-115 | ✅ | 公式ドキュメントサイトの基盤を構築する | - |
| TASK-116 | ✅ | Getting Started チュートリアルを執筆する | TASK-115 |
| TASK-117 | ✅ | 機能リファレンスをドキュメントに整備する | TASK-115,TASK-089 |
| TASK-118 | ✅ | Javadoc 公開ビルドを整備する | TASK-001 |
| TASK-119 | ✅ | examples モジュールに代表サンプル群を整備する | TASK-060,TASK-107 |
| TASK-120 | ✅ | Spring Boot 統合手順をドキュメント化する | TASK-107,TASK-115 |
| TASK-121 | ✅ | カスタムコンポーネント作成ガイドを執筆する | TASK-100,TASK-115 |
| TASK-122 | ✅ | Maven Central パブリッシュ用 POM とメタデータを整備する | TASK-110 |
| TASK-123 | ✅ | GPG 署名と Sonatype アカウント設定を整備する | TASK-122 |
| TASK-124 | ✅ | Maven 利用者向け導入手順を整備する | TASK-122 |
| TASK-125 | ✅ | JBang カタログに CLI を登録する | TASK-059 |
| TASK-126 | 🚫 | playground（ホストされたデモ）を構築する | TASK-119 |
| TASK-127 | 🚫 | コア・互換要素・統合機能の E2E テストスイートを整備する | TASK-060,TASK-064,TASK-066,TASK-070,TASK-071,TASK-081,TASK-089,TASK-107 |
| TASK-128 | 🚫 | 0.1.0 リリースタグとリリースノートを作成する | TASK-109,TASK-112,TASK-113,TASK-114,TASK-116,TASK-117,TASK-118,TASK-120,TASK-121,TASK-124,TASK-125,TASK-127 |
| TASK-129 | 🚫 | Maven Central へ 0.1.0 を公開する | TASK-123,TASK-128 |
| TASK-130 | 🚫 | playground を 0.1.0 ビルドに更新し公開する | TASK-126,TASK-129 |
| TASK-131 | ⏳ | VitePress docs サイトの build を CI に組み込む | - |
| TASK-132 | ⏳ | OSV-Scanner 検出時の対応フローを security-scan.md に追記する | - |
| TASK-133 | ⛔ | cli の maven-shade-plugin で発生する同名ファイル警告を整理する（cli モジュール撤廃により無効化） | - |
| TASK-134 | ✅ | SpotBugs を Java 静的解析層として導入する | - |
| TASK-135 | ✅ | PMD + CPD を導入し重複コード検出を含む追加静的解析を実施する | - |
| TASK-136 | ✅ | Streamlit4jServer に静的アセット配信ハンドラーを追加（GET / で 404 になるバグ修正） | - |

## タスク詳細

### TASK-008

- 補足: ブラウザ起動と WebSocket 配下の E2E シナリオを実行可能にする
- 注意: TASK-028 以降の動作確認の前提となる

### TASK-009

- 補足: lint/test/build/coverage を一括実行する
- 注意: シークレットを必要とする publish ジョブは TASK-123 で別途整備する

### TASK-011

- 補足: バージョン付きエンベロープと type フィールドを必須とする
- 注意: 後で MessagePack に差し替えられる抽象境界を残す

### TASK-017

- 補足: StackWalker と引数ハッシュを併用する
- 注意: key 明示時はそれを最優先する

### TASK-021

- 補足: 組み込み Jetty または Undertow から選定する
- 注意: 認証は TASK-094 で委譲対応する

### TASK-037

- 補足: ノードは kind/id/props/children の不変表現とする
- 注意: 最小パッチ列を生成する keyed diff を採用する

### TASK-056

- 補足: 再実行レイテンシ・WebSocket 接続数も含める
- 注意: OpenTelemetry 連携は Backlog 扱い

### TASK-089

- 補足: v1 カバレッジ目標は主要 40 要素
- 注意: 全要素について Java API/プロトコル/フロント描画の 3 点セットを揃える

### TASK-051

- 補足: WS テキスト envelope `file_upload` で base64 エンコードしたバイト列を送信し `ProtocolEndpoint` で復号
- 注意: 真のバイナリ WS フレームによる大容量ストリーミング最適化は後続改善

### TASK-053

- 補足: `core.port.UploadValidator` + `DefaultUploadValidator` で size/MIME 検証
- 注意: St 側からの constraint 受け渡し API は後続で

### TASK-057

- 補足: `core.runtime.SourceWatcher` (NIO WatchService) + `ReloadNotice` envelope。CLI に `--watch <dir>` 追加
- 注意: 仮想スレッドで watcher を回す。Debounce は今後検討

### TASK-058

- 補足: `Cli.main(args)` で `--port` と `--watch` を解析。examples の Hello.run を hardcode 起動
- 注意: 任意 `.java` ファイル指定は TASK-059（JBang 経由）で対応

### TASK-059

- 補足: `jbang-catalog.json` を repo ルートに追加。`jbang app install streamlit4j@t-izuno/streamlit4j` でインストール可能
- 注意: 0.1.0 リリース後に JBang index 登録

### TASK-064

- 補足: `App.tsx` が `hashchange` を購読して `__page__` を server に同期。Page クリックは `window.location.hash` を更新
- 注意: pushState ベースのクリーン URL は今後検討

### TASK-076

- 補足: `theme.ts` で localStorage 永続化 + `data-theme` 属性切替。`styles.css` の CSS 変数でライト/ダーク定義
- 注意: System preference (`prefers-color-scheme`) との連動は今後

### TASK-077

- 補足: `styles.css` で `--color-accent` 等を変数化し `data-theme` ごとに上書き
- 注意: ユーザーが独自パレットを指定する API は今後

### TASK-080

- 補足: `core.port.DownloadStore` + `InMemoryDownloadStore`。`DownloadHandler` が `/download/{key}` で配信
- 注意: 大容量ファイルのストリーム配信は後続

### TASK-081

- 補足: `St.downloadCsv` と `St.downloadJson` を実装（CSV エスケープ含む）
- 注意: PDF 生成は PDFBox/iText 等を選定して後続で追加

### TASK-090

- 補足: アーキテクチャースタイル決定は design.md §0 に明文化（Clean Architecture lite、DDD/Onion/DI コンテナーは不採用）
- 注意: M3 着手予定だったが PoC 完了時点に前倒し実施

### TASK-091

- 補足: 層は `domain` / `protocol` / `port` / `application` / `runtime` / `bootstrap`、合成ルートは `Bootstrap.standalone`
- 注意: 依存方向違反は CoreArchitectureTest 9 ルール + ServerArchitectureTest 2 ルールで CI 検査

### TASK-092

- 補足: Spring Boot 3.5.14 を採用、`spring-boot-autoconfigure` を `optional=true` で依存追加
- 補足: `Streamlit4jAutoConfiguration` + `Streamlit4jProperties` のスケルトンを実装
- 補足: `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` を整備
- 注意: bean 配線・マウント処理は TASK-093 以降。親 pom に `spring-boot-dependencies` BOM を import 済み

### TASK-093

- 補足: Spring `WebSocketHandler` adapter (`Streamlit4jWebSocketHandler`) を新設
- 補足: AutoConfig で `Bootstrap.standalone(EntrypointSource)` を Bean 化
- 補足: `${streamlit4j.base-path}/ws` に WebSocket を登録。basePath 正規化を 6 パターンで検証
- 注意: `@ConditionalOnWebApplication(SERVLET)` + `@ConditionalOnClass(WebSocketHandler)`。
  認証・セッション委譲は TASK-094/095、静的リソースは TASK-096

### TASK-094

- 補足: `Streamlit4jPrincipalHandshakeInterceptor` が `SecurityContextHolder` の
  `Authentication` を WebSocket 属性 `streamlit4j.authentication` にコピー
- 補足: `@ConditionalOnClass(SecurityContextHolder)` で gating。ObjectProvider で
  HandshakeInterceptor 群を集約し WebSocket 登録時に attach
- 注意: アクセス制御自体は宿主側の `SecurityFilterChain` 責務（streamlit4j は強制しない）

### TASK-095

- 補足: `Streamlit4jHttpSessionHandshakeInterceptor` が HTTP セッション ID を
  WebSocket 属性に持ち越し、`Streamlit4jHttpSessionBinder` (Streamlit4jConnectionListener)
  が `Streamlit4jHttpSessionRegistry` に登録
- 補足: `Streamlit4jHttpSessionListener` (Servlet `HttpSessionListener`) が
  HTTP セッション破棄を購読して関連 Streamlit4j セッションを `SessionStore.remove` する
- 注意: Servlet 標準 API ベースなので Spring Session 利用時も透過的に動作する

### TASK-096

- 補足: `ResourceRegistration` (WebMvcConfigurer) が `${streamlit4j.base-path}/**`
  → `classpath:/META-INF/resources/streamlit4j/` のリソースハンドラーを登録
- 補足: frontend-assets jar 同梱の SPA 資産を Spring Boot 上で提供。basePath が
  blank / root の場合は登録をスキップしてデフォルトハンドラー衝突を回避
- 注意: 実際の SPA バンドル（Vite build 成果物）の同梱パイプラインは TASK-100 で整備

### TASK-107

- 補足: `examples/spring/SpringBootHelloApp` が `@SpringBootApplication` で起動し
  `EntrypointSource` Bean 経由で `Hello.run` を提供
- 補足: `@SpringBootTest(webEnvironment=RANDOM_PORT)` で Tomcat 起動 + AutoConfig
  全 Bean (Streamlit4jApplication / WebSocketHandler / EntrypointSource) の解決を検証
- 注意: spring-boot-starter-web / -websocket を optional 依存で取り込む（examples 由来）

### TASK-110

- 補足: ライセンスは MIT で確定（特許明示や NOTICE 伝達などの追加条件を不要にし、
  利用者側の義務を最小化する方針）。`LICENSE` は MIT 全文を維持
- 補足: 親 pom の `<licenses>` を MIT に揃え、子 pom は宣言を継承
- 注意: TASK-109 の未決事項リストから「ライセンス」は本タスクをもって確定済み

### TASK-111

- 中止理由: 本体ライセンスを MIT で確定したため Apache-2.0 4(d) 由来の NOTICE 伝達義務が消滅し、
  プロジェクト本体の NOTICE ファイルは不要。依存ライブラリのライセンス一覧 (THIRD-PARTY-NOTICES)
  も配布形態が source / Maven Central jar のみであり、依存元のライセンスは各 jar に同梱されるため再録不要
- 注意: 将来 fat-jar / native image など依存を再配布する形態を採る場合は再開を検討

### TASK-112

- 補足: README 冒頭の disclaimer を強化。「not affiliated with, endorsed by, or
  sponsored by」と Streamlit を商標と明記し nominative fair use の三要件を文面上担保
- 注意: License セクションは MIT への単純リンクのみ（NOTICE / THIRD-PARTY-NOTICES は採用せず）

### TASK-118

- 補足: `maven-javadoc-plugin` 3.10.1 を pluginManagement に追加、`release` profile
  でのみ `attach-javadocs` を実行する構成。各 jar モジュールに `*-javadoc.jar` を生成
- 補足: JDK 21 doclint 厳格化対策として `doclint=none` + `failOnError=false` を設定
- 補足: frontend-assets は Java ソース無しのため `maven.javadoc.skip=true` で除外
- 注意: source jar 同梱や GPG 署名は TASK-122 / TASK-123（Maven Central パブリッシュ準備）で扱う

### TASK-115

- 補足: VitePress 1.6.4 を採用。`docs/public/.vitepress/config.ts` でナビ / サイドバー / 検索 /
  footer disclaimer を設定。`docs/public/package.json` に `docs:dev` / `docs:build` / `docs:preview`
  スクリプトを定義
- 補足: ランディング `docs/public/index.md` (hero + features) と、`docs/public/guide/getting-started.md`、
  `docs/public/guide/spring-boot.md`、`docs/public/reference/overview.md` のスケルトンを配置
- 補足: 既存 `docs/devel/requirements.md` / `docs/devel/specification.md` / `docs/devel/design.md` をサイトから
  参照。`docs/devel/tasks/**` はサイト対象外
- 注意: 詳細コンテンツは TASK-116 / 117 / 120 / 121 で肉付け。デプロイ自動化は未定義。
  GitHub Pages 想定だが本タスクのスコープ外

### TASK-113

- 補足: ランディング (`docs/public/index.md`) hero 直下に強調表示の disclaimer を配置。
  `themeConfig.footer.message` でサイト全ページのフッターにも常時掲出
- 注意: 各ガイドページにも個別 disclaimer を出すかは TASK-116 以降の編集方針に委ねる

### TASK-116

- 補足: `docs/public/guide/getting-started.md` を完成版に差し替え。Prerequisites / CLI 起動 /
  Maven 経由でのアプリ作成 / 再実行モデル解説 / `--watch` フラグ案内まで網羅
- 補足: コード例は `examples/Hello.java` と整合。Maven Central パブリッシュ前提のため
  バージョンは `0.1.0-SNAPSHOT` 表記

### TASK-120

- 補足: `docs/public/guide/spring-boot.md` を完成版に差し替え。starter 依存・EntrypointSource /
  base-path 設定・Spring Security 委譲・Spring Session 自動連動・auto-config 配線一覧を網羅
- 補足: `examples/spring/SpringBootHelloApp` を参照する end-to-end 例を含む
- 注意: `spring-boot-maven-plugin` 統合は TASK-122（Maven Central パブリッシュ準備）に持ち越し

### TASK-122

- 補足: 親 pom に Maven Central 公開要件メタデータを追加。developers / scm /
  issueManagement / ciManagement
- 補足: `maven-source-plugin` 3.3.1 を pluginManagement に追加し `release` profile で
  `attach-sources` 発火。各 jar モジュールに `*-sources.jar` を生成
- 補足: `central-publishing-maven-plugin` 0.6.0 を導入。`autoPublish=false` で
  最初は手動公開、`publishingServerId=central` で Central Portal 認証を参照
- 補足: examples モジュールは `maven.deploy.skip=true` + `central-publishing.skip=true`
  で公開対象から除外
- 注意: GPG 署名・Sonatype 認証情報設定は TASK-123 で別途整備

### TASK-123

- 補足: `maven-gpg-plugin` 3.2.7 を pluginManagement に追加、`release` profile の
  `verify` フェーズで `sign` ゴール発火。`--pinentry-mode loopback` で CI 環境対応
- 補足: `gpg.skip=true` をデフォルトにし、リリース時のみ `-Dgpg.skip=false` で
  明示的に署名発火する運用。普段の `mvn -P release package` は失敗しない
- 補足: Sonatype Central Portal アカウント情報は pom にハードコードせず、
  `~/.m2/settings.xml` の `<server id="central">` で扱う運用。手順は `docs/devel/publishing.md`
- 注意: 実際の鍵生成・鍵公開・Portal アカウント作成は配布者責任。CI でのシークレット注入は
  TASK-128 のリリース工程で扱う想定

### TASK-124

- 補足: `docs/public/guide/installation.md` を新設。Maven 座標一覧 / 標準 / Spring Boot /
  Gradle / JBang / Snapshot / GPG 検証手順を網羅
- 補足: VitePress サイドバーに Installation ページを追加
- 注意: 0.1.0 が Maven Central に上がるまでは Snapshot 取得手順がメインパス

### TASK-125

- 補足: `jbang-catalog.json` に `main: io.streamlit4j.cli.Cli` を明示。
  これで `jbang app install streamlit4j@t-izuno/streamlit4j` から CLI が直接起動
- 注意: 0.1.0 リリース時に `script-ref` の version を `0.1.0` に切り替える（TASK-128）。
  公式 JBang Community Catalog への登録は jbangdev/jbang-catalog への PR で行う（リリース後）

### TASK-119

- 補足: `examples` に 3 つの代表サンプルを追加。
  `WidgetsDemo` (text/number/select/radio/checkbox/button/slider/date/time/color picker)、
  `LayoutDemo` (sidebar/columns/tabs/expander/form)、
  `DataDemo` (cacheData/dataframe/metric/line/bar/area/scatter chart)
- 補足: 各サンプルは独立した `run()` 静的メソッドを公開。
  `new Streamlit4jServer(port, () -> WidgetsDemo::run)` のように差し替えて起動
- 注意: CLI から複数サンプルを切り替えるフラグは未整備（現状は `Hello` 固定）。
  TASK-127（E2E スイート）で必要なら CLI 側に `--app <完全修飾クラス名>` オプションを追加検討

### TASK-097

- 補足: `core.domain.CustomComponent<R>` Record でコンポーネントの型安全宣言。
  `name` と `resultType` を保持し、`ofVoid(name)` factory も提供
- 補足: `St.component(spec, args)` / `St.component(spec, args, default)` /
  `St.component(name, args)` の 3 メソッドを追加。戻り値型は宣言と一致（compile-time 型安全）
- 補足: ノード `kind="component"`、props は `name` / `args` / `value`（値が null の場合は省略）
- 補足: ウィジェット ID は既存と同じ `WidgetIds.generate(...)` で安定生成、args ハッシュ込み
- 補足: 7 ケースのテスト（emit / default / rerun / display-only / 別 args / blank name 拒否 / void factory）
- 注意: 引数 / 戻り値のシリアライザー、インプロセス component 登録、iframe 隔離、フロント TS SDK は
  TASK-098 / TASK-099 / TASK-101 / TASK-104 で別途整備

### TASK-098

- 補足: `core.protocol.ComponentCodec` を新設し、引数を JsonNode に符号化する
  `encodeArg(Object)`、戻り値を宣言型に復号する `decodeReturn(JsonNode, Class)`、
  両者をまとめた `coerce(Object, Class, fallback)` を提供。共有 `Codec.mapper()` を
  再利用して `render_delta` / `widget_event` の符号化規約を一本化
- 補足: `St.component(spec, args, default)` の読出しを `ComponentCodec.coerce` に
  差し替え。WS から JsonNode で届いた複合型を宣言型 (`spec.resultType()`) に
  自動デコードする
- 補足: 両 WS アダプター (`ProtocolEndpoint` / `Streamlit4jWebSocketHandler`) の
  `unwrap()` を修正し、object/array は文字列化せず JsonNode のまま下流へ渡す
- 補足: `ComponentCodecTest` 13 ケース（primitive/Map/record/List 符号化、null/object
  /primitive 復号、fallback、Map→record 変換、不変換時 fallback）と
  `CustomComponentTest` に JsonNode 復号ラウンドトリップを追加
- 注意: フロント TS SDK 側の値受け渡し API（TASK-104）でこの符号化規約に合わせる。
  iframe 隔離 component の境界検証（TASK-103）は別タスク

### TASK-099

- 補足: `core.port.ComponentRegistry` ポートを新設（`register` / `find` / `all` / `size`）。
  名前重複は上書き、`all()` は読み取り専用スナップショットを返す
- 補足: `core.runtime.InMemoryComponentRegistry` をデフォルト実装として提供。
  `ConcurrentHashMap` ベースでスレッドセーフ
- 補足: `core.runtime.ComponentRegistryAccess` で静的アクセサーを公開（`DownloadAccess`
  と同パターン）。テストでは `use(...)` で差し替え可能
- 補足: `Bootstrap.standalone(...)` で配線し、`Streamlit4jApplication#components()` で
  外部公開
- 補足: `St.registerComponent(spec)` を追加。返り値は spec 自体（流暢宣言向け）
- 補足: 7 ケースのテスト（registry 単体 6 + St.registerComponent 1）。Bootstrap 配線は
  既存の `Streamlit4jApplication` 構築経路でカバー
- 注意: フロントへの登録名通知 / バンドルパイプライン連携は TASK-100、iframe との
  分岐（mode マーカー）は TASK-101 で扱う

### TASK-100

- 補足: フロント側に `src/component-registry.ts` を新設し、`registerComponent(name, renderer)`
  / `findComponent(name)` / `registeredNames()` / `clearComponents()` を提供。
  `CustomComponentRenderProps` (args/value/onChange) で型契約を明示
- 補足: `src/component-builtins.ts` を first-party 登録のエントリーポイントとして
  追加。`main.tsx` から無条件 import し、起動時に組み込みコンポーネントを登録する
  パイプラインを確立。組み込みコンポーネント追加手順を JSDoc にまとめた
- 補足: `render.tsx` に `case 'component':` を追加。`props.name` でレジストリを引いて
  描画。未登録時は `.component--unregistered` プレースホルダーにフォールバックし、
  TASK-101 で iframe ホストに置き換える境界とする
- 補足: dependency-cruiser ルール `components-do-not-cross-depend` に合わせ、
  レジストリと builtins は `src/components/` の外（UI コンポーネントの一段上）に配置
- 補足: TASK-096 の注記通り、frontend-assets pom に `maven-antrun-plugin` を追加し、
  `generate-resources` フェーズで `frontend/dist/**` を
  `META-INF/resources/streamlit4j/` にコピー。`erroronmissingdir="false"` で
  dist 未生成環境でも build を継続（プレースホルダー index.html が残る）
- 補足: フロント vitest 12 ケース（registry 単体 6 + App 統合 2: 登録レンダラー描画 +
  未登録フォールバック）。eslint / prettier / depcruise / `vite build` / `mvn verify`
  すべて通過
- 注意: フロント TS SDK のラッパー API（TASK-104）と再描画ブリッジ（TASK-105）は
  レジストリ上で `CustomComponentRenderProps` を成形して提供する。CLI 雛形生成は
  TASK-106 でこのレジストリ呼び出しを生成する

### TASK-101 〜 TASK-106（iframe 系: 中止）

- 中止理由: 本家 Streamlit V2 Components が iframe を廃止しホスト直マウントへ
  移行している動向、`allow-scripts` 単独 sandbox の機能制約、postMessage 境界
  検証 / CSP / payload size などの運用負荷を踏まえ、iframe 隔離方式の採用を
  取りやめた。詳細は `design.md` §9 を参照
- 削除内容:
  - Core: `St.iframeComponent(...)` メソッド群および対応するテスト 4 件
  - Frontend: `components/IframeComponent.tsx` /
    `iframe-payload-validator.ts` / `sdk/Streamlit4jComponent.ts` /
    `sdk/useComponentState.ts` および各々のテスト計 21 件、`App.test.tsx`
    の iframe 経路テスト 4 件、`render.tsx` の iframeSrc 分岐
  - CLI: `ComponentScaffold` クラス・テスト、`Cli` のサブコマンドルーター、
    `cli/pom.xml` の JUnit / AssertJ テスト依存
  - Docs: `design.md` §9-2 / §9-3、`specification.md` 9.1 第三者向け項、
    9.3 / 9.4
- 注意: カスタムコンポーネントは in-process 方式のみで提供（TASK-098〜100
  の成果物は維持）。第三者コンポーネントが必要になった場合は npm 依存と同様
  に取り込み in-process として配布する運用とする

### TASK-108

- 補足: in-process カスタムコンポーネントの代表例として `star-rating`
  レンダラー（`frontend/src/components/StarRating.tsx`）を新設。
  `component-builtins.ts` で `registerComponent('star-rating', StarRating)`
  を実行することで TS 側登録を実施
- 補足: Java 側サンプル `examples/.../ComponentDemo.java` を追加。
  `St.registerComponent(new CustomComponent<>("star-rating", Integer.class))`
  と `St.component(spec, args, default)` の最小フルパスを提示
- 補足: `StarRating.tsx` は `args.label` / `args.max`（1〜10 にクランプ）/
  `value`（0〜max にクランプ）を受け取り、クリックでレーティング更新を
  `onChange` 経由で送信。同じ値をクリックすると 0 にリセット
- 補足: `frontend/src/components/StarRating.test.tsx` 6 ケース（初期表示 /
  value 反映 / クリック発火 / 同値リセット / 不正 args / max クランプ）
  および `styles.css` の `.component--star-rating` 追加
- 注意: 本来スコープに含めていた iframe 系サンプルは「iframe 機能自体の廃止」
  に伴い対象外。TASK-127 の E2E 依存も TASK-108 を外して整合化済み

### TASK-109

- 補足: `docs/devel/adr/` を新設し、アーキテクチャー判断を 5 件の ADR として確定:
  - ADR-0002 プロトコルは JSON（Jackson）。MessagePack は不採用
  - ADR-0004 GraalVM ネイティブ対応は v1.x 以降へ繰り延べ
  - ADR-0005 マルチページは `St.pages(List<Page>)` の明示登録を既定
  - ADR-0006 ライセンスは MIT（TASK-110 にて確定済みを ADR として追記）
  - ADR-0007 カスタムコンポーネントは iframe 隔離を採らず in-process のみ
- 補足: 終端 API 命名（`St.*` 直接呼び出し）とキャッシュ API スタイル（`St.cacheData` /
  `St.cacheResource` ラッパー関数）は実装レベルの選択と整理し、ADR ではなく
  `design.md` §10-2「API 設計指針」に併記する形へ移行
- 補足: `design.md` §10 を「未決事項」から「決定済み事項」に書き換え、§10-1 に
  ADR 一覧表、§10-2 に実装指針の併記を追加
- 補足: `.vitepress/config.ts` の sidebar に ADR セクションを追加（ドキュメントサイトに反映）

### TASK-114

- 補足: ブランドカラーは teal 主軸（light `#0d9488` / dark `#2dd4bf`）。
  Streamlit 赤系（`#FF4B4B`）と明確に区別。サポート色は slate（fg / muted /
  border）。`design.md` §11-1 にトークン定義表を追記
- 補足: ロゴは「並列データストリーム」を抽象化したシンボルマーク。
  Streamlit の swirl 模様は流用せず、上下に細い短バー / 中央に太い長バーの
  3 本構成 SVG を採用。light / dark 版と favicon の 3 ファイルを
  `docs/public/` と `frontend/public/` に配置
- 補足: 反映先 3 箇所:
  - `frontend/src/styles.css`: CSS 変数を teal パレットに刷新
  - `frontend/index.html`: `link rel="icon"` で favicon を読み込み
  - `docs/public/.vitepress/config.ts`: `logo` / `head` favicon を設定、
    `docs/public/.vitepress/theme/{index.ts,brand.css}` で `--vp-c-brand-*` を上書き
- 補足: VitePress 採用の根拠を [ADR-0010](../adr/0010-vitepress-for-docs.md) として
  併せて記録（フロント Vite/TS スタックとの一致 / Markdown ファースト /
  `--vp-c-brand-*` 経由のテーマ刷新容易性が決定理由）
- 補足: ADR-0010 が前提とする「SPA が Vite ベース」自体が遡及的に未記録だった
  ため、[ADR-0009](../adr/0009-vite-for-frontend.md) として Vite（フロント
  ビルドツール）の採用根拠も併記。webpack / Rollup / Parcel / esbuild との
  比較と、HMR 速度 / Vitest 統一 / VitePress 親和性が決定理由
- 注意: ロゴ SVG は本リポジトリの MIT ライセンス下で配布（商用利用可）。
  ロゴアセットを更新するときは 4 ファイル（2 ディレクトリー）すべて同期させる

### TASK-117

- 補足: `docs/public/reference/` を `St` のカテゴリー分割（`core/api/*Widgets.java` 群）に
  合わせて 13 ページ構成に整備（overview / text / status / data / media /
  charts / inputs / files / layout / forms / cache / pages / components /
  control）。各要素について Java API シグネチャー / プロトコル `kind` と props
  / フロント描画の 3 点をテーブルで網羅
- 補足: `docs/public/.vitepress/config.ts` の `/reference/` サイドバーを 13 リンクに拡張
- 補足: `overview.md` から ADR / specification への横断リンクを整備
- 注意: chart 系は v1 ではプレースホルダー描画。実描画ライブラリー対応は backlog
- 注意: `dataEditor` の編集 → サーバー反映は v1 未実装。ノード描画のみ

### TASK-121

- 補足: `docs/public/guide/custom-components.md` を新設。in-process 方式の手順を
  Java（CustomComponent 宣言 → registerComponent → St.component）/
  React（CustomComponentRenderProps を受けるレンダラー → component-builtins
  への登録）の 2 系統で示し、star-rating を実装例として全文掲載
- 補足: 戻り値型変換テーブル（`ComponentCodec.coerce` の 4 段階）と
  チェックリストを併記
- 補足: `docs/public/.vitepress/config.ts` の `/guide/` サイドバーに Custom Components
  リンクを追加
- 補足: 同タイミングで `getting-started.md` の `mvnw 未提供` 記述を解消し、
  `St` import パスを `io.streamlit4j.core.api.St` に更新（A の sub-package
  移動の追従）

### TASK-126 〜 TASK-130（リリース系: 中止）

- 中止理由: 内部で使ってブラッシュアップする期間が必要なため、0.1.0 の対外公開
  および playground の公開を保留。E2E スイート（TASK-127）もリリース連動の
  作業として一旦中止。ブラッシュアップ後、公開タイミングが見えた段階で
  改めて計画する
- 対象:
  - TASK-126 playground 構築
  - TASK-127 E2E テストスイート整備（再開時は widget 操作 + multipage + form
    の軽量版から始める）
  - TASK-128 0.1.0 リリースタグとリリースノート
  - TASK-129 Maven Central 公開
  - TASK-130 playground を 0.1.0 で更新公開
- 注意: 内部利用フェーズで判明した問題は新タスク（TASK-131 以降）として登録
  する。本中止項目は将来再開する際に同番号 / タイトルのまま新設しない

### TASK-131

- 補足: 旧構成（`site/` + `srcDir: "../docs"`）では VitePress build が
  `docs/public/guide/custom-components.md` で `vue/server-renderer` の解決失敗により
  失敗していた。CI フロントジョブが `frontend/` しか触っていないため、
  docs サイト破損が PR を通過してしまう
- 補足: `.github/workflows/ci.yml` に docs build ステップを追加し、
  `cd docs/public && npm ci && npm run docs:build` を走らせる
- 注意: 現在の build 失敗自体の原因究明も本タスクの範囲（VitePress と Vue の
  peer dep バージョン整合 / コードブロック内の Vue テンプレート誤評価が候補）

### TASK-132

- 補足: `docs/devel/security-scan.md` は OWASP の手動実行手順と suppression
  運用を扱うが、`ci.yml` の OSV-Scanner で検出された脆弱性への対応フローは
  別途必要。OSV は OWASP と suppression の仕組みが異なる（`osv-scanner.toml`）
- 補足: 検出 → 真陽性なら依存更新 / 偽陽性なら `osv-scanner.toml` で
  `IgnoredVulns` 追記、というフローを文書化
- 注意: OWASP の `owasp-suppressions.xml` と OSV の `osv-scanner.toml` を
  混在運用するため、どちらに何を書くかの境界を明確化する

### TASK-133

- 補足: 今回 cli モジュールに `maven-shade-plugin` を導入したことで複数 JAR に
  同名ファイル警告が出ている（動作には影響なし）。将来 SPI / META-INF 同名
  ファイルの取り扱いでトラブル源になる可能性
- 補足: `mvn -pl cli package -X` で重複ファイル一覧を取得し、必要に応じて
  ServicesResourceTransformer / ApacheNoticeResourceTransformer 等を追加、
  もしくは filter で除外
- 注意: Jetty 12 / Jackson 周りの `META-INF/services/*` を消すと実行時 fail
  するため、安易に exclude しない

### TASK-134

- 補足: `spotbugs-maven-plugin` 4.8.6.6 を導入。`effort=Max` / `threshold=Medium` /
  `failOnError=true` / `includeTests=false` / `excludeFilterFile=spotbugs-exclude.xml`
- 補足: ユーザー承認済み exclude（`spotbugs-exclude.xml` に根拠コメント付きで記録）:
  - `EI` / `EI2`（EI_EXPOSE_REP / EI_EXPOSE_REP2）— record auto-accessor / port-based DI が
    interface ref を field 保持するパターン。defensive copy 不能 / 不適切
  - `NM_SAME_SIMPLE_NAME_AS_SUPERCLASS` — sentinel 例外型の命名衝突
  - `UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD` / `URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD` —
    公開 API / シリアライズ対象フィールド
- 補足: コード修正で解消したもの: `Codec.mapper()` を廃止し、shared `ObjectMapper` を
  encapsulate する `Codec.valueToTree` / `treeToValue` / `convertValue` の static
  ヘルパー化（MS_EXPOSE_REP 解消）
- 注意: exclude の追加は **ユーザー承認必須**（`feedback-no-suppression` 参照）。
  AI が reactive に追加するのは禁止
- 注意: ErrorProne は引き続き不採用（BACKLOG-011 維持）

### TASK-135

- 補足: `maven-pmd-plugin` 3.26.0 + `pmd-core` / `pmd-java` 7.7.0 を導入。
  `category/java/bestpractices.xml` + `category/java/errorprone.xml` の 2 カテゴリーを採用
- 補足: ユーザー承認済み exclude（`pmd-ruleset.xml` に根拠コメント付き）:
  - errorprone: `AvoidFieldNameMatchingMethodName` — 現代 Java の accessor 規約
    （`int id()` で `id` field を返す）と本質的に衝突
  - bestpractices: `GuardLogStatement` — SLF4J の parameterized format（`{}`）が
    内部で `toString()` を遅延評価するため、`isXxxEnabled()` guard は冗長
- 補足: コード修正（合計 11+ 件、suppress なし）:
  - `UploadValidator.Constraints` / `RenderDelta` / `RenderNode` の record を
    compact constructor → **canonical constructor** に書き換え（`this.field = X`
    が field 書込みであるため PMD UnusedAssignment 対象外になる性質を利用）
  - widget facade ファイル 5 つで重複リテラルを `private static final` 定数化
    （`PROP_TEXT` / `PROP_BODY` / `KIND_CODE` / `PROP_LABEL` / `PROP_VALUE` /
    `PROP_OPTIONS` / `PROP_DATA` / `KIND_METRIC` / `KIND_COMPONENT`）
  - `ControlSignals` ホルダーを削除、`RerunRequested` / `StopRequested` を
    top-level クラスに昇格 + `serialVersionUID` 追加
  - `ScriptRunner` の `ExecutionException` unwrap を `throw new RuntimeException("Render failed", e)`
    に簡素化（PreserveStackTrace 解消）。`++reruns` を increment と condition に分割
  - `Streamlit4jAutoConfiguration` で Yoda 比較（`"/".equals(basePath)`）+ 単一要素
    最適化分岐削除（AvoidLiteralsInIfCondition 解消）
  - `Cli` で `System.out.println` → SLF4J ログ化、`for (int i=...; i++)` の `args[++i]` を
    while ループ + 明示的 `i += 2` に変更
- 補足: CPD（Copy-Paste Detector）は `failOnViolation=false` で計測のみ（minimumTokens=100）。
  現状検出 0 件
- 注意: PMD `UnusedAssignment` は record の compact constructor を理解しない既知バグ。
  将来 PMD が対応したら canonical constructor を compact に戻して可読性を回復する余地あり

## Backlog一覧

| ID | Status | Summary | DependsOn |
| ---- | ---- | ---- | ---- |
| BACKLOG-001 | ⏳ | ScopedValue へのレンダーコンテキスト移行を実装する | - |
| BACKLOG-002 | ⏳ | OpenTelemetry トレース/メトリクス連携を実装する | - |
| BACKLOG-003 | ⏳ | プロトコルを MessagePack/Protobuf に切り替える | - |
| BACKLOG-004 | ⏳ | Quarkus Extension を実装する | - |
| BACKLOG-005 | ⏳ | GraalVM native image 公式サポートを実装する | - |
| BACKLOG-006 | ⏳ | 共有・ホスティング基盤（Community Cloud 相当）を提供する | - |
| BACKLOG-007 | ⏳ | リアルタイム協調編集を実装する | - |
| BACKLOG-008 | ⏳ | gRPC / SSE フォールバックトランスポートを実装する | - |
| BACKLOG-009 | 🚫 | CycloneDX で SBOM を自動生成しリリース成果物に同梱する | - |
| BACKLOG-010 | 🚫 | license-maven-plugin で第三者ライセンスをホワイトリスト管理する | - |
| BACKLOG-011 | 🚫 | SpotBugs / ErrorProne による Java 静的解析層を追加する | - |
| BACKLOG-012 | 🚫 | PIT で mutation testing を導入しカバレッジの質を担保する | - |
| BACKLOG-013 | 🚫 | OpenAPI / AsyncAPI で WebSocket プロトコルを機械可読化する | - |

## Backlog詳細

### BACKLOG-003

- 補足: エンベロープのバージョニングを利用して段階移行する
- 注意: v1 では JSON 維持

### BACKLOG-005

- 補足: reflection 設定の網羅が必要
- 注意: 着手時期は TASK-109 で決定する

### BACKLOG-009（中止）

- 中止理由: 個人 OSS スケールでは SBOM 生成のメンテナンスコストが先行し、
  受益者（取り込み先の SCA ツール / エンタープライズ利用者）が限定的。
  現時点では Maven Central の POM メタデータ + GPG 署名で供給網検証は十分。
  エンタープライズ採用が見えたタイミングで再評価する
- 再開条件: SBOM 提供を前提とする利用者要望が出た場合、または
  Maven Central / 業界標準として SBOM 同梱が必須化された場合

### BACKLOG-010（中止）

- 中止理由: 依存は BOM 経由で Spring Boot / Jetty / Jackson / JUnit / AssertJ /
  Mockito / ArchUnit に限定されており、いずれも Apache 2.0 / EPL / MIT の
  許諾系。ホワイトリスト機械検査を入れずとも、依存追加時の目視確認で十分カバー可能
- 再開条件: 依存ツリーが大幅に拡大する、または GPL / AGPL 等のコピーレフト系を
  含む可能性があるエコシステムの依存を追加する場合

### TASK-136

- 補足: バグ — `Streamlit4jServer` は `DownloadHandler` + `WebSocketUpgradeHandler` のみ登録しており、`META-INF/resources/streamlit4j/` の SPA を配信する handler が無かった。README で謳う「`jbang app install streamlit4j@... && streamlit4j 8501`」直後にブラウザーで `http://localhost:8501/` を開くと 404 になっていた
- 補足: 修正内容 — `ResourceHandler` を追加し、`META-INF/resources/streamlit4j/` を base resource として mount。base URI は `index.html` の URL から親を導出（JAR 内 directory の classloader 解決が不安定なため）
- 補足: Jetty 12 の `ResourceHandler` welcome-file dispatch が JAR 内リソースで安定して発火しないため、`/` ルート専用の `FrontendRootHandler`（`Handler.Abstract` 継承）を前段に追加。pathInContext が `/` または空のとき `index.html` の bytes を直接 `response.write` で返す
- 補足: テスト追加: GET `/` で 200 + `text/html` + `<!doctype html>` + `<div id="root">` を確認、GET `/index.html` 直アクセスも 200 を確認
- 補足: ローカル検証で `cd cli` → JBang/手動 java 起動 → `curl http://localhost:8501/` が STATUS=200 / CT=text/html を返すことを実機確認
- 注意: Spring Boot Starter 側（`Streamlit4jAutoConfiguration.ResourceRegistration`）は base-path が空（root）のとき意図的に handler 登録をスキップしている。host アプリケーション側に独自 home page を持たせる前提のため別バグ判定はしない

### BACKLOG-011（中止 → 部分的に再評価）

- 中止理由（初回判断）: Checkstyle（フォーマット / 命名 / 構造）+ ArchUnit
  （レイヤー強制）+ 単体 / 結合テスト + Javadoc 厳格 ですでに本プロジェクト規模では
  十分な検査層。SpotBugs / ErrorProne を追加すると false positive 抑制と
  suppression 管理の運用コストが先行する
- 再評価結果: SpotBugs と PMD/CPD は TASK-134 / TASK-135 として個別採用。
  ErrorProne は引き続き不採用（SpotBugs と機能重複・併用維持コスト）

### BACKLOG-012（中止）

- 中止理由: カバレッジ閾値強制を採用しない方針（[[feedback-coverage-threshold]]）
  と整合せず、mutation testing は計算コストと CI 時間を大幅に増やす割に
  本プロジェクト規模では新規バグの検出効率が低い
- 再開条件: テスト品質に明確な問題が観測される、または外部ツール（SonarQube
  等）と統合して非同期で回せる構成が成立した場合

### BACKLOG-013（中止）

- 中止理由: 現在 WebSocket クライアントは同一リポジトリーの React SPA 単一
  実装で、多言語クライアント実装計画は未定。プロトコル仕様は
  `protocol/Envelope` 等の Java DTO + JSON コーデックで一元管理されており、
  これが事実上の仕様書として機能している
- 再開条件: Python / TypeScript / Go 等の独立クライアント実装計画が
  立ち上がった場合、または外部開発者に WebSocket 互換実装を提供する需要が出た場合

