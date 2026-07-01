# SSE / WebSocket 制約比較

## 目的

LLM UI の標準トランスポートを決めるため、SSE + HTTP POST と WebSocket を、
認証、プロキシ、再接続、生成中キャンセル、E2E 安定性の観点で比較する。

## 参照元

- official: WHATWG HTML Living Standard, Server-sent events  
  <https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events>
- official: RFC 6455, The WebSocket Protocol  
  <https://www.rfc-editor.org/rfc/rfc6455>
- reference: MDN, Using server-sent events  
  <https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events>

## 比較

| 評価軸 | SSE + HTTP POST | WebSocket | 採用判断 |
| --- | --- | --- | --- |
| サーバーからブラウザーへの逐次配信 | `EventSource` と `text/event-stream` で標準化されている。UTF-8 のテキストイベントを順次受信できる | 双方向フレーム上で任意 JSON を送れる | SSE が LLM token 配信に直接合う |
| ブラウザーからサーバーへのイベント | `EventSource` は受信専用なので、`fetch` / HTTP POST を併用する | 同じ接続で送信できる | 双方向性だけなら WebSocket が単純。ただし通常の widget event は POST で十分 |
| 認証 | 通常の HTTP request と同じ Cookie / redirect / header 境界で扱いやすい。`withCredentials` も仕様化されている | Upgrade 後の長寿命接続になるため、プロキシや認証境界で追加検証が必要 | SSE 優先 |
| プロキシ | HTTP response stream として扱えるため、一般的な HTTP 経路に載せやすい | RFC 6455 は HTTP Upgrade handshake を使う。Upgrade を許可しない環境では失敗しうる | SSE 優先 |
| 再接続 | `EventSource` には再接続モデルと `Last-Event-ID` がある | アプリ側で reconnect / replay / ordering を設計する必要がある | SSE 優先 |
| 生成中キャンセル | POST で cancel event を送る。サーバー側 stream を中断する API が必要 | 同じ WebSocket に cancel frame を送る。接続断と cancel を区別する必要がある | 同等。ただし SSE + POST の方が HTTP ログで追いやすい |
| E2E 安定性 | Fake LLM シナリオを SSE + POST で通過済み | 同じ Fake LLM シナリオを `?transport=websocket` で通過済み | 標準は SSE、WebSocket は互換経路として維持 |
| ブラウザー接続数 | HTTP/1.1 ではブラウザー + domain ごとの SSE 接続数制限がある。HTTP/2 では同時 stream 数を交渉する | 1 接続で双方向通信できる | 多タブ時は HTTP/2 前提または接続共有設計が必要 |

## 現時点の実機確認

- WebSocket:
  - `MAVEN_REPO_LOCAL=/private/tmp/streamlit4j-m2 npm run test:e2e`
  - Fake LLM embedded server を起動し、`GET /`、静的 asset、初期 render、
    Stop / Retry / Edit regenerate の WebSocket payload と DOM 更新を確認済み
- SSE + HTTP POST:
  - `MAVEN_REPO_LOCAL=/private/tmp/streamlit4j-m2 npm run test:e2e`
  - Fake LLM embedded server を起動し、`EventSource` 接続、`POST /events` payload、
    Stop / Retry / Edit regenerate の DOM 更新を確認済み
  - `./mvnw -Dmaven.repo.local=/private/tmp/streamlit4j-m2 -pl server test`
    で `/events` の session_init と POST 後 render_delta を確認済み

## 結論

標準トランスポートは SSE + HTTP POST とする。理由は、LLM token 配信が
server-to-browser の順次イベントであり、認証・プロキシ・再接続の扱いが通常の HTTP
境界に近いからである。

WebSocket は実装と E2E 実績があるため、互換トランスポートとして残す。ただし、
WebSocket を唯一の標準にはしない。Upgrade 制約のある環境で動かない可能性を前提に、
標準経路の E2E は SSE + HTTP POST で維持する。
