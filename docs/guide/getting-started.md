# Getting Started

streamlit4j を実際に動かして評価するための導入手順。所要時間 10〜20 分。

## 前提

| ツール | バージョン | 用途 |
| --- | --- | --- |
| JDK | 21 LTS（24 以下も可） | 実行時必須。JDK 25 は formatter 内部 API 非互換のため未サポート |
| Maven Wrapper | リポジトリー同梱の `./mvnw` | `JAVA_HOME` が JDK 21 を指していれば追加インストール不要 |
| Node.js | 22+ | フロントを改造したい場合のみ |

> 0.1.0 はまだ Maven Central に公開されていません。当面はソースから `./mvnw install` でローカルリポジトリーへ配置するか、JBang 経由で CLI を取得してください。

## 評価コースの選択

| コース | 所要 | 何が確認できるか |
| --- | --- | --- |
| **A**: バンドル済み Hello を CLI で動かす | 約 3 分 | 起動 / WebSocket 接続 / 基本ウィジェット |
| **B**: ライブラリーとして取り込み独自スクリプト | 約 10 分 | API 感触 / 自分のロジックとの統合容易性 |
| **C**: Spring Boot Starter で既存アプリへマウント | 約 15 分 | Spring Security / Session 連携 |
| **D**: 機能カタログを一通り眺める | 約 5 分 | 提供ウィジェットの網羅性 |

## A. バンドル済み Hello デモを動かす

```sh
git clone https://github.com/t-izuno/streamlit4j.git
cd streamlit4j
./mvnw -DskipTests install

# 8501 はリッスンポート
java -jar cli/target/streamlit4j-cli-0.1.0-SNAPSHOT.jar 8501
```

ブラウザーで <http://localhost:8501> を開くと、`examples/Hello.java` の内容（タイトル / マークダウン / スライダー / メトリック / ボタン）が表示される。

確認ポイント:

- スライダーを動かすと右側のメトリックが追従するか
- 「Greet」ボタンを押すとトースト通知が出るか
- ブラウザー DevTools の Network タブで `ws://localhost:8501/ws` を流れる JSON envelope を観察

## B. ライブラリーとして取り込む

`pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>io.streamlit4j</groupId>
    <artifactId>streamlit4j-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </dependency>
  <dependency>
    <groupId>io.streamlit4j</groupId>
    <artifactId>streamlit4j-server</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </dependency>
</dependencies>
```

`src/main/java/com/example/MyApp.java`:

```java
package com.example;

import io.streamlit4j.core.api.St;
import io.streamlit4j.server.Streamlit4jServer;

public final class MyApp {

  public static void main(String[] args) throws Exception {
    try (var server = new Streamlit4jServer(8501, () -> MyApp::render)) {
      server.start();
      System.out.println("Open http://localhost:" + server.port());
      Thread.currentThread().join();
    }
  }

  static void render() {
    St.title("Sales dashboard");
    St.markdown("Pick a target month:");
    int month = St.slider("Month", 1, 12, 6);
    St.metric("Selected month", month);
    if (St.button("Submit")) {
      St.toast("Submitted month " + month);
    }
  }
}
```

実行:

```sh
./mvnw -q exec:java -Dexec.mainClass=com.example.MyApp
```

確認ポイント:

- `render()` を編集 → 再起動だけで UI が変わるか
- セッションを別タブで開くと独立した state を持つか（同一スライダー位置にならないか）

## C. Spring Boot Starter を試す

詳細手順は [Spring Boot Integration](./spring-boot) を参照。要点だけ:

```xml
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-spring-boot-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

```yaml
streamlit4j:
  base-path: /streamlit
```

`@Bean EntrypointSource` を 1 つ宣言すれば `${streamlit4j.base-path}` 配下で動く。

確認ポイント:

- 既存 Spring Security の `SecurityFilterChain` でアクセス制御が効くか
- Spring Session（Redis 等）と組み合わせて HTTP セッション破棄 → streamlit4j セッション連動消滅するか

## D. 機能カタログを眺める

`examples/` に主要機能のサンプルが揃っている。

| デモ | 確認できる要素 |
| --- | --- |
| `Hello` | title / markdown / slider / metric / button / toast |
| `WidgetsDemo` | text / number / select / radio / checkbox / button / slider / date / time / colorPicker |
| `DataDemo` | dataframe / table / line / bar / area / scatter / metric / cache |
| `LayoutDemo` | columns / container / expander / tabs / sidebar |
| `ComponentDemo` | カスタムコンポーネント（star-rating） |

`cli` の `--watch` フラグでスクリプト変更時にフロントへリロード通知も送れる:

```sh
java -jar cli/target/streamlit4j-cli-0.1.0-SNAPSHOT.jar 8501 --watch ./src/main/java
```

## 再実行モデルの理解

各 WebSocket セッションごとに **仮想スレッド** 上で `render()` が実行される。ウィジェットが変更されると:

1. 新しい値でセッション状態を更新
2. `render()` を再実行（同セッション内では直列化）
3. 直前のレンダーツリーと keyed diff を取り、`render_delta` を送信
4. フロントがパッチを適用

スクリプトを書き換えるだけで状態管理が完結する Streamlit と同じモデル。

## 評価チェックリスト

採用判断に向けた観点。

- [ ] 必要なウィジェットが揃っているか（[Reference](../reference/overview) で網羅）
- [ ] チャートが用途に足りるか（v1 はプレースホルダー描画のみ）
- [ ] パフォーマンスが要件を満たすか（仮想スレッド前提・1 セッション 1 スレッド）
- [ ] セキュリティーモデルが社内ポリシーと整合するか（Spring Security 連携可 / カスタムコンポーネントは内製のみ）
- [ ] ライセンス（MIT）が社内基準で許容されるか
- [ ] 制約事項（README §「制約事項」）が許容範囲か

## 次の一歩

- [Reference](../reference/overview) — 全 API 一覧
- [Custom Components Guide](./custom-components) — 独自 React 部品の追加
- [Spring Boot Integration](./spring-boot) — auto-config と Session / Security 連携
- [Design](../design) / [ADRs](../adr/) — 内部アーキテクチャーと設計判断
