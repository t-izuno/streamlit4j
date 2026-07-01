# streamlit4j

> English: [README.md](./README.md)

Java 向けの対話型データアプリ / ダッシュボードフレームワーク。Streamlit Python と同じ「スクリプト再実行 + 自動再描画」モデルを JVM 上で実現する。

> **Independent community open-source software.** streamlit4j is not affiliated with,
> endorsed by, or sponsored by Snowflake, Inc. or the Streamlit project. The name
> "Streamlit" appears within "streamlit4j" solely as nominative fair use to describe
> this project's design lineage; "Streamlit" is a trademark of its respective owner
> and no trademark claim is asserted by this project.

## 何ができるか

`St.*` の静的メソッド呼び出しを並べるだけで、SSE + HTTP POST + React 製の UI が自動生成される。
WebSocket は互換トランスポートとして利用できる。

```java
import io.streamlit4j.core.api.St;

public final class SalesDashboard {
  public static void run() {
    St.title("Sales dashboard");
    int year = St.slider("Year", 2020, 2030, 2026);
    St.metric("Selected", year);
    St.lineChart(loadSales(year));
  }
}
```

提供されるカテゴリーは次のとおり（[reference 一覧](docs/public/reference/overview.md)）。

| カテゴリー | 主な要素 |
| --- | --- |
| テキスト | title / header / markdown / write / code / latex / html / divider |
| ステータス | metric / toast / progress / spinner / status |
| 表 / グラフ | dataframe / table / data_editor / line / bar / area / scatter |
| 入力 | slider / textInput / selectbox / button / date / time / colorPicker など 14 種 |
| ファイル | fileUploader / downloadButton（バイト列対応） / downloadCsv / downloadJson |
| レイアウト | columns / container / expander / tabs / sidebar / empty |
| その他 | form / cache / pages / カスタムコンポーネント / rerun / state |

## 導入方法（2 つの選択肢）

streamlit4j はライブラリーであり、ユーザーが書いた Java コードからフレームワークが UI を駆動する。利用シナリオに応じて次の 2 形態から選べる。

| 形態 | 用途 | 起点 |
| --- | --- | --- |
| **A. ライブラリー（core + server）** | 既存 Java プロジェクトに組み込む / 独自 `main` で起動 | `streamlit4j-core` + `streamlit4j-server` を依存に追加 |
| **B. Spring Boot Starter** | Spring Boot アプリの一機能としてマウント | `streamlit4j-spring-boot-starter` を依存に追加 |

> 動作イメージを先に見たい場合は、リポジトリーをクローンして対応するサブプロジェクトから
> 起動する。[`examples/embedded`](examples/embedded) にスタンドアロン `main`（A 候補）、
> [`examples/spring-boot`](examples/spring-boot) に `SpringBoot<Name>App`（B 候補）が
> 同じ 6 デモぶん同梱されている。詳細は [Run from source](docs/public/guide/run-from-source.md)。

### A. ライブラリー導入（独自スクリプト用）

`pom.xml`:

```xml
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-core</artifactId>
  <version>0.1.0</version>
</dependency>
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-server</artifactId>
  <version>0.1.0</version>
</dependency>
```

`main`:

```java
import io.streamlit4j.core.api.St;
import io.streamlit4j.server.Streamlit4jServer;

public final class App {
  public static void main(String[] args) throws Exception {
    try (var server = new Streamlit4jServer(8501, () -> App::render)) {
      server.start();
      Thread.currentThread().join();
    }
  }
  static void render() {
    St.title("Hello");
    St.write("Hello, " + St.textInput("Name", "world"));
  }
}
```

### B. Spring Boot Starter（既存 Web アプリへの組み込み）

```xml
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-spring-boot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

`application.yml`:

```yaml
streamlit4j:
  base-path: /streamlit4j       # 既定 /streamlit4j
```

`@Bean EntrypointSource` を 1 つ宣言するだけで、Spring Security / Spring Session と連携した状態でマウントされる。詳細は [Spring Boot Integration](docs/public/guide/spring-boot.md)。

## モジュール一覧

| Maven 座標 | 役割 |
| --- | --- |
| `io.streamlit4j:streamlit4j-core` | フレームワーク非依存の実行エンジン |
| `io.streamlit4j:streamlit4j-server` | 組み込み Jetty + SSE / WebSocket トランスポート |
| `io.streamlit4j:streamlit4j-frontend-assets` | 事前ビルド済みフロントを classpath に同梱 |
| `io.streamlit4j:streamlit4j-spring-boot-starter` | Spring Boot 自動構成 |
| `io.streamlit4j:streamlit4j-examples-embedded` | 自前 `main` + 組み込み `Streamlit4jServer` で起動するスタンドアロンデモ群 |
| `io.streamlit4j:streamlit4j-examples-spring-boot` | embedded デモを Spring Boot からマウントするランチャー群 |

## 制約事項

採用時に把握しておくべき主な制限。

- **Java 21 LTS 以上が必須**。仮想スレッド前提のため JDK 17 以下では動作しない
- **プロトコルは JSON 固定**。MessagePack 等は未対応（v1.x 以降の検討事項）
- **`dataEditor` の双方向同期は未実装**（編集値はサーバーに反映されない）
- **マルチページは明示登録のみ**。`pages/` ディレクトリー規約はサポートしない
- **カスタムコンポーネントは in-process 方式のみ**。iframe 隔離は採用しない
- **GraalVM ネイティブ対応は v1.x 以降**

## ドキュメント

- [Getting Started](docs/public/guide/getting-started.md) — ライブラリー / Spring Boot での導入手順
- [Run from source](docs/public/guide/run-from-source.md) — リポジトリーをクローンして同梱 ShowcaseDemo を起動
- [Reference](docs/public/reference/overview.md) — `St.*` 全要素の Java シグネチャー / プロトコル / フロント描画
- [Custom Components Guide](docs/public/guide/custom-components.md) — 独自 React レンダラーの追加
- [Spring Boot Integration](docs/public/guide/spring-boot.md) — auto-config と Security / Session 連携

## Contributing

ビルド / テスト / プルリクエストの手順は [CONTRIBUTING.md](CONTRIBUTING.md) を参照。

## ライセンス

[MIT License](LICENSE)
