# ソースからクローンして動かす

JBang を入れずに、リポジトリーをクローンしてバンドル済みデモを起動するまでの最短手順。所要時間 5〜10 分。

> **このコースの想定**: 評価・動作確認・コントリビュート目的でローカル動作させたい方向け。
> JBang での one-liner 起動は [Installation](./installation#jbang) を参照してください。

## 前提

| ツール | バージョン | 確認コマンド |
| --- | --- | --- |
| JDK | 21 LTS（22 / 23 / 24 も可。**25 は未サポート**） | `java -version` |
| Git | 任意 | `git --version` |
| ネットワーク | Maven Central への HTTPS アウトバウンド | — |

Maven 本体のインストールは不要です。リポジトリー同梱の `./mvnw`（Maven Wrapper）が必要なバージョンを自動取得します。

### JDK 21 を持っていない場合

[SDKMAN!](https://sdkman.io/) 経由が最速：

```sh
sdk install java 21.0.9-librca
sdk use java 21.0.9-librca
```

リポジトリールートに `.sdkmanrc` を同梱しているため、SDKMAN! の `auto_env` 設定を有効にしていれば `cd` 時に自動切替えされます。

## ステップ 1: クローン

```sh
git clone https://github.com/t-izuno/streamlit4j.git
cd streamlit4j
```

## ステップ 2: ビルド

```sh
./mvnw -DskipTests install
```

| オプション | 効果 |
| --- | --- |
| `-DskipTests` | 起動だけが目的なら単体テストを省略して時短（フル `mvn verify` は数分） |
| `install` | ローカル `~/.m2/repository` に各モジュール jar を配置し、`cli` モジュールから依存解決可能にする |

初回はフロントエンドビルド（React + Vite）と Maven 依存ダウンロードを含むため 2〜5 分。2 回目以降はインクリメンタルで数十秒。

## ステップ 3: デモ起動

```sh
# 8501 はリッスンポート（任意の空きポートに変更可）
java -jar cli/target/streamlit4j-cli-0.1.0-SNAPSHOT.jar 8501
```

`cli` モジュールは `maven-shade-plugin` で全依存を同梱した実行可能 jar（fat jar）として package されるため、追加の classpath 指定なしで起動できます。

起動すると以下が表示されます：

```text
streamlit4j listening on ws://localhost:8501/ws
```

## ステップ 4: ブラウザーで確認

<http://localhost:8501> を開くと、`examples/Hello.java` の内容（タイトル / マークダウン / スライダー / メトリック / ボタン）が表示されます。

動作確認ポイント：

- スライダーを動かすとメトリックがリアルタイム追従するか
- 「Greet」ボタンを押すとトースト通知が出るか
- ブラウザー DevTools の **Network** タブで `ws://localhost:8501/ws` を流れる JSON envelope を観察できるか

停止は起動シェルで `Ctrl+C`。

## 編集サイクル（任意）

スクリプトを書き換えてフロントへ自動リロード通知を送りたい場合は `--watch` を付けます：

```sh
java -jar cli/target/streamlit4j-cli-0.1.0-SNAPSHOT.jar 8501 --watch examples/src/main/java
```

`examples/src/main/java` 配下のファイル変更を検知すると、接続中の全クライアントに `source_change:<path>` 通知が飛び、フロントがリロードします（クラスの再コンパイルは別途必要）。

## トラブルシューティング

| 症状 | 原因 / 対処 |
| --- | --- |
| `enforcer requires JDK 21 LTS` で fail | JDK 25 など範囲外。`sdk use java 21.0.9-librca` で切替え |
| `Address already in use` | 別プロセスが 8501 を使用中。`-Dexec.args=8502` などに変更 |
| `no main manifest attribute` / `ClassNotFoundException` | ステップ 2 の `install` を未実行で fat jar 化前。`./mvnw -DskipTests install` を先に走らせる |
| 起動はするが画面が真っ白 | フロントエンドが組み込まれていない可能性。`./mvnw -pl frontend-assets clean install` で再ビルド |
| 編集→反映のたびに再起動が必要 | `--watch` が指定されていない、または対象ディレクトリーが違う |

## 次の一歩

- [Getting Started](./getting-started) — 独自スクリプトを書く / Spring Boot に組み込む
- [Spring Boot Integration](./spring-boot) — 既存 Web アプリへのマウント
- [Reference](../reference/overview) — 全 API 一覧
