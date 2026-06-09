# Custom Component Authoring Guide

How to add your own React parts to streamlit4j and call them from a Java script via `St.component(...)`. The only supported delivery model is in-process (iframe isolation is not adopted).

## Prerequisites

| Item | Value |
| --- | --- |
| Java | 21 LTS |
| Node | 22+ (when touching the frontend) |
| Trust boundary | First-party plus vendored third-party sources only. Dynamic loading of untrusted third-party code is out of scope |

## Big picture

Adding a custom component means registering the **same name in two places**: the Java side and the TypeScript side.

```text
   Java script                          React frontend
 ─────────────────────                ──────────────────────────────
 (1) CustomComponent declaration   (2) In components/StarRating.tsx,
   ↓                                    a React component that
 (2) St.registerComponent(spec)       receives CustomComponentRenderProps
   ↓                                    ↓
 (3) St.component(spec, args, def) (3) In component-builtins.ts,
                                        registerComponent('star-rating', X)
```

As long as the `name` string matches on both sides, `render.tsx` looks up the renderer from the registry and mounts it.

## Step 1: Declare on the Java side

```java
import io.streamlit4j.core.api.St;
import io.streamlit4j.core.domain.CustomComponent;
import java.util.Map;

public final class MyApp {

  // The result type R is any type Jackson can decode.
  private static final CustomComponent<Integer> RATING =
      St.registerComponent(new CustomComponent<>("star-rating", Integer.class));

  public static void run() {
    St.title("Survey");
    int rating = St.component(RATING, Map.of("label", "Rate this app", "max", 5), 0);
    St.write("Current rating: " + rating);
  }
}
```

Key points:

- `CustomComponent` is a record with a type parameter `R`. `name` should match `[a-z][a-z0-9-]*` (lowercase plus hyphens) by convention.
- `St.registerComponent` returns the spec as-is. Storing it in a `static final` field is the common pattern.
- Three variants exist: `St.component(spec, args)` returns a value (two-arg form), `St.component(spec, args, default)` is the three-arg form, and `St.component(name, args)` is display-only.

## Step 2: Write the React renderer

Add a new `.tsx` file under `frontend/src/components/` (for example, `StarRating.tsx`). Accept `CustomComponentRenderProps` as props and return values via `onChange(value)`.

```tsx
// frontend/src/components/StarRating.tsx
import { useMemo } from 'react';
import type { CustomComponentRenderProps } from '../component-registry';

const DEFAULT_MAX = 5;

function clampMax(raw: unknown): number {
  const m = typeof raw === 'number' && Number.isFinite(raw) ? Math.floor(raw) : DEFAULT_MAX;
  return Math.max(1, Math.min(10, m));
}

function clampValue(raw: unknown, max: number): number {
  const v = typeof raw === 'number' && Number.isFinite(raw) ? Math.floor(raw) : 0;
  return Math.max(0, Math.min(max, v));
}

export function StarRating({ args, value, onChange }: CustomComponentRenderProps) {
  const label = typeof args.label === 'string' ? args.label : 'Rating';
  const max = clampMax(args.max);
  const current = clampValue(value, max);
  const stars = useMemo(() => Array.from({ length: max }, (_, i) => i + 1), [max]);

  return (
    <fieldset className="component component--star-rating">
      <legend>{label}</legend>
      {stars.map((n) => (
        <button
          key={n}
          type="button"
          aria-pressed={n <= current}
          onClick={() => onChange(n === current ? 0 : n)}
        >
          {n <= current ? '★' : '☆'}
        </button>
      ))}
    </fieldset>
  );
}
```

Key points:

- `args` is the JSON-serialized result of the `Map` passed from Java. On the TypeScript side its type is `Record` of string key to unknown value, so a runtime guard is recommended.
- `value` is the current value held in session state (on the first render, the `defaultValue` from `St.component`).
- Calling `onChange(value)` sends a `widget_event` to the server, and on the next rerun `St.component` on the Java side returns the new value.

## Step 3: Register in the frontend registry

Call `registerComponent` in `frontend/src/component-builtins.ts`. Because `main.tsx` imports this module unconditionally at startup, adding an entry here enables the component across every app.

```ts
import { registerComponent } from './component-registry';
import { StarRating } from './components/StarRating';

registerComponent('star-rating', StarRating);
```

Add more lines to register more components. Always make sure the `name` string matches the Java-side `CustomComponent.name`.

## Step 4: Verify it works

```sh
# Java-side build
./mvnw -pl core,server,examples,cli -am verify

# Frontend tests
cd frontend && npm test
```

Start the server, open a page that calls `St.component(...)` in the browser, and the registered renderer mounts. Invoking an unregistered `name` falls back to the `.component--unregistered` placeholder (see design.md §9).

## Return value type conversion

`R` can be any type Jackson can decode. The server-side `ComponentCodec.coerce` applies the following precedence:

| Step | Action |
| --- | --- |
| 1 | The session state value is already an instance of `R` -- pass through |
| 2 | `JsonNode` -- `treeToValue` |
| 3 | Other (Map / List, etc.) -- `convertValue` |
| 4 | If all of the above fail -- return `defaultValue` |

Example: even when returning a record like `ColorRgb(int r, int g, int b)`, `new CustomComponent<>("color-picker", ColorRgb.class)` is enough. Sending `onChange({ r: 255, g: 128, b: 64 })` from the frontend lets the server decode it automatically.

## On vendoring the SDK

In the future, when components developed in external repositories need to be shared, we plan to publish an SDK under `frontend/src/sdk/` to npm. As of 0.1.0 we operate on a vendoring model: consumers should fork this repository's `frontend/` or pull it in as a submodule, and register their components in `component-builtins.ts`.

## Checklist

- [ ] Did you use the same `name` string in Java and in TypeScript?
- [ ] Do the `args` keys and types agree on both sides?
- [ ] Is the value sent via `onChange` decodable into the `CustomComponent` type parameter `R`?
- [ ] Did you remember to register in `frontend/src/component-builtins.ts`?
- [ ] Did you add at least one unit test for the React renderer that runs under `npm test`?
