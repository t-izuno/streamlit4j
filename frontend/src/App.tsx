import { useCallback, useEffect, useRef, useState } from 'react';
import type { Envelope, RenderNode } from './protocol';
import { applyPatches, RenderTree } from './render';
import { StreamlitClient } from './ws';

interface AppProps {
  websocketUrl?: string;
  client?: StreamlitClient;
}

export function App({ websocketUrl, client: injectedClient }: AppProps = {}) {
  const [root, setRoot] = useState<RenderNode | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const clientRef = useRef<StreamlitClient | null>(null);

  useEffect(() => {
    const client = injectedClient ?? new StreamlitClient();
    clientRef.current = client;
    client.onMessage((envelope: Envelope) => {
      if (envelope.type === 'session_init') {
        setRoot(envelope.root);
        setSessionId(envelope.sessionId);
      } else if (envelope.type === 'render_delta') {
        setRoot((prev) => applyPatches(prev, envelope.patches));
      }
    });
    if (!injectedClient) {
      const url = websocketUrl ?? defaultWebsocketUrl();
      client.connect(url);
    }
    return () => {
      if (!injectedClient) {
        client.close();
      }
    };
  }, [injectedClient, websocketUrl]);

  const handleWidgetChange = useCallback(
    (widgetId: string, value: unknown) => {
      if (!clientRef.current || !sessionId) {
        return;
      }
      clientRef.current.sendWidgetEvent(sessionId, widgetId, value);
    },
    [sessionId],
  );

  if (!root) {
    return <p role="status">Connecting…</p>;
  }
  return <RenderTree root={root} onWidgetChange={handleWidgetChange} />;
}

function defaultWebsocketUrl(): string {
  if (typeof window === 'undefined') {
    return 'ws://localhost/ws';
  }
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${proto}//${window.location.host}/ws`;
}
