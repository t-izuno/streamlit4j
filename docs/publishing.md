# Publishing to Maven Central

このページは streamlit4j の jar を Maven Central（Sonatype Central Portal）に
公開する際の手順です。リリース担当者向けのリファレンス。

> **対象**: 0.1.0 以降のリリース担当者のみ。通常の利用者は読む必要はありません。

## 前提

| 項目 | 必要なもの |
| --- | --- |
| Sonatype Central Portal アカウント | <https://central.sonatype.com/> で発行 |
| ネームスペース | `io.streamlit4j` の所有確認（DNS TXT または GitHub 認証） |
| GPG キー | RSA 4096-bit 推奨、公開鍵を keyservers.openpgp.org に登録 |
| Maven | 3.9+ |
| JDK | 21 LTS |

## 1. GPG キーの準備

```sh
gpg --full-generate-key
# RSA, 4096, 有効期限を設定して鍵を作成

gpg --list-secret-keys --keyid-format=long
# → KEYID をメモ

gpg --armor --export $KEYID | gpg --keyserver hkps://keys.openpgp.org --send-keys $KEYID
```

## 2. `~/.m2/settings.xml`

ローカル環境（または CI のシークレットで注入）に以下を配置：

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>${env.CENTRAL_USERNAME}</username>
      <password>${env.CENTRAL_PASSWORD}</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>gpg</id>
      <properties>
        <gpg.keyname>${env.GPG_KEY_ID}</gpg.keyname>
        <gpg.passphrase>${env.GPG_PASSPHRASE}</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>gpg</activeProfile>
  </activeProfiles>
</settings>
```

- `central` の username / password は Central Portal で発行する
  **User Token**（ログイン名/パスワードではない）
- GPG_PASSPHRASE は **誤って echo しないよう注意**

## 3. リリースビルド

```sh
# バージョンを 0.1.0 に切り替え (タグ付け後)
mvn versions:set -DnewVersion=0.1.0 -DgenerateBackupPoms=false

# ローカル検証 (publish しない)
mvn -P release -DskipTests clean verify

# 各モジュールの target/ に以下が揃うことを確認:
#   *-<version>.jar
#   *-<version>-sources.jar
#   *-<version>-javadoc.jar
#   *-<version>.jar.asc        (GPG 署名)
#   *-<version>.pom.asc
#   *-<version>-sources.jar.asc
#   *-<version>-javadoc.jar.asc

# 公開 (autoPublish=false なので Portal で手動承認)
mvn -P release -DskipTests deploy
```

`central-publishing-maven-plugin` は `deploy` フェーズで Central Portal に
バンドルをアップロードします。`waitUntil=published` 設定により、Portal 側で
公開承認するまで Maven プロセスがブロック待機します。

## 4. 動作確認

公開承認後、最大数時間以内に <https://repo1.maven.org/maven2/io/streamlit4j/>
配下から取得可能になります。空のプロジェクトで以下を試行：

```xml
<dependency>
  <groupId>io.streamlit4j</groupId>
  <artifactId>streamlit4j-core</artifactId>
  <version>0.1.0</version>
</dependency>
```

## トラブルシューティング

| 症状 | 原因 / 対処 |
| --- | --- |
| `gpg: signing failed: Inappropriate ioctl for device` | `--pinentry-mode loopback` を pom 側で指定済み。CI 環境では `GPG_TTY` 設定や `gpg-agent` の事前起動が必要 |
| `401 Unauthorized` | `~/.m2/settings.xml` の `<server id="central">` が User Token になっているか確認 |
| バンドルが拒否される | source jar / javadoc jar / 署名 / required POM フィールドのいずれか欠落。`mvn -P release verify` で各 module の `target/` を必ず確認 |
| `Could not resolve dependencies` after publish | Portal 側で承認まだ。1〜4 時間程度 待つ |

## 関連タスク

- TASK-110 / TASK-122 / TASK-123 — 本ページが扱う POM・署名整備
- TASK-128 — 0.1.0 リリースタグ / リリースノート
- TASK-129 — 実際の公開実行
