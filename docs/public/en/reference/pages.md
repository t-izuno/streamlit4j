# Multi-page

An explicit registration page navigator. Auto-registration via the `pages/` directory convention is not supported.

| Java API | Protocol `kind` | props | Return value |
| --- | --- | --- | --- |
| `St.pages(List<Page> pages)` | `pages` | `pages` (`[{name, path}, ...]`), `current` | none |

`Page` is the following record:

```java
public record Page(String name, String path, Runnable body) {}
```

## Behavior model

1. `St.pages(...)` reads the session state `"__page__"` to determine the current path
   - If unset, the first entry in the list is the default
2. Emit a `pages` node (the front end renders `<nav class="pages">`)
3. Call `body.run()` of the matching page and stack the subsequent emits as its body

Page switching on the front end is achieved by sending `widget_event(widgetId="__page__", value=<path>)`.

## Notes

- The page order follows the `List` order as-is
- pushState-based clean URLs are not supported (the front end syncs `__page__` via `window.location.hash`)
- An app that does not call `pages` is a single-page app. It works simply by calling widgets directly at the top level
