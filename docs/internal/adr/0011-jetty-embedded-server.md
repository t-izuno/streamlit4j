# ADR-0011: 組み込みサーバーに Jetty 12 を採用する

- 状態: Accepted（遡及記録）
- 日付: 2026-06-05
- 関連: `server/pom.xml`、`pom.xml`（`<jetty.version>12.0.14`）、`design.md` §8

## Context

streamlit4j は CLI / JBang 単体起動と Spring Boot 埋め込みの両モードを提供する。スタンドアロン側で WebSocket と静的リソース配信を行う組み込み HTTP サーバーが必要。候補は次のとおり。

| 候補 | API レイヤー | WebSocket 実装 | 組み込み起動 | 主な利点 |
| --- | --- | --- | --- | --- |
| Jetty 12 | Servlet 6 / Jakarta EE / 独自 Handler | 公式 `jetty-websocket-jetty-server` | 公式サポート | 軽量・低レイテンシー・組み込み実績豊富 |
| Undertow | Servlet 5 / 独自 XNIO | 独自 WebSocket | 公式サポート | 低メモリー・JBoss 系で実績 |
| Netty | 低レベル NIO | 自前実装が必要 | 公式サポート | 生 NIO 最速だが上位 API を自作する負担大 |
| Tomcat（埋め込み） | Servlet 6 / Jakarta EE | 標準 WebSocket | 公式サポート | Spring Boot 既定だがスタンドアロン用途で重い |

参考:

- WebSocket Frameworks performance comparison（IEEE）: <https://ieeexplore.ieee.org/document/8605989/>
- Spring Boot 埋め込みサーバー比較: <https://medium.com/@skhatri.dev/springboot-performance-testing-various-embedded-web-servers-7d460bbfdb1b>

## Decision

スタンドアロンモードの組み込み HTTP サーバーに **Eclipse Jetty 12** を採用する。
Spring Boot 埋め込みモード（`spring-boot-starter` モジュール）では Spring Boot
既定の Tomcat をそのまま使用し、Jetty とは独立に共存させる。

主な理由:

- **WebSocket の公式上位 API がある**: `jetty-websocket-jetty-server` で `@WebSocket` アノテーション / Listener API が直接使え、Netty のように自前でフレーミングを書かなくてよい
- **組み込み起動の薄さ**: `new Server(port).start()` 相当で起動でき、Tomcat 埋め込みのような ServletContext 初期化のオーバーヘッドがない
- **メモリー / レイテンシー特性**: 持続接続を多く張る対話型ワークロードにおいて Tomcat より軽量。Netty ほど低レベルではないが、上位 API の整備度で勝る
- **Spring Boot との非干渉**: Spring Boot 統合側は Tomcat 既定を変えないので、利用者の Spring Boot アプリ設定（管理エンドポイント / actuator）がそのまま動く
- **長期メンテナンス**: Jetty 12 系は Jakarta EE 10 ベースで、JDK 21 + 仮想スレッドとの整合性も公式に確認済み

## Consequences

良い影響:

- スタンドアロン起動が高速（数百 ms オーダー）でメモリーフットプリントが小さい
- WebSocket 実装の保守をフレームワークに委譲できる
- Spring Boot 統合時に既存ユーザーの Tomcat 前提アプリへ非干渉

悪い影響:

- Spring Boot 統合側と CLI 側で 2 種類のサーバー実装（Tomcat / Jetty）がプロジェクト内に同居する。WebSocket ハンドラーは抽象化したが、サーバー固有の設定項目はモード別に分かれる
- Netty を採用した場合と比べると、極限の同時接続数（100 万級）には到達しない（streamlit4j のユースケースではそこまで不要）

## Alternatives Considered

- **Netty**: 生 NIO で最速だが、Servlet / WebSocket 上位 API を自作する負担が大きい。本プロジェクトのスコープに合わない
- **Undertow**: メモリーは Jetty 並みかそれ以下だが、独自 XNIO に依存し、Spring Boot 統合の WebSocket 実装と分離設計するのが面倒
- **埋め込み Tomcat**: 起動オーバーヘッドが大きく、スタンドアロン CLI 用途では不利。Spring Boot 統合側では既定として使うが、CLI で採る理由は薄い
