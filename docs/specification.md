# streamlit4j 仕様書

> ステータス: ドラフト v0.1
>
> 関連ドキュメント: 要件は `requirements.md`、設計は `design.md`、実行計画は `tasks/task.md`

本書は streamlit4j の外部仕様（API サーフェス・プロトコル・要素カタログ）を定義する。
内部実装方式（ID 生成アルゴリズム・差分計算・スレッドモデル等）は `design.md` を参照する。

## 1. API ファサード仕様

### 1.1 St ファサード

- `St` ファサードに Streamlit 互換の命名でメソッドを並べる
- 入力系メソッドは現在値を直接返す
- 表示系メソッドは要素ハンドルを返す
- よく使う組み合わせは直接戻り値オーバーロードを用意し、詳細指定はビルダーで補う
- 終端メソッド名（`show()` / `render()` / `use()` 等）は未決定（`design.md` 10 章参照）

### 1.2 サンプルコード

```java
import io.streamlit4j.St;

public class App {
    public static void main(String[] args) {
        St.title("売上ダッシュボード");

        // 直接戻り値オーバーロード（簡潔パス）
        int year = St.slider("対象年", 2018, 2026, 2025);

        var df = SalesRepository.byYear(year);
        St.metric("総売上", df.total());
        St.lineChart(df.monthly());

        // ビルダー（詳細指定パス）
        String region = St.selectbox("地域")
                          .options("東日本", "西日本", "全国")
                          .key("region")
                          .show();

        if (St.button("CSV を再集計")) {
            St.toast("再集計しました");
        }
    }
}
```

### 1.3 エントリポイント定義形式

ユーザーは以下のいずれかでエントリポイントを 1 つ定義する。

- `void main()` 相当のメソッド
- `St.app(...)` に渡すラムダ

### 1.4 制御 API

- `St.rerun()`: 明示的な再実行をトリガーする
- `St.stop()`: 現在の再実行を中断する

### 1.5 ウィジェット同一性

- 明示キー優先 + 呼び出し位置由来の決定的 ID で同一性を確定する
- `key` パラメーターは任意指定可能

## 2. WebSocket プロトコル仕様

### 2.1 エンベロープ

- WebSocket による双方向通信
- v1 は JSON エンベロープを採用（バイナリ最適化は将来差し替え可能）
- バージョン付きエンベロープでメッセージ種別を明示する

### 2.2 メッセージ例（render_delta）

```json
{
  "v": 1,
  "type": "render_delta",
  "sessionId": "s-8f3a",
  "seq": 42,
  "patches": [
    {
      "op": "replace",
      "path": "main/0",
      "node": {
        "kind": "slider",
        "id": "w_year",
        "props": { "label": "対象年", "min": 2018, "max": 2026, "value": 2025 }
      }
    }
  ]
}
```

### 2.3 メッセージ種別

- `session_init`: セッション確立・初期レンダー
- `render_delta`: サーバー → クライアント の差分
- `widget_event`: クライアント → サーバー の値変更・ボタン押下
- `rerun_request`: 明示再実行
- `file_upload` / `download_ready`: ファイル入出力
- `error`: 実行エラーの通知

## 3. レンダーツリーノード仕様

- ノードは `kind` / `id` / `props` / `children` を持つ不変表現とする
- `kind` は要素種別の識別子
- `id` はウィジェット同一性を表す決定的 ID
- `props` は要素固有のパラメーター集合
- `children` は子ノードリスト

## 4. 要素カタログ仕様

### 4.1 要素定義の 3 点セット

各要素は以下の 3 点セットで定義する。

- Java 側ビルダー API
- プロトコル上のノード表現（`kind` と `props`）
- フロント描画コンポーネント

### 4.2 入力系ウィジェット

- `slider` / `text_input` / `number_input` / `selectbox` / `multiselect`
- `checkbox` / `radio` / `button` / `date_input` / `time_input`
- `color_picker` / `select_slider` / `text_area` / `data_editor`
- `file_uploader`

### 4.3 表示系要素

- `write` / `markdown` / `title` / `header` / `subheader` / `caption`
- `metric` / `dataframe` / `table` / `json` / `code` / `latex` / `html`
- `image` / `audio` / `video` / `divider`
- `toast` / `progress` / `spinner` / `status`
- チャート: `line_chart` / `bar_chart` / `area_chart` / `scatter_chart`

### 4.4 レイアウト系

- `columns` / `container` / `expander` / `tabs` / `sidebar`
- `empty` 相当のプレースホルダー

## 5. セッション状態 API 仕様

- `session_state` 相当の Key-Value ストアを提供する
- 型付きアクセサーで `String` キーと型を指定して安全に取得・設定する
- セッションのライフサイクルリスナー（生成・タイムアウト・破棄）を登録できる

## 6. マルチページ仕様

ページ宣言方法を 2 系統サポートする。既定方式は未決定（`design.md` 10 章参照）。

- 規約ベース: 特定パッケージ・ディレクトリのクラスを自動探索する
- 明示登録ベース: `St.pages(...)` にページ定義を渡す

ナビゲーション機構として、ページ間ナビゲーション UI と URL ディープリンクを提供する。

## 7. フォーム仕様

- `form` コンテナー内のウィジェットは送信ボタン押下まで再実行を抑制する
- 送信時にまとめて値を確定し、1 回の再実行で反映する

## 8. キャッシュ仕様

### 8.1 種別

- データキャッシュ: 戻り値の再利用
- リソースキャッシュ: コネクション等の共有

### 8.2 指定方式

アノテーションまたはラッパー関数の両対応を検討する。
既定方式は未決定（`design.md` 10 章参照）。

### 8.3 無効化

- 無効化キー（引数ハッシュ）を指定可能
- TTL を指定可能

## 9. カスタムコンポーネント仕様

### 9.1 種別

- 第一者向け「インプロセス component」: 同梱バンドルに React 部品を登録
- 第三者向け「iframe 隔離 component」: sandbox 属性で隔離

### 9.2 Java 側 API

- コンポーネントの引数・戻り値を型で宣言する

### 9.3 フロント TS SDK

- 値の受け渡しのブリッジを提供する
- 再描画通知のブリッジを提供する

### 9.4 CLI

- `streamlit4j component create` 相当の雛形生成コマンドを提供する

## 10. テーマ仕様

- ライト / ダーク / カスタムカラーの 3 種を設定で切り替え可能にする
- カラーパレットは独自視覚アイデンティティーに基づく（Streamlit 公式パレット流用禁止）

## 11. ファイル入出力仕様

### 11.1 アップロード

- サイズ上限を設定可能
- MIME 制限を設定可能
- ストリーミング受信に対応

### 11.2 ダウンロード

- ダウンロードボタンを提供する
- 生成物（CSV / 画像 / PDF 等）の配信に対応する

## 12. 開発体験仕様

- ソース変更検知時、ブラウザーを自動リロードする
- エラーは画面上にスタックトレース付きで表示する
- CLI 起動コマンド: `streamlit4j run App.java`

## 13. 実行形態仕様

### 13.1 スタンドアロン CLI

- 単一 `.java` ファイルを JBang 経由で起動できる

### 13.2 埋め込み

- 既存 Spring Boot アプリにマウントし、指定パスで公開できる

### 13.3 共通

- いずれの形態でも同一の `App` 定義が動く
