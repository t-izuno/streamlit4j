# OWASP Dependency-Check 手動実行手順

このページは streamlit4j の依存ライブラリーに対して OWASP Dependency-Check を
手動で実行する手順です。メンテナー向けのリファレンス。

> **対象**: メンテナーのみ。通常の利用者・コントリビューターは読む必要はありません。
> PR の自動脆弱性検査は OSV-Scanner（`ci.yml` 内）で実施しており、本ページの
> OWASP スキャンは **より深い CVSS スコア評価・SARIF 出力を目的とした補完的な検査** です。

## 実施頻度

| タイミング | 想定 |
| --- | --- |
| 月 1 回（推奨） | 通常運用 |
| メジャー依存更新時 | Spring Boot / Jetty / Jackson のメジャーアップ後 |
| リリース前 | 0.x → 1.0 などの公開前最終確認 |

定期スケジュール実行（cron）は採用していません。NVD API キーが必須かつ
オフピーク帯で実行したいため、起動タイミングは人が選びます。

## 前提

| 項目 | 必要なもの |
| --- | --- |
| NVD API キー | <https://nvd.nist.gov/developers/request-an-api-key> から無料発行 |
| JDK | 21 LTS |
| Maven | 3.9+ |
| ネットワーク | NVD（nvd.nist.gov）への HTTPS アウトバウンド |
| 推定所要時間 | 初回 10〜20 分 / 2 回目以降（キャッシュあり） 1〜3 分 |

### NVD API キーがない場合の挙動

非認証だと NIST 側のレート制限が 30 秒に 5 リクエストと厳しく、
初回ダウンロードに **30〜60 分** を要します。手動運用では必ず API キーを取得してください。

## 実行方法 A: ローカル CLI（推奨）

### A-1. API キーの保存

```sh
# シェル起動時に読み込まれる場所（~/.zshrc 等）に追記
export NVD_API_KEY="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
```

または `~/.m2/settings.xml` の `<profile>` に格納：

```xml
<profiles>
  <profile>
    <id>owasp</id>
    <properties>
      <nvdApiKey>xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx</nvdApiKey>
    </properties>
  </profile>
</profiles>
```

> `NVD_API_KEY` は機微情報です。**dotfiles リポジトリーや shell history に
> 平文で残さないよう** 注意してください。

### A-2. スキャン実行

```sh
# ルートディレクトリで
mvn -B -ntp -P security-scan -DnvdApiKey="$NVD_API_KEY" -DskipTests verify
```

主要オプション：

| オプション | 効果 |
| --- | --- |
| `-P security-scan` | `pom.xml` の `security-scan` プロファイル（OWASP プラグイン）を有効化 |
| `-DnvdApiKey=...` | NVD API キーを渡す |
| `-DskipTests` | 単体テストをスキップ（脆弱性検査が目的のため） |
| `-DfailBuildOnCVSS=7` | CVSS 7.0 以上で fail（既定値）。一時的に緩めるなら `-DfailBuildOnCVSS=10` |

### A-3. レポート確認

`target/` 配下に以下が生成されます：

```text
target/dependency-check-report.html    # 人間が読む用
target/dependency-check-report.sarif   # GitHub Code Scanning 等への取り込み用
```

HTML を開くか、CVE が出ていれば次節「検出時の対応フロー」へ。

## 実行方法 B: GitHub Actions 手動起動

NVD API キーは `NVD_API_KEY` という名前で repository secret に登録されている前提。

### B-1. 起動

1. GitHub リポジトリーの **Actions** タブを開く
2. 左サイドバーから **Security scan (manual)** を選択
3. 右上の **Run workflow** ボタンを押す
4. ブランチを選んで **Run workflow** を実行

### B-2. レポート取得

Job 完了後：

| 取得先 | 内容 |
| --- | --- |
| Run の **Artifacts** セクション | `dependency-check-report`（HTML + SARIF zip） |
| リポジトリーの **Security → Code scanning** | SARIF が自動取り込みされ、Finding として表示 |

## 検出時の対応フロー

```text
CVE 発見
    │
    ├─ 真陽性（実コードに影響あり）
    │     ↓
    │     依存バージョンを上げて再スキャン
    │     ↓
    │     PR / コミットしてマージ
    │
    └─ 偽陽性（誤検知・影響なしと判断）
          ↓
          owasp-suppressions.xml に抑止ルールを追記
          ↓
          コメントで「なぜ抑止して良いか」を明記
```

### 抑止ルールの書き方

`owasp-suppressions.xml`（リポジトリーのルート）に追記：

```xml
<suppress>
  <notes><![CDATA[
    CVE-YYYY-NNNNN: <ライブラリー名> の脆弱性は <理由> により本プロジェクトでは
    影響を受けない。詳細: <Issue URL or 内部判断メモ>
    judged-by: @<判断者> on YYYY-MM-DD
  ]]></notes>
  <gav regex="true">^io\.example:.*$</gav>
  <cve>CVE-YYYY-NNNNN</cve>
</suppress>
```

判断根拠を `notes` に必ず残してください（他のメンテナーが将来見返したときに、
再評価の手間を増やさないため）。

## トラブルシューティング

| 症状 | 原因 / 対処 |
| --- | --- |
| 初回ダウンロードが終わらない | NVD API キーが渡っていない可能性。`mvn` 引数の `-DnvdApiKey` を再確認 |
| `401 Unauthorized` from NVD | API キーが失効または誤り。NVD ダッシュボードで再発行 |
| `Could not connect to NVD` | プロキシ環境では `-Dhttps.proxyHost=...` 等の追加が必要 |
| `target/dependency-check-data` が肥大 | データキャッシュ。削除すれば次回フル再ダウンロード（10〜20 分） |
| 既知の偽陽性が毎回出る | `owasp-suppressions.xml` に抑止ルール追加（上記参照） |
| `failBuildOnCVSS` で fail するが PR を進めたい | 一時的に `-DfailBuildOnCVSS=10` で回避し、別チケットで本対応 |

## 関連ファイル

- `.github/workflows/security-scan.yml` — 手動起動ワークフロー定義
- `.github/workflows/ci.yml` — PR ごとに走る OSV-Scanner（軽量検査）
- `pom.xml` の `<profile id="security-scan">` — OWASP プラグイン設定
- `owasp-suppressions.xml` — 偽陽性抑止ルール
