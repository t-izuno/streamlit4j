# Getting Started

This page walks you through running your first streamlit4j app — first via the
standalone CLI, then as a Maven dependency in your own Java project.

## Prerequisites

| Tool | Version | Notes |
| --- | --- | --- |
| JDK | 21 LTS (24 以下も可) | JDK 25 は formatter 内部 API 非互換のため未サポート |
| Maven | 3.9+ | wrapped Maven (mvnw) は未提供 |
| Node.js | 22+ | フロントエンドや docs サイトをローカルで触る場合のみ |

streamlit4j 0.1.0 はまだ Maven Central に公開されていません。当面はソースから
`mvn install` でローカルリポジトリへ配置するか、後日 JBang から取得します。

## 1. Run the bundled Hello app via the CLI

```sh
git clone https://github.com/t-izuno/streamlit4j.git
cd streamlit4j
mvn -DskipTests install

# 8501 はリッスンポート
java -jar cli/target/streamlit4j-cli-0.1.0-SNAPSHOT.jar 8501
```

ブラウザで <http://localhost:8501> を開くと、`examples/Hello.java` の内容
（タイトル・マークダウン・スライダー・メトリック・ボタン）が表示されます。

## 2. Write your own app

`pom.xml`：

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

`src/main/java/com/example/MyApp.java`：

```java
package com.example;

import io.streamlit4j.core.St;
import io.streamlit4j.server.Streamlit4jServer;

public final class MyApp {

  public static void main(String[] args) throws Exception {
    try (Streamlit4jServer server = new Streamlit4jServer(8501, () -> MyApp::render)) {
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

実行：

```sh
mvn -q exec:java -Dexec.mainClass=com.example.MyApp
```

Hot reload はサンプル CLI の `--watch` フラグで利用できます（後述）。

## 3. Re-run model — what just happened

各 WebSocket セッションごとに **仮想スレッド** 上で `render()` が実行されます。
ウィジェットが変更されると、サーバーは：

1. 新しい値でセッション状態を更新
2. `render()` を再実行（同セッション内では直列化）
3. 直前のレンダーツリーと keyed diff を取り、`render_delta` を送信
4. フロントがパッチを適用

スクリプトを書き換えるだけで状態管理が完結する、Streamlit と同じ「スクリプト再実行」モデルです。

## 4. Watch mode (live reload)

`cli` モジュールには `--watch <dir>` フラグがあります（TASK-057 / TASK-058）。
指定ディレクトリ配下の `.java` 変更を検知すると、サーバーがフロントにリロード
通知を送ります。

```sh
java -jar cli/target/streamlit4j-cli-0.1.0-SNAPSHOT.jar 8501 --watch ./src/main/java
```

## Next steps

- [Spring Boot Integration](./spring-boot) — auto-config と `${streamlit4j.base-path}` でのマウント
- [Specification](../specification) — WebSocket エンベロープと DTO 契約
- [Design](../design) — Clean Architecture (lite) と runtime 構成
