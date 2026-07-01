# ADR-0002: プロトコルは JSON（Jackson）を採用する

- 状態: Accepted
- 日付: 2026-06-05
- 関連: `core/src/main/java/io/streamlit4j/core/protocol/Codec.java`

## Context

サーバーとブラウザー間のプロトコルペイロードは次の 2 候補があった。

1. **JSON**: ブラウザー標準の `JSON.parse` / `JSON.stringify`、Java 側は Jackson
2. **MessagePack**: バイナリーで小さく速い。ただし両端で専用ライブラリーを要する

streamlit4j のメッセージは `session_init` / `render_delta` / `widget_event` /
`file_upload` / `error` / `reload` の数種類で、render_delta は keyed diff 後の
最小パッチ列のため、1 回あたりのペイロードは小〜中規模。

## Decision

v1 は **JSON（Jackson）** を採用する。MessagePack は採用しない。

主な理由:

- **ブラウザー側ゼロ依存**: `JSON.parse` がネイティブ実装で、SPA バンドルに追加デコーダーを含めずに済む
- **既存資産の活用**: Java 側は Jackson が事実上の標準で、Records / sealed / `@JsonTypeInfo` 等の機能成熟度が高い
- **可観測性**: DevTools の Network タブでメッセージが可読のため、開発・デバッグ・障害解析が容易
- **早期最適化の回避**: render_delta は keyed diff 後の最小パッチ列で 1 回あたりの
  ペイロードが小〜中規模。MessagePack の利得（バイト数削減）は計測上の
  ボトルネックが顕在化するまで不要
- **抽象化済み**: `Codec` クラスを介しているため、将来ベンチマーク結果で必要になれば別 ADR で MessagePack へ差し替え可能

## Consequences

良い影響:

- ブラウザー側にデコーダー実装が不要、`event.data` をそのまま JSON.parse
- Java 側は Jackson のエコシステム（Records / sealed / `@JsonTypeInfo`）が成熟
- DevTools の Network タブでメッセージが可読、デバッグ容易性が高い
- Jackson `ObjectMapper` は `Codec.mapper()` で共有し、`ComponentCodec` 等から再利用

悪い影響:

- 同じデータ量で MessagePack より大きい
- 数値の精度（とくに Long / BigDecimal）に JSON 仕様上の上限がある（53bit）

## Alternatives Considered

- **MessagePack 採用**: バイナリー利点はあるが、まだ計測上のボトルネックではなく早期最適化となる
- **MessagePack へ後で差し替え**: 設計上は可能。`Codec` クラスを抽象化しているので、ベンチマーク結果で必要になった時点で別 ADR で見直す
- **Protocol Buffers**: スキーマ定義の運用負荷が大きく、UI 部品の追加ごとに `.proto` 更新が必要
