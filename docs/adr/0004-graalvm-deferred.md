# ADR-0004: GraalVM ネイティブ対応は v1.x 以降へ繰り延べる

- 状態: Accepted
- 日付: 2026-06-05

## Context

GraalVM `native-image` による AOT コンパイルは起動時間 / メモリーフットプリント
で大きな差別化要因になる一方、reflection / proxy / リソース読み込みに対して
`reflect-config.json` / `resource-config.json` 等の設定が必要になる。
Jetty / Spring Boot / Jackson を組み合わせる本プロジェクトでは、これらの設定を
初版ですべて整える負荷が大きい。

## Decision

**v1（0.1.0）には GraalVM ネイティブ対応を含めない**。標準 JVM（Java 21 LTS）での動作を最優先とする。GraalVM 対応は v1.x の更新で追加する。

主な理由:

- **初版スコープの管理**: reflect-config / resource-config / proxy-config を Jetty / Spring Boot / Jackson すべてで初版から整える負荷が大きく、コア機能の完成を遅らせる
- **依存ライブラリーの成熟待ち**: Jackson / Jetty 各ライブラリーが提供する GraalVM サポート（reachability metadata）が継続的に改善されており、後着手の方が個別設定の必要量が減る
- **後付け可能**: ネイティブ化は API 互換性を壊さない後方互換な改善で、v1.x のマイナーアップデートで導入しても利用者影響が少ない
- **検証コスト**: ネイティブ対応の継続的検証には専用 CI が必要で、初版ではテスト基盤を JVM 側に集中させた方がリリース品質を担保しやすい

## Consequences

良い影響:

- リリースまでの作業量を抑えられる
- Jackson / Jetty 各ライブラリーの GraalVM サポート成熟を待てる
- reflect-config の保守負荷を初版から負わずに済む

悪い影響:

- 起動時間 / メモリーで他言語フレームワークに見劣りする
- v1.x で対応する際に、後付けで全モジュールの reflection 利用点を洗い直す必要が
  ある（とくに [ADR-0002](./0002-json-over-messagepack.md) の Jackson、
  Spring Boot starter）

## Alternatives Considered

- **v1 から対応**: スコープが大きく、コア機能の完成が遅れる
- **対応を完全に放棄**: 中期的な競争力で不利
- **AppCDS のみ**: 起動時間の改善幅は小さく、メモリー削減は GraalVM ほどではないため差別化にならない
