# Architecture Decision Records

設計上の重要決定をテンプレート（Context / Decision / Consequences / Alternatives Considered）に沿って記録する。各 ADR は一度確定したら原則として書き換えず、覆す場合は新しい ADR で `Supersedes: ADR-xxxx` を明記する。

ADR にはアーキテクチャー境界・非機能特性・外部依存・セキュリティーモデルなどに影響する判断のみを残す。命名や API スタイルなどの実装レベルの選択は `design.md` / `specification.md` に併記する。

## 一覧

| ID | 状態 | タイトル |
| --- | --- | --- |
| [ADR-0002](./0002-json-over-messagepack.md) | Accepted | プロトコルは JSON（Jackson）を採用する |
| [ADR-0004](./0004-graalvm-deferred.md) | Accepted | GraalVM ネイティブ対応は v1.x 以降へ繰り延べる |
| [ADR-0005](./0005-explicit-page-registration.md) | Accepted | マルチページは規約ベースではなく明示登録を既定とする |
| [ADR-0006](./0006-mit-license.md) | Accepted | ライセンスは MIT とする |
| [ADR-0007](./0007-no-iframe-components.md) | Accepted | カスタムコンポーネントは iframe 隔離を採らず in-process のみとする |
| [ADR-0008](./0008-react-frontend.md) | Accepted | フロント UI フレームワークに React 18 を採用する |
| [ADR-0009](./0009-vite-for-frontend.md) | Accepted | フロントエンドビルドツールに Vite を採用する |
| [ADR-0010](./0010-vitepress-for-docs.md) | Accepted | ドキュメントサイトに VitePress を採用する |
| [ADR-0011](./0011-jetty-embedded-server.md) | Accepted | 組み込みサーバーに Jetty 12 を採用する |

VitePress を含むサイドバーへの追加と、ブランドカラー反映は TASK-114 で同時に行った。

Java / Maven は streamlit4j の前提（言語と OSS 公開先）であり、選択肢を比較する性質の判断ではないため ADR の対象外。

ADR-0001 / ADR-0003 は実装レベル（API 命名・API スタイル）の選択と整理し、ADR から除外して `design.md` に併記する形へ移行した。
