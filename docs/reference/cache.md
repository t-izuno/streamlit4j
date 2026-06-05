# キャッシュ

重い処理の結果を `key` で保持し、再 rerun 時に再実行を回避する。プロトコル / フロント描画には現れない（サーバー側のみ）。

| Java API | 戻り値 | 説明 |
| --- | --- | --- |
| `St.cacheData(String key, Duration ttl, Supplier<T> loader)` | `T` | TTL 付き。`ttl` 経過後の最初の呼び出しで `loader` 再実行 |
| `St.cacheResource(String key, Supplier<T> loader)` | `T` | 実質的に永続（TTL = 365 日）。プロセス内で 1 回だけロードしたい接続 / モデル等向け |

## 実装

- `CacheAccess.dataCache()` / `CacheAccess.resourceCache()` から取得した `CacheStore` ポート経由
- 既定実装は `InMemoryCacheStore`（`ConcurrentMap<String, Entry>` + Instant ベースの expiry）
- Spring Boot starter / カスタム実装で別ストアに差し替え可能

## 注意点

- アノテーション方式（`@Cache` 等）は採用しない（[design.md §10-2](../design#10-2-api-設計指針実装レベル) 参照）。AOP / IoC への依存を避ける目的
- `key` は呼び出し側の責任で衝突しないよう命名する（パッケージ名やパラメーターをキーに含める運用を推奨）
- `loader` が例外を投げた場合、キャッシュには記録されず次回呼び出しで再試行される
