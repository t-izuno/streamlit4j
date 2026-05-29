# TASKS

マイルストーン: M1
ゴール: docs/requirements.md の v1 スコープ全体（実行エンジン・コア・互換拡充・統合・公開）を満たし streamlit4j 0.1.0 を Maven Central に公開する

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
| TASK-004 | ✅ | Spotless と Checkstyle で Java 静的解析を設定する | TASK-002 |
| TASK-005 | ✅ | JaCoCo でカバレッジ計測とレポート出力を構築する | TASK-003 |
| TASK-006 | ✅ | Vitest と Testing Library でフロント単体テスト基盤を構築する | TASK-001 |
| TASK-007 | ✅ | ESLint と Prettier でフロント静的解析と整形を設定する | TASK-001 |
| TASK-008 | ✅ | Playwright で E2E テスト基盤を構築する | TASK-006 |
| TASK-009 | ✅ | GitHub Actions で push/PR の CI パイプラインを構築する | TASK-003,TASK-004,TASK-005,TASK-006,TASK-007 |
| TASK-010 | ✅ | OWASP Dependency-Check で脆弱性スキャンを CI に組み込む | TASK-009 |
| TASK-011 | ⏳ | WebSocket JSON エンベロープのスキーマを定義する | TASK-002 |
| TASK-012 | ⏳ | レンダーノードの不変データモデルを実装する | TASK-002 |
| TASK-013 | ⏳ | session_init / render_delta / widget_event の DTO を実装する | TASK-011,TASK-012 |
| TASK-014 | ⏳ | JSON シリアライザー/デシリアライザーを実装する | TASK-013 |
| TASK-015 | ⏳ | 仮想スレッドで動くスクリプトランナーを実装する | TASK-002 |
| TASK-016 | ⏳ | ThreadLocal ベースのレンダーコンテキスト束縛を実装する | TASK-012,TASK-015 |
| TASK-017 | ⏳ | 呼び出し位置由来の決定的ウィジェット ID 生成を実装する | TASK-016 |
| TASK-018 | ⏳ | St.title / St.markdown / St.write の表示系 API を実装する | TASK-016 |
| TASK-019 | ⏳ | St.slider 入力系 API と値返却ロジックを実装する | TASK-017 |
| TASK-020 | ⏳ | 単一セッションのライフサイクル管理を実装する | TASK-015 |
| TASK-021 | ⏳ | 組み込み HTTP/WS サーバの起動と接続受付を実装する | TASK-014,TASK-020 |
| TASK-022 | ⏳ | widget_event 受信から再実行までの直列処理を実装する | TASK-019,TASK-021 |
| TASK-023 | ⏳ | React + Vite + TypeScript のフロント雛形を構築する | TASK-007,TASK-011 |
| TASK-024 | ⏳ | WebSocket クライアントとメッセージ受発信を実装する | TASK-013,TASK-023 |
| TASK-025 | ⏳ | title / markdown / write のフロント描画を実装する | TASK-024 |
| TASK-026 | ⏳ | slider のフロント描画と値変更イベント送信を実装する | TASK-024 |
| TASK-027 | ⏳ | フロント差分適用と全置換フォールバックを実装する | TASK-025,TASK-026 |
| TASK-028 | ⏳ | PoC サンプル App を作成し E2E 動作を確認する | TASK-022,TASK-027,TASK-008 |
| TASK-029 | ⏳ | セッションマネージャーと生成/タイムアウト/破棄を実装する | TASK-020 |
| TASK-030 | ⏳ | セッションライフサイクルリスナー API を実装する | TASK-029 |
| TASK-031 | ⏳ | 仮想スレッドによるセッション間並行実行を実装する | TASK-029 |
| TASK-032 | ⏳ | セッション内直列再実行とイベントキューを実装する | TASK-031 |
| TASK-033 | ⏳ | SessionState の Key-Value ストアを実装する | TASK-029 |
| TASK-034 | ⏳ | SessionState の型付きアクセサー API を実装する | TASK-033 |
| TASK-035 | ⏳ | ウィジェット値を ID キーで再実行間に保持する仕組みを実装する | TASK-033 |
| TASK-036 | ⏳ | St.rerun / St.stop の制御 API を実装する | TASK-032 |
| TASK-037 | ⏳ | レンダーツリーの keyed diff アルゴリズムを実装する | TASK-012 |
| TASK-038 | ⏳ | render_delta の op/path/node パッチ生成を実装する | TASK-037 |
| TASK-039 | ⏳ | フロント差分適用ロジックを keyed diff 対応に拡張する | TASK-027,TASK-038 |
| TASK-040 | ⏳ | text_input / number_input ウィジェットを実装する | TASK-035 |
| TASK-041 | ⏳ | selectbox / multiselect ウィジェットを実装する | TASK-035 |
| TASK-042 | ⏳ | checkbox / radio / button ウィジェットを実装する | TASK-035 |
| TASK-043 | ⏳ | date_input ウィジェットを実装する | TASK-035 |
| TASK-044 | ⏳ | header / metric の表示系要素を実装する | TASK-018 |
| TASK-045 | ⏳ | dataframe / table 表示要素を実装する | TASK-018 |
| TASK-046 | ⏳ | json / code 表示要素を実装する | TASK-018 |
| TASK-047 | ⏳ | columns / container レイアウト要素を実装する | TASK-016 |
| TASK-048 | ⏳ | expander / tabs レイアウト要素を実装する | TASK-047 |
| TASK-049 | ⏳ | sidebar レイアウト領域を実装する | TASK-047 |
| TASK-050 | ⏳ | empty 相当のプレースホルダー API を実装する | TASK-047 |
| TASK-051 | ⏳ | ファイルアップロードのストリーミング受信を実装する | TASK-032 |
| TASK-052 | ⏳ | file_uploader ウィジェットを実装する | TASK-051 |
| TASK-053 | ⏳ | アップロードのサイズ上限と MIME 検証を実装する | TASK-051 |
| TASK-054 | ⏳ | error メッセージ送信とフロントのスタックトレース表示を実装する | TASK-038 |
| TASK-055 | ⏳ | 構造化ログ（セッション ID・再実行 seq・所要時間）を実装する | TASK-032 |
| TASK-056 | ⏳ | アクティブセッション数等のメトリクスを実装する | TASK-029 |
| TASK-057 | ⏳ | ソース変更検知とブラウザ自動リロード機構を実装する | TASK-038 |
| TASK-058 | ⏳ | CLI モジュールと streamlit4j run コマンドを実装する | TASK-057 |
| TASK-059 | ⏳ | JBang による単一 .java ファイル起動を実装する | TASK-058 |
| TASK-060 | ⏳ | フロントで全要素ノードの描画コンポーネントを整備する | TASK-040,TASK-041,TASK-042,TASK-043,TASK-044,TASK-045,TASK-046,TASK-048,TASK-049 |
| TASK-061 | ⏳ | ページ宣言の規約ベース自動探索機構を実装する | TASK-016 |
| TASK-062 | ⏳ | St.pages による明示登録 API を実装する | TASK-016 |
| TASK-063 | ⏳ | ページ間ナビゲーション UI を実装する | TASK-061,TASK-062 |
| TASK-064 | ⏳ | URL ディープリンクとルーティング同期を実装する | TASK-063 |
| TASK-065 | ⏳ | form コンテナーと再実行抑制機構を実装する | TASK-032 |
| TASK-066 | ⏳ | form_submit_button と一括値確定を実装する | TASK-065 |
| TASK-067 | ⏳ | データキャッシュのアノテーション指定を実装する | TASK-016 |
| TASK-068 | ⏳ | データキャッシュのラッパー関数指定を実装する | TASK-067 |
| TASK-069 | ⏳ | 引数ハッシュによるキャッシュキー生成を実装する | TASK-067 |
| TASK-070 | ⏳ | データキャッシュの TTL と無効化を実装する | TASK-069 |
| TASK-071 | ⏳ | リソースキャッシュとスレッドセーフ共有を実装する | TASK-067 |
| TASK-072 | ⏳ | line_chart チャート要素を実装する | TASK-018 |
| TASK-073 | ⏳ | bar_chart チャート要素を実装する | TASK-018 |
| TASK-074 | ⏳ | area_chart チャート要素を実装する | TASK-018 |
| TASK-075 | ⏳ | scatter_chart チャート要素を実装する | TASK-018 |
| TASK-076 | ⏳ | テーマ切替機構（ライト/ダーク）を実装する | TASK-023 |
| TASK-077 | ⏳ | カスタムカラーパレット適用を実装する | TASK-076 |
| TASK-078 | ⏳ | アクセシビリティ（コントラスト/フォーカス）を整備する | TASK-076 |
| TASK-079 | ⏳ | キーボード操作対応をフロント全体に適用する | TASK-078 |
| TASK-080 | ⏳ | download_button とファイル配信エンドポイントを実装する | TASK-021 |
| TASK-081 | ⏳ | CSV/画像/PDF 生成物の配信パイプラインを実装する | TASK-080 |
| TASK-082 | ⏳ | toast 通知要素を実装する | TASK-018 |
| TASK-083 | ⏳ | progress / spinner / status 要素を実装する | TASK-018 |
| TASK-084 | ⏳ | image / audio / video 表示要素を実装する | TASK-018 |
| TASK-085 | ⏳ | divider / caption / subheader 補助要素を実装する | TASK-018 |
| TASK-086 | ⏳ | color_picker / time_input / select_slider 入力要素を実装する | TASK-035 |
| TASK-087 | ⏳ | text_area / data_editor 追加入力要素を実装する | TASK-035 |
| TASK-088 | ⏳ | latex / html 表示要素を実装する | TASK-018 |
| TASK-089 | ⏳ | 主要 40 要素のカタログ整合性を検証する | TASK-082,TASK-083,TASK-084,TASK-085,TASK-086,TASK-087,TASK-088 |
| TASK-090 | ⏳ | core と server のモジュール境界を整理する | TASK-021 |
| TASK-091 | ⏳ | core を Web フレームワーク非依存に分離する | TASK-090 |
| TASK-092 | ⏳ | spring-boot-starter モジュールを新設する | TASK-091 |
| TASK-093 | ⏳ | auto-configuration で指定パスにマウントする | TASK-092 |
| TASK-094 | ⏳ | Spring Security への認証委譲アダプターを実装する | TASK-093 |
| TASK-095 | ⏳ | Spring Session への委譲アダプターを実装する | TASK-093 |
| TASK-096 | ⏳ | 埋め込みパス配下のリソース提供を実装する | TASK-093 |
| TASK-097 | ⏳ | カスタムコンポーネント宣言用の型安全 API を実装する | TASK-016 |
| TASK-098 | ⏳ | コンポーネントの引数/戻り値シリアライザーを実装する | TASK-097 |
| TASK-099 | ⏳ | インプロセス component の登録機構を実装する | TASK-097 |
| TASK-100 | ⏳ | 同梱バンドルへの React 部品登録パイプラインを構築する | TASK-099 |
| TASK-101 | ⏳ | iframe 隔離 component のホスト機構を実装する | TASK-097 |
| TASK-102 | ⏳ | iframe sandbox 属性と CSP を適用する | TASK-101 |
| TASK-103 | ⏳ | iframe component の値検証と境界チェックを実装する | TASK-101 |
| TASK-104 | ⏳ | フロント TS SDK の値受け渡し API を実装する | TASK-098 |
| TASK-105 | ⏳ | フロント TS SDK の再描画通知ブリッジを実装する | TASK-104 |
| TASK-106 | ⏳ | streamlit4j component create 雛形生成コマンドを実装する | TASK-104 |
| TASK-107 | ⏳ | Spring Boot 埋め込みサンプルアプリを作成する | TASK-094,TASK-095,TASK-096 |
| TASK-108 | ⏳ | カスタムコンポーネントサンプル（インプロセス/iframe）を作成する | TASK-100,TASK-103 |
| TASK-109 | ⏳ | 未決事項（終端 API 名/プロトコル等）を ADR で最終決定する | - |
| TASK-110 | ⏳ | Apache-2.0 ライセンスを全モジュールに適用する | - |
| TASK-111 | ⏳ | NOTICE と third-party ライセンス一覧を整備する | TASK-110 |
| TASK-112 | ⏳ | 独立 OSS 旨の disclaimer を README 冒頭に明記する | - |
| TASK-113 | ⏳ | 独立 OSS 旨の disclaimer を公式ドキュメント冒頭に明記する | TASK-115 |
| TASK-114 | ⏳ | 独自ロゴと独自カラーパレットを策定しテーマに反映する | TASK-077 |
| TASK-115 | ⏳ | 公式ドキュメントサイトの基盤を構築する | - |
| TASK-116 | ⏳ | Getting Started チュートリアルを執筆する | TASK-115 |
| TASK-117 | ⏳ | 機能リファレンスをドキュメントに整備する | TASK-115,TASK-089 |
| TASK-118 | ⏳ | Javadoc 公開ビルドを整備する | TASK-001 |
| TASK-119 | ⏳ | examples モジュールに代表サンプル群を整備する | TASK-060,TASK-107 |
| TASK-120 | ⏳ | Spring Boot 統合手順をドキュメント化する | TASK-107,TASK-115 |
| TASK-121 | ⏳ | カスタムコンポーネント作成ガイドを執筆する | TASK-106,TASK-115 |
| TASK-122 | ⏳ | Maven Central パブリッシュ用 POM とメタデータを整備する | TASK-110 |
| TASK-123 | ⏳ | GPG 署名と Sonatype アカウント設定を整備する | TASK-122 |
| TASK-124 | ⏳ | Maven 利用者向け導入手順を整備する | TASK-122 |
| TASK-125 | ⏳ | JBang カタログに CLI を登録する | TASK-059 |
| TASK-126 | ⏳ | playground（ホストされたデモ）を構築する | TASK-119 |
| TASK-127 | ⏳ | コア・互換要素・統合機能の E2E テストスイートを整備する | TASK-060,TASK-064,TASK-066,TASK-070,TASK-071,TASK-081,TASK-089,TASK-107,TASK-108 |
| TASK-128 | ⏳ | 0.1.0 リリースタグとリリースノートを作成する | TASK-109,TASK-111,TASK-112,TASK-113,TASK-114,TASK-116,TASK-117,TASK-118,TASK-120,TASK-121,TASK-124,TASK-125,TASK-127 |
| TASK-129 | ⏳ | Maven Central へ 0.1.0 を公開する | TASK-123,TASK-128 |
| TASK-130 | ⏳ | playground を 0.1.0 ビルドに更新し公開する | TASK-126,TASK-129 |

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

### TASK-102

- 補足: postMessage の origin 検証を必須とする
- 注意: CSP の nonce 戦略を文書化する

### TASK-109

- 補足: 終端メソッド名・プロトコル選定・キャッシュ指定方式・GraalVM 時期・ライセンス・マルチページ既定方式を確定する
- 注意: 決定理由と却下案を ADR として残す

### TASK-112

- 補足: Snowflake 社および Streamlit プロジェクトとは無関係と明記する
- 注意: nominative fair use の第三要件を担保する文面とする

### TASK-114

- 補足: Streamlit のロゴ/カラー/公式マークを流用しない
- 注意: 商用利用可能なライセンスで配布できるアセットとする

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

## Backlog詳細

### BACKLOG-003

- 補足: エンベロープのバージョニングを利用して段階移行する
- 注意: v1 では JSON 維持

### BACKLOG-005

- 補足: reflection 設定の網羅が必要
- 注意: 着手時期は TASK-109 で決定する
