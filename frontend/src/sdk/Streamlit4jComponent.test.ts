import { afterEach, describe, expect, it, vi } from 'vitest';
import { Streamlit4jComponent } from './Streamlit4jComponent';

interface ParentPostMessageCall {
  data: Record<string, unknown>;
  targetOrigin: string;
}

function setupParentSpy(): ParentPostMessageCall[] {
  const calls: ParentPostMessageCall[] = [];
  const parentStub = {
    postMessage: (data: unknown, targetOrigin: string) => {
      calls.push({ data: data as Record<string, unknown>, targetOrigin });
    },
  };
  Object.defineProperty(window, 'parent', { value: parentStub, configurable: true });
  return calls;
}

function teardownParentSpy(): void {
  Object.defineProperty(window, 'parent', { value: window, configurable: true });
}

describe('Streamlit4jComponent SDK', () => {
  afterEach(() => {
    teardownParentSpy();
    vi.restoreAllMocks();
  });

  it('rejects empty name', () => {
    expect(() => new Streamlit4jComponent('')).toThrow();
  });

  it('ready() posts a streamlit4j:ready envelope to the parent', () => {
    const sent = setupParentSpy();
    const sdk = new Streamlit4jComponent('my-widget');
    sdk.ready();
    expect(sent).toEqual([
      { data: { type: 'streamlit4j:ready', name: 'my-widget' }, targetOrigin: '*' },
    ]);
    sdk.dispose();
  });

  it('setValue posts a streamlit4j:widget_event envelope', () => {
    const sent = setupParentSpy();
    const sdk = new Streamlit4jComponent<{ tone: string }, number>('counter');
    sdk.setValue(42);
    expect(sent).toEqual([
      { data: { type: 'streamlit4j:widget_event', name: 'counter', value: 42 }, targetOrigin: '*' },
    ]);
    sdk.dispose();
  });

  it('onState handlers receive matching state messages from the parent', () => {
    setupParentSpy();
    const sdk = new Streamlit4jComponent<{ tone: string }, string>('color-badge');
    const handler = vi.fn();
    sdk.onState(handler);
    window.dispatchEvent(
      new MessageEvent('message', {
        source: window.parent,
        data: {
          type: 'streamlit4j:state',
          name: 'color-badge',
          args: { tone: 'warm' },
          value: 'initial',
        },
      }),
    );
    expect(handler).toHaveBeenCalledWith({ args: { tone: 'warm' }, value: 'initial' });
    expect(sdk.currentState()).toEqual({ args: { tone: 'warm' }, value: 'initial' });
    sdk.dispose();
  });

  it('onState late subscribers receive the most recent cached state', () => {
    setupParentSpy();
    const sdk = new Streamlit4jComponent<Record<string, unknown>, number>('counter');
    window.dispatchEvent(
      new MessageEvent('message', {
        source: window.parent,
        data: { type: 'streamlit4j:state', name: 'counter', args: {}, value: 7 },
      }),
    );
    const handler = vi.fn();
    sdk.onState(handler);
    expect(handler).toHaveBeenCalledWith({ args: {}, value: 7 });
    sdk.dispose();
  });

  it('ignores state messages for other component names', () => {
    setupParentSpy();
    const sdk = new Streamlit4jComponent('a');
    const handler = vi.fn();
    sdk.onState(handler);
    window.dispatchEvent(
      new MessageEvent('message', {
        source: window.parent,
        data: { type: 'streamlit4j:state', name: 'b', args: {}, value: 'leaked' },
      }),
    );
    expect(handler).not.toHaveBeenCalled();
    sdk.dispose();
  });

  it('ignores messages whose source is not window.parent', () => {
    setupParentSpy();
    const sdk = new Streamlit4jComponent('a');
    const handler = vi.fn();
    sdk.onState(handler);
    window.dispatchEvent(
      new MessageEvent('message', {
        source: window,
        data: { type: 'streamlit4j:state', name: 'a', args: {}, value: 'spoofed' },
      }),
    );
    expect(handler).not.toHaveBeenCalled();
    sdk.dispose();
  });

  it('dispose() removes the message listener and clears handlers', () => {
    setupParentSpy();
    const sdk = new Streamlit4jComponent('a');
    const handler = vi.fn();
    sdk.onState(handler);
    sdk.dispose();
    window.dispatchEvent(
      new MessageEvent('message', {
        source: window.parent,
        data: { type: 'streamlit4j:state', name: 'a', args: {}, value: 'after-dispose' },
      }),
    );
    expect(handler).not.toHaveBeenCalled();
  });

  it('onState returns an unsubscribe function', () => {
    setupParentSpy();
    const sdk = new Streamlit4jComponent('a');
    const handler = vi.fn();
    const unsubscribe = sdk.onState(handler);
    unsubscribe();
    window.dispatchEvent(
      new MessageEvent('message', {
        source: window.parent,
        data: { type: 'streamlit4j:state', name: 'a', args: {}, value: 'after-unsub' },
      }),
    );
    expect(handler).not.toHaveBeenCalled();
    sdk.dispose();
  });
});
