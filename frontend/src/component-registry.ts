import type { ComponentType } from 'react';

/**
 * Props passed to every in-process custom component renderer. The bundled SPA
 * looks up a renderer by name in {@link findComponent} and invokes it with the
 * args declared from {@code St.component(...)} on the Java side, the latest
 * stored value, and an {@code onChange} callback that emits a widget event.
 */
export interface CustomComponentRenderProps {
  args: Record<string, unknown>;
  value: unknown;
  onChange: (value: unknown) => void;
}

export type CustomComponentRenderer = ComponentType<CustomComponentRenderProps>;

const registry = new Map<string, CustomComponentRenderer>();

/**
 * Registers an in-process custom component renderer under {@code name}.
 * Re-registering an existing name overwrites the previous entry — the last
 * registration wins, matching the Java-side {@code ComponentRegistry} semantics.
 */
export function registerComponent(name: string, renderer: CustomComponentRenderer): void {
  if (!name) {
    throw new Error('component name must not be empty');
  }
  registry.set(name, renderer);
}

/** Returns the renderer registered under {@code name}, or undefined if absent. */
export function findComponent(name: string): CustomComponentRenderer | undefined {
  return registry.get(name);
}

/** Returns a snapshot of all registered component names. */
export function registeredNames(): string[] {
  return Array.from(registry.keys());
}

/**
 * Removes all registered renderers. Intended for test isolation; production
 * code registers renderers once at module load and never clears.
 */
export function clearComponents(): void {
  registry.clear();
}
