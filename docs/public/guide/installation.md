# インストール

streamlit4j は **Maven Central** から `io.streamlit4j` グループ ID で配布されます。実行モードに合わせて依存を選択してください。

> **ステータス（0.1.0-SNAPSHOT）**: まだ Maven Central には公開されていません。当面はリポジトリーをクローンして `mvn -DskipTests install` でローカルビルドしてください。0.1.0 が公開されれば、以下の座標で直接解決できるようになります。

## Maven 座標

| モジュール | 座標 | 利用する場面 |
| --- | --- | --- |
| `streamlit4j-core` | `io.streamlit4j:streamlit4j-core` | 常に必要（公開 API とランタイム） |
| `streamlit4j-server` | `io.streamlit4j:streamlit4j-server` | スタンドアロン（組み込み HTTP/WS サーバー） |
| `streamlit4j-frontend-assets` | `io.streamlit4j:streamlit4j-frontend-assets` | 常に必要 — SPA jar を同梱 |
| `streamlit4j-spring-boot-starter` | `io.streamlit4j:streamlit4j-spring-boot-starter` | Spring Boot ホスト |
| `streamlit4j-cli` | `io.streamlit4j:streamlit4j-cli` | CLI / JBang 経由でスクリプトを実行 |

すべてのアーティファクトはバージョンを共有するため、BOM スタイルのプロパティで一度だけ定義するか、親の `dependencyManagement` をインポートしてください。

## スタンドアロン（組み込みサーバー）

```xml
<properties>
  <streamlit4j.version>0.1.0</streamlit4j.version>
</properties>

<dependencies>
  <dependency>
    <groupId>io.streamlit4j</groupId>
    <artifactId>streamlit4j-server</artifactId>
    <version>${streamlit4j.version}</version>
  </dependency>
  <!-- Optional: write your render() in your own module -->
  <dependency>
    <groupId>io.streamlit4j</groupId>
    <artifactId>streamlit4j-core</artifactId>
    <version>${streamlit4j.version}</version>
  </dependency>
</dependencies>
```

`streamlit4j-server` は `streamlit4j-core` と `streamlit4j-frontend-assets` を推移的に取り込みます。`core` を個別に宣言しているのは可読性のためだけです。

## Spring Boot

```xml
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-spring-boot-starter</artifactId>
  <version>${streamlit4j.version}</version>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

配線と設定プロパティの詳細は [Spring Boot Integration ガイド](./spring-boot) を参照してください。

## Gradle（Kotlin DSL）

```kotlin
dependencies {
  implementation("io.streamlit4j:streamlit4j-server:0.1.0")
  // または:
  // implementation("io.streamlit4j:streamlit4j-spring-boot-starter:0.1.0")
}
```

## JBang

完全な Maven プロジェクトを用意せずに 1 ファイルで試したい場合は、0.1.0 公開後に JBang 経由で CLI をインストールできます。

```sh
jbang app install streamlit4j@t-izuno/streamlit4j
streamlit4j 8501
```

カタログ定義はリポジトリールートの `jbang-catalog.json` にあります。

## JDK 要件

| コンポーネント | JDK |
| --- | --- |
| ランタイム | 21 LTS（24 以下も可） |
| ビルド（開発者 / コントリビューター） | 21 LTS — JDK 25 は formatter 内部 API 非互換のため未サポート |

streamlit4j は仮想スレッド（JEP 444 / JDK 21）と sealed 型に対するパターンマッチングに依存しているため、これより古い JDK はサポートされませんし、今後もサポートされません。

## スナップショット（開発者向け）

`0.1.0-SNAPSHOT` のビルドは公開されていません。現在の `main` ブランチに依存するには次のようにします。

```sh
git clone https://github.com/t-izuno/streamlit4j.git
cd streamlit4j
mvn -DskipTests install
# 後はあなたの pom.xml で version を 0.1.0-SNAPSHOT に
```

## 完全性の検証

0.1.0 が公開されると、各アーティファクトには分離型の GPG 署名（`*.asc`）が同梱されます。検証するには次のようにします。

```sh
gpg --verify streamlit4j-core-0.1.0.jar.asc streamlit4j-core-0.1.0.jar
```
