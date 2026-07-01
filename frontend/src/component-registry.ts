import type { ComponentType, ReactNode } from 'react';

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
  children?: ReactNode;
}

export type CustomComponentRenderer = ComponentType<CustomComponentRenderProps>;
export type ChatComponentSlot = 'container' | 'message' | 'stream' | 'input' | 'controls';

const registry = new Map<string, CustomComponentRenderer>();
const chatRegistry = new Map<ChatComponentSlot, CustomComponentRenderer>();

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

/** Registers a renderer that replaces one standard chat UI slot. */
export function registerChatComponent(
  slot: ChatComponentSlot,
  renderer: CustomComponentRenderer,
): void {
  chatRegistry.set(slot, renderer);
}

/** Returns the renderer registered for a chat UI slot, or undefined if absent. */
export function findChatComponent(slot: ChatComponentSlot): CustomComponentRenderer | undefined {
  return chatRegistry.get(slot);
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
  chatRegistry.clear();
}
