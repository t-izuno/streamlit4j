/**
 * Third-party iframe component SDK for streamlit4j. This module ships with the
 * frontend bundle and is exposed for component authors to import from their
 * iframe-hosted code; it MUST stay self-contained (no React or other deps) so
 * it can be vendored into arbitrary build setups.
 *
 * <p>Wire protocol mirrors the host {@code IframeComponent} (see TASK-101 /
 * TASK-102):
 *
 * <ul>
 *   <li>guest → host {@code streamlit4j:ready}  ── signal load complete</li>
 *   <li>host  → guest {@code streamlit4j:state} ── deliver args + last value</li>
 *   <li>guest → host {@code streamlit4j:widget_event} ── emit new value</li>
 * </ul>
 *
 * <p>The SDK filters incoming messages by {@code event.source === window.parent}
 * and by component {@code name} so a malicious sibling iframe cannot inject
 * state. Boundary checks on payload shape are enforced by the host
 * ({@link validateIframePayload}) before {@code onChange} is invoked.
 */

export interface ComponentState<TArgs = Record<string, unknown>, TValue = unknown> {
  args: TArgs;
  value: TValue;
}

export type StateHandler<TArgs, TValue> = (state: ComponentState<TArgs, TValue>) => void;

const READY_TYPE = 'streamlit4j:ready';
const STATE_TYPE = 'streamlit4j:state';
const WIDGET_EVENT_TYPE = 'streamlit4j:widget_event';

export class Streamlit4jComponent<TArgs = Record<string, unknown>, TValue = unknown> {
  private readonly stateHandlers: Array<StateHandler<TArgs, TValue>> = [];
  private latestState: ComponentState<TArgs, TValue> | null = null;
  private messageListener: ((event: MessageEvent) => void) | null = null;

  constructor(public readonly name: string) {
    if (!name) {
      throw new Error('component name must not be empty');
    }
    this.attachListener();
  }

  /** Signals to the host that the iframe is ready to receive state. */
  ready(): void {
    if (typeof window === 'undefined' || !window.parent || window.parent === window) {
      return;
    }
    window.parent.postMessage({ type: READY_TYPE, name: this.name }, '*');
  }

  /** Subscribes to state updates from the host. Returns an unsubscribe handle. */
  onState(handler: StateHandler<TArgs, TValue>): () => void {
    this.stateHandlers.push(handler);
    if (this.latestState) {
      handler(this.latestState);
    }
    return () => {
      const idx = this.stateHandlers.indexOf(handler);
      if (idx >= 0) {
        this.stateHandlers.splice(idx, 1);
      }
    };
  }

  /** Returns the most recently delivered state, or null before the first message. */
  currentState(): ComponentState<TArgs, TValue> | null {
    return this.latestState;
  }

  /** Sends a new widget value back to the host. */
  setValue(value: TValue): void {
    if (typeof window === 'undefined' || !window.parent || window.parent === window) {
      return;
    }
    window.parent.postMessage({ type: WIDGET_EVENT_TYPE, name: this.name, value }, '*');
  }

  /** Detaches the message listener. Call from teardown / hot reload. */
  dispose(): void {
    if (this.messageListener) {
      window.removeEventListener('message', this.messageListener);
      this.messageListener = null;
    }
    this.stateHandlers.length = 0;
  }

  private attachListener(): void {
    if (typeof window === 'undefined') {
      return;
    }
    const listener = (event: MessageEvent) => {
      if (event.source !== window.parent || event.source === window) {
        return;
      }
      const data = event.data;
      if (!data || typeof data !== 'object') {
        return;
      }
      const record = data as Record<string, unknown>;
      if (record.type !== STATE_TYPE || record.name !== this.name) {
        return;
      }
      const state: ComponentState<TArgs, TValue> = {
        args: (record.args as TArgs) ?? ({} as TArgs),
        value: record.value as TValue,
      };
      this.latestState = state;
      this.stateHandlers.forEach((handler) => handler(state));
    };
    window.addEventListener('message', listener);
    this.messageListener = listener;
  }
}
