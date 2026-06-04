import { act, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { useComponentState } from './useComponentState';

interface ParentCall {
  data: Record<string, unknown>;
  targetOrigin: string;
}

function setupParentSpy(): ParentCall[] {
  const calls: ParentCall[] = [];
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

function ProbeComponent({ name }: { name: string }) {
  const { state, setValue } = useComponentState<{ tone: string }, number>(name);
  if (!state) {
    return <p data-testid="probe">no-state</p>;
  }
  return (
    <button data-testid="probe" onClick={() => setValue((state.value ?? 0) + 1)}>
      tone={state.args.tone};value={String(state.value)}
    </button>
  );
}

describe('useComponentState React hook', () => {
  afterEach(() => {
    teardownParentSpy();
    vi.restoreAllMocks();
  });

  it('renders nothing-state placeholder before host responds', () => {
    setupParentSpy();
    render(<ProbeComponent name="alpha" />);
    expect(screen.getByTestId('probe')).toHaveTextContent('no-state');
  });

  it('signals ready on mount via postMessage to parent', () => {
    const sent = setupParentSpy();
    render(<ProbeComponent name="alpha" />);
    expect(sent).toContainEqual({
      data: { type: 'streamlit4j:ready', name: 'alpha' },
      targetOrigin: '*',
    });
  });

  it('re-renders when host posts a state message', () => {
    setupParentSpy();
    render(<ProbeComponent name="beta" />);
    act(() => {
      window.dispatchEvent(
        new MessageEvent('message', {
          source: window.parent,
          data: { type: 'streamlit4j:state', name: 'beta', args: { tone: 'warm' }, value: 3 },
        }),
      );
    });
    expect(screen.getByTestId('probe')).toHaveTextContent('tone=warm;value=3');
  });

  it('setValue posts a widget_event envelope back to the host', () => {
    const sent = setupParentSpy();
    render(<ProbeComponent name="counter" />);
    act(() => {
      window.dispatchEvent(
        new MessageEvent('message', {
          source: window.parent,
          data: { type: 'streamlit4j:state', name: 'counter', args: { tone: '' }, value: 5 },
        }),
      );
    });
    act(() => {
      screen.getByTestId('probe').click();
    });
    expect(sent).toContainEqual({
      data: { type: 'streamlit4j:widget_event', name: 'counter', value: 6 },
      targetOrigin: '*',
    });
  });
});
