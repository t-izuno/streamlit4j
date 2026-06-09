# ソースからクローンして動かす

リポジトリーをクローンして同梱の examples を実起動するまでの最短手順。
所要時間 5〜10 分。

> **このコースの想定**: 評価・動作確認・コントリビュート目的で
> ローカル動作させたい方向け。

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
| `install` | ローカル `~/.m2/repository` に各モジュール jar を配置し、examples モジュールから依存解決可能にする |

初回はフロントエンドビルド（React + Vite）と Maven 依存ダウンロードを含むため 2〜5 分。2 回目以降はインクリメンタルで数十秒。

## ステップ 3: examples を起動

examples には A 候補（Library / 自前 `main`）と B 候補（Spring Boot Starter）の両方の起動例が同梱されています。どちらの形式でも同じ画面が確認できます。

### A 候補（Library / 自前 `main`）で起動

```sh
# 8501 はリッスンポート（任意の空きポートに変更可）
./mvnw -pl examples/embedded -q exec:java \
    -Dexec.mainClass=io.streamlit4j.examples.Hello \
    -Dexec.args=8501
```

他の同梱デモも同じパターンで起動できます：

| メインクラス | 主な確認要素 |
| --- | --- |
| `io.streamlit4j.examples.Hello` | title / markdown / slider / metric / button |
| `io.streamlit4j.examples.WidgetsDemo` | text / number / select / radio / checkbox / date / time / colorPicker |
| `io.streamlit4j.examples.LayoutDemo` | columns / container / expander / tabs / sidebar / form |
| `io.streamlit4j.examples.DataDemo` | dataframe / line / bar / area / scatter / metric / cache |
| `io.streamlit4j.examples.ComponentDemo` | カスタムコンポーネント（star-rating） |
| `io.streamlit4j.examples.ShowcaseDemo` | 上記全カテゴリーを 1 画面で網羅したショーケース |

起動すると以下が表示されます：

```text
streamlit4j listening on ws://localhost:8501/ws
```

### B 候補（Spring Boot Starter）で起動

各デモには対応する `SpringBoot<Name>App` クラスが同梱されています。

```sh
./mvnw -pl examples/spring-boot -q exec:java \
    -Dexec.mainClass=io.streamlit4j.examples.spring.hello.SpringBootHelloApp
```

各デモはサブパッケージに分かれて配置されています
（`io.streamlit4j.examples.spring.{hello,widgets,layout,data,component,showcase}`）。
クラス名と組み合わせて他のデモを起動してください。デフォルトでは
`${streamlit4j.base-path}`（既定 `/streamlit4j`）配下にマウントされるため、
`http://localhost:8080/streamlit4j` を開きます。

## ステップ 4: ブラウザーで確認

A 候補の場合は <http://localhost:8501> 、B 候補の場合は <http://localhost:8080/streamlit4j> を開きます。

動作確認ポイント（`Hello` の場合）：

- スライダーを動かすとメトリックがリアルタイム追従するか
- 「Greet」ボタンを押すとトースト通知が出るか
- ブラウザー DevTools の **Network** タブで `ws://...` を流れる JSON envelope を観察できるか

停止は起動シェルで `Ctrl+C`。

## トラブルシューティング

| 症状 | 原因 / 対処 |
| --- | --- |
| `enforcer requires JDK 21 LTS` で fail | JDK 25 など範囲外。`sdk use java 21.0.9-librca` で切替え |
| `Address already in use` | 別プロセスが 8501 / 8080 を使用中。`-Dexec.args=8502` などに変更 |
| `ClassNotFoundException: io.streamlit4j...` | ステップ 2 の `install` を未実行で依存が未配置。`./mvnw -DskipTests install` を先に走らせる |
| 起動はするが画面が真っ白 | フロントエンドが組み込まれていない可能性。`./mvnw -pl frontend-assets clean install` で再ビルド |

## 次の一歩

- [Getting Started](./getting-started) — 独自スクリプトを書く / Spring Boot に組み込む
- [Spring Boot Integration](./spring-boot) — 既存 Web アプリへのマウント
- [Reference](../reference/overview) — 全 API 一覧
