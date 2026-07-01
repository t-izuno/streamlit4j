# ADR-0012: 標準トランスポートは SSE + HTTP POST とする

- 状態: Accepted
- 日付: 2026-07-01
- 関連: `docs/devel/transport-comparison.md`, `frontend/e2e/smoke.spec.ts`

## Context

LLM UI では、サーバーからブラウザーへ token / render_delta を順次送り、
ブラウザーからサーバーへ widget event / cancel / retry / edit regenerate を送る。
候補は次の 2 つ。

1. **SSE + HTTP POST**: サーバーからブラウザーへのイベントは `EventSource`、
   ブラウザーからサーバーへのイベントは HTTP POST で送る
2. **WebSocket**: 1 本の双方向接続で JSON envelope を送受信する

Playwright E2E は SSE + HTTP POST と WebSocket の両方で通っている。一方、
ユーザー環境では WebSocket の利用可否が怪しく、通信プロトコルは SSE を標準にしたい
という制約がある。

## Decision

標準トランスポートは **SSE + HTTP POST** とする。

WebSocket は削除せず、互換トランスポートとして扱う。ただし WebSocket は
「動く環境では使える代替」であり、標準・必須経路にはしない。

## Rationale

- **LLM token 配信に合う**: 主経路は server-to-browser の逐次イベントであり、
  SSE の `text/event-stream` が用途に合う
- **認証境界が単純**: SSE と POST は通常の HTTP request として扱えるため、
  Cookie / redirect / proxy / access log との相性がよい
- **再接続が仕様化されている**: `EventSource` には reconnect と `Last-Event-ID`
  の仕組みがある
- **WebSocket 制約を避ける**: RFC 6455 の WebSocket は HTTP Upgrade handshake を
  必要とするため、Upgrade を許可しない proxy / gateway で失敗しうる
- **既存実装は活かす**: WebSocket E2E も通っているため、比較対象と
  fallback として維持する価値がある

## Consequences

良い影響:

- WebSocket が使えない環境でも標準経路を提供できる
- 認証、プロキシ、監査ログ、キャンセル POST を HTTP 境界で扱いやすい
- WebSocket 実装を残すことで、低遅延双方向用途や既存 E2E を継続利用できる

悪い影響:

- `EventSource` は受信専用のため、送信用 HTTP POST endpoint が別途必要
- HTTP/1.1 ではブラウザー + domain ごとの SSE 接続数制限に注意が必要
- SSE と WebSocket の 2 経路を維持するため、E2E マトリクスの実行時間が増える

## Required Follow-up

- HTTP/1.1 多タブ時の SSE 接続数制限と HTTP/2 時の同時 stream 数を検証する
- 認証付き本番 proxy / gateway 配下で SSE と POST のタイムアウト設定を検証する

## Alternatives Considered

- **WebSocket を標準にする**: 既存実装はあるが、HTTP Upgrade 制約が今回の利用環境に
  合わない可能性が高いため不採用
- **SSE のみ対応する**: 制約面では単純だが、既存 WebSocket 実装と E2E 実績を捨てる
  利点が小さいため不採用
- **transport 決定を延期する**: 評価軸改善に必要な要件が固まらないため不採用
