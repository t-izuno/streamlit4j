# Custom components

API for adding your own first-party in-process React components. The iframe isolation approach is not adopted.

## Declaration and registration

```java
public record CustomComponent<R>(String name, Class<R> resultType) {
    public static CustomComponent<Void> ofVoid(String name);
}
```

| Java API | Return value | Description |
| --- | --- | --- |
| `St.registerComponent(CustomComponent<R> spec)` | `CustomComponent<R>` | Registers the component in the server-side registry (`ComponentRegistry`). Returns the spec as-is to support fluent declaration |

## Invocation

| Java API | Protocol `kind` | props | Return value |
| --- | --- | --- | --- |
| `St.component(CustomComponent<R> spec, Map<String, Object> args)` | `component` | `name`, `args`, `value` (optional) | `R` (`null` on the first call) |
| `St.component(CustomComponent<R> spec, Map<String, Object> args, R defaultValue)` | `component` | same as above | `R` (`defaultValue` on the first call) |
| `St.component(String name, Map<String, Object> args)` | `component` | `name`, `args` | none (display only) |

## Front-end registration

Register a React renderer with `registerComponent(name, renderer)` in `frontend/src/component-registry.ts`, then import it from `frontend/src/component-builtins.ts` so it is activated when `main.tsx` starts.

A renderer is a React component that receives `CustomComponentRenderProps` (`args` / `value` / `onChange`) as its props.

## Value type conversion

`R` can be any type that Jackson can decode. `ComponentCodec.coerce(stored, resultType, fallback)` performs the conversion in the following order of priority:

1. `stored` is already an instance of `resultType` -> use as-is
2. `stored` is a `JsonNode` -> `treeToValue`
3. Otherwise -> `convertValue` (Map -> record, etc.)
4. All failed -> `fallback`

## Sample

As a built-in example, `star-rating` (5-star rating) is bundled at `frontend/src/components/StarRating.tsx`.
