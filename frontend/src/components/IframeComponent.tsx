import { useEffect, useMemo, useRef } from 'react';
import { validateIframePayload } from '../iframe-payload-validator';

export interface IframeComponentProps {
  name: string;
  src: string;
  args: Record<string, unknown>;
  value: unknown;
  onChange: (value: unknown) => void;
}

const READY_TYPE = 'streamlit4j:ready';
const STATE_TYPE = 'streamlit4j:state';
const WIDGET_EVENT_TYPE = 'streamlit4j:widget_event';
const NULL_ORIGIN = 'null';

/**
 * Hosts a third-party custom component in a sandboxed iframe and bridges
 * widget events via {@code postMessage}. The iframe is treated as untrusted:
 * it ships with a minimal {@code sandbox="allow-scripts"} profile so it cannot
 * read parent cookies, navigate the top frame, or co-opt user gestures.
 *
 * <p>Inbound message safety:
 *   1. {@code event.source} must equal this iframe's {@code contentWindow}
 *      — rejects messages from unrelated windows or other iframes
 *   2. {@code event.origin} must be either the iframe's src origin (when the
 *      caller opts into a relaxed sandbox) or {@code 'null'} (the canonical
 *      opaque origin for {@code allow-scripts}-only sandboxes)
 *   3. {@code data.name} must match this component's name so a misbehaving
 *      iframe cannot inject events for unrelated widgets
 *
 * <p>CSP policy for the host page (frame-src allowlist, script nonce strategy)
 * is documented in {@code docs/design.md} §9-3. Inbound widget values and
 * outbound state are funneled through {@link validateIframePayload}
 * (TASK-103) to bound size, nesting depth, and forbidden prototype keys.
 */
export function IframeComponent({ name, src, args, value, onChange }: IframeComponentProps) {
  const iframeRef = useRef<HTMLIFrameElement | null>(null);

  const expectedOrigin = useMemo(() => {
    try {
      return new URL(src, window.location.href).origin;
    } catch {
      return '';
    }
  }, [src]);

  useEffect(() => {
    const sendState = () => {
      const iframeWindow = iframeRef.current?.contentWindow;
      if (!iframeWindow) {
        return;
      }
      const payload = { args, value };
      if (!validateIframePayload(payload).ok) {
        return;
      }
      const targetOrigin = expectedOrigin && expectedOrigin !== NULL_ORIGIN ? expectedOrigin : '*';
      iframeWindow.postMessage({ type: STATE_TYPE, name, args, value }, targetOrigin);
    };
    const handleMessage = (event: MessageEvent) => {
      if (event.source !== iframeRef.current?.contentWindow) {
        return;
      }
      if (event.origin !== NULL_ORIGIN && event.origin !== expectedOrigin) {
        return;
      }
      const data = event.data;
      if (!data || typeof data !== 'object') {
        return;
      }
      const record = data as Record<string, unknown>;
      if (record.name !== name) {
        return;
      }
      if (record.type === READY_TYPE) {
        sendState();
      } else if (record.type === WIDGET_EVENT_TYPE) {
        if (!validateIframePayload(record.value).ok) {
          return;
        }
        onChange(record.value);
      }
    };
    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, [name, args, value, onChange, expectedOrigin]);

  return (
    <iframe
      ref={iframeRef}
      className="component component--iframe"
      data-component-name={name}
      src={src}
      sandbox="allow-scripts"
      title={`streamlit4j component: ${name}`}
    />
  );
}
