# Layout

Container primitives that arrange child elements in a nested structure. Emits emitted inside a `Runnable` / `IntConsumer` become the children of that container.

| Java API | Protocol `kind` | props | Child rendering |
| --- | --- | --- | --- |
| `St.columns(int count, IntConsumer body)` | `columns` | `count` | Has `count` `column` nodes as children. Each column holds the emits from `body.accept(index)` |
| `St.container(Runnable body)` | `container` | none | Emits from `body` |
| `St.expander(String label, Runnable body)` | `expander` | `label` | Emits from `body` placed inside `<details><summary>` |
| `St.tabs(List<String> labels, IntConsumer body)` | `tabs` | `labels` | Each `tab` node corresponding to a label holds the emits from `body.accept(index)` |
| `St.sidebar(Runnable body)` | `sidebar` | none | Mounted as a fixed left-side `<aside>` on the frontend |
| `St.empty()` | `empty` | none | `<div class="empty">` placeholder |

## How RenderContext works

- `pushFrame()` pushes an empty child frame onto the stack; emits made while `body` runs accumulate in that frame
- `popFrame()` retrieves the child elements and attaches them as the parent node's `children` via `addNode`
- `columns` / `tabs` loop through `pushFrame` -> `body.accept(i)` -> `popFrame`

## Notes

- `sidebar` is rendered on the frontend as a single unique region. If called multiple times, only the last one may end up visible
- The open/closed state of `expander` is local UI state on the client side (it is not saved to session state)
- `empty` is intended as a placeholder for future differential updates that "swap out this spot later on"
