import { useEffect, useMemo, useState, useCallback } from 'react';
import { Streamlit4jComponent, type ComponentState } from './Streamlit4jComponent';

export interface UseComponentStateResult<TArgs, TValue> {
  /** The most recently delivered host state, or null before the first message. */
  state: ComponentState<TArgs, TValue> | null;
  /** Sends a new widget value back to the streamlit4j host. */
  setValue: (value: TValue) => void;
  /** Component name as declared by the host. */
  name: string;
}

/**
 * React bridge for the {@link Streamlit4jComponent} SDK. Subscribes to host
 * state messages, triggers a re-render whenever a new state arrives, and
 * signals {@code ready} on mount so the host posts the initial state.
 *
 * <p>The hook returns the latest {@code state} together with a {@code setValue}
 * callback. State is {@code null} until the host responds; consumers should
 * branch on that to render a loading placeholder.
 *
 * @example
 * function MyComponent() {
 *   const { state, setValue } = useComponentState<{ tone: string }, number>('counter');
 *   if (!state) return null;
 *   return <button onClick={() => setValue((state.value ?? 0) + 1)}>+1</button>;
 * }
 */
export function useComponentState<TArgs = Record<string, unknown>, TValue = unknown>(
  name: string,
): UseComponentStateResult<TArgs, TValue> {
  const sdk = useMemo(() => new Streamlit4jComponent<TArgs, TValue>(name), [name]);
  const [state, setState] = useState<ComponentState<TArgs, TValue> | null>(() =>
    sdk.currentState(),
  );

  useEffect(() => {
    const unsubscribe = sdk.onState((next) => setState(next));
    sdk.ready();
    return () => {
      unsubscribe();
      sdk.dispose();
    };
  }, [sdk]);

  const setValue = useCallback(
    (value: TValue) => {
      sdk.setValue(value);
    },
    [sdk],
  );

  return { state, setValue, name };
}
