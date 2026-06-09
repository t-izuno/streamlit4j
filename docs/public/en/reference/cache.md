# Cache

Store the result of an expensive computation under `key` and avoid re-execution on subsequent reruns. Not present in the protocol or front-end rendering (server-side only).

| Java API | Return value | Description |
| --- | --- | --- |
| `St.cacheData(String key, Duration ttl, Supplier<T> loader)` | `T` | With TTL. `loader` is re-executed on the first call after `ttl` elapses |
| `St.cacheResource(String key, Supplier<T> loader)` | `T` | Effectively permanent (TTL = 365 days). For connections, models, etc. that should be loaded only once within the process |

## Implementation

- Accessed via the `CacheStore` port obtained from `CacheAccess.dataCache()` / `CacheAccess.resourceCache()`
- The default implementation is `InMemoryCacheStore` (`ConcurrentMap<String, Entry>` plus Instant-based expiry)
- Can be swapped to another store via a Spring Boot starter or a custom implementation

## Notes

- Annotation-based approaches (`@Cache`, etc.) are not adopted, in order to avoid dependencies on AOP / IoC
- The caller is responsible for naming `key` so that it does not collide (we recommend including the package name and parameters in the key)
- If `loader` throws an exception, nothing is recorded in the cache and the next call retries
