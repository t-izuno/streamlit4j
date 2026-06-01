# streamlit4j

An idiomatic, open-source Java framework for building interactive data apps and dashboards on the JVM
— inspired by Streamlit, built for Java.

> Independent community OSS. Not affiliated with Snowflake, Inc. or the Streamlit project.

## Requirements

- JDK 21 LTS（24 以下も可。JDK 25 は formatter 内部 API 非互換のため未サポート）
- Maven 3.9+
- Node.js 22+（frontend のビルド/テスト時のみ）

## Modules

| Module | Description |
| --- | --- |
| `core` | Web フレームワーク非依存の実行エンジン |
| `server` | 組み込み HTTP/WS サーバー |
| `frontend-assets` | 事前ビルド済みフロントを jar 同梱 |
| `cli` | スタンドアロン CLI (JBang 対応) |
| `spring-boot-starter` | Spring Boot 統合 |
| `examples` | サンプルアプリ |
| `frontend/` (非 Maven) | React + Vite + TS フロントエンドソース |

## Install via JBang

```sh
jbang app install streamlit4j@t-izuno/streamlit4j
streamlit4j 8501
```

カタログ定義は `jbang-catalog.json`。

## Build

```sh
# Java 全モジュール: lint + format + test + coverage
mvn verify

# フロントエンド
cd frontend
npm ci
npm run lint
npm run format:check
npm test
npm run build
```

## Documentation

- 要件: [`docs/requirements.md`](docs/requirements.md)
- 仕様: [`docs/specification.md`](docs/specification.md)
- 設計: [`docs/design.md`](docs/design.md)
- 実行計画: [`docs/tasks/task.md`](docs/tasks/task.md)

## License

Apache License 2.0
