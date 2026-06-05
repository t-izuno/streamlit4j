# Contributing

streamlit4j の開発・ビルド・テスト手順。利用者向けの導入方法は [README](README.md) を参照。

## 必要なツール

| ツール | バージョン | 用途 |
| --- | --- | --- |
| JDK | 21 LTS | コンパイル / 実行 |
| Maven Wrapper | 同梱の `./mvnw` | Maven 3.9.4 を自動取得 |
| Node.js | 22+ | `frontend/` および `docs/` のビルド・テスト |

別途 Maven をインストールする必要はない。`JAVA_HOME` が JDK 21 を指していれば `./mvnw` で完結する。

## モジュール構成

```text
core/                     ← フレームワーク非依存の実行エンジン
  ├ domain/               ← Session / SessionState / CustomComponent / Page
  ├ protocol/             ← WebSocket envelope と RenderNode（DTO）
  ├ port/                 ← SessionStore / Renderer 等の interface
  ├ application/          ← StartSession / ProcessWidgetEvent ユースケース
  ├ runtime/              ← ScriptRunner / RenderContext / WidgetIds 等
  ├ api/                  ← 公開ファサード St + カテゴリー別 package-private クラス
  └ bootstrap/            ← Bootstrap / Streamlit4jApplication

server/                   ← 組み込み Jetty + WebSocket
spring-boot-starter/      ← Spring Boot 自動構成 + Session / Security 統合
frontend-assets/          ← フロント dist を classpath 同梱（antrun でコピー）
cli/                      ← JBang 配布される CLI
examples/                 ← Hello / WidgetsDemo / DataDemo / LayoutDemo / ComponentDemo
frontend/                 ← React 18 + Vite + TypeScript の SPA
docs/                     ← VitePress ドキュメントサイト
```

依存方向は core 内で `domain → protocol → port → application → runtime → bootstrap` の inward 方向に限定（ArchUnit で強制）。

## ビルドとテスト

### Java 全モジュール

```sh
./mvnw verify
```

実行される検証:

- Spotless（palantir-java-format）
- Checkstyle
- JaCoCo カバレッジ
- ArchUnit（レイヤー違反 / threadlocal / virtual thread 局所化）
- JUnit 5 + AssertJ + Mockito
- Maven Enforcer（JDK 21 必須）

### 公開ビルド（Maven Central 想定）

```sh
./mvnw -P release -DskipTests package
```

`*-javadoc.jar` + `*-sources.jar` も生成。Javadoc は `<doclint>all</doclint>` + `<failOnWarnings>true</failOnWarnings>` で厳格モード。

### フロントエンド

```sh
cd frontend
npm ci
npm run lint            # ESLint
npm run lint:arch       # dependency-cruiser（components 横依存禁止など）
npm run format:check    # Prettier
npm test                # Vitest
npm run build           # Vite production build
```

E2E は Playwright を使用（`npm run e2e`、未統合タスクは TASK-127）。

### ドキュメントサイト

ドキュメントのコンテンツは `docs/` 配下、VitePress サイト基盤は `site/`
配下に分離されている。サイトを起動するには `site/` で操作する。

```sh
cd site
npm install
# Vite が docs/ 配下の .md からモジュール解決するため symlink を 1 本張る
ln -sfn ../site/node_modules ../docs/node_modules
npm run docs:dev        # ローカルプレビュー http://localhost:5173
npm run docs:build      # site/.vitepress/dist へ静的出力
```

`docs/node_modules` は `site/node_modules` への symlink で `.gitignore`
済み。VitePress の `srcDir: "../docs"` の関係で必要になる。

## コーディング規約

| 項目 | ルール |
| --- | --- |
| Java フォーマット | palantir-java-format（Spotless で自動適用） |
| Java 静的解析 | Checkstyle / ArchUnit |
| TypeScript フォーマット | Prettier |
| TypeScript 静的解析 | ESLint + dependency-cruiser |
| Markdown | markdownlint + テキスト校正くん（jtf-style + prh）。日本語スタイル詳細は [Markdown スキル](https://github.com/izuno4t/...) または `docs/adr/` の慣例に従う |
| 公開 API | Javadoc 必須（`<doclint>all</doclint>` で warning ゼロが必須） |

## アーキテクチャー判断

設計上の重要決定はすべて [`docs/adr/`](docs/adr/index.md) に ADR として記録する。新規 ADR を起こす場合は次の連番（現状 ADR-0011 まで存在）で追加。

実装レベル（API 命名や DX スタイルの選択）は ADR ではなく [`docs/design.md`](docs/design.md) §10-2「API 設計指針」に併記する。

## タスク管理

進行中のタスクは [`docs/tasks/task.md`](docs/tasks/task.md) を参照。0.1.0 リリースまでに着手予定のものから完了済みまで含まれる。

## プルリクエスト

- フォークして topic ブランチで作業
- ローカルで `./mvnw verify` および `cd frontend && npm test && npm run lint && npm run build` を通す
- 関連する ADR / design.md / specification.md / reference の更新を同 PR で行う
- コミットメッセージは `feat:` / `fix:` / `docs:` などの conventional 慣習に従う（既存履歴を参考）

## ライセンス

すべての貢献は MIT License で受け入れる（CLA は不要）。詳細は [LICENSE](LICENSE)。
