import { useCallback, useEffect, useRef, useState } from 'react';
import type { Envelope, RenderNode } from './protocol';
import { applyPatches, RenderTree } from './render';
import { applyTheme, readStoredTheme, type Theme } from './theme';
import { StreamlitClient } from './ws';

interface AppProps {
  websocketUrl?: string;
  client?: StreamlitClient;
}

export function App({ websocketUrl, client: injectedClient }: AppProps = {}) {
  const [root, setRoot] = useState<RenderNode | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [theme, setTheme] = useState<Theme>(readStoredTheme());
  const clientRef = useRef<StreamlitClient | null>(null);

  useEffect(() => {
    applyTheme(theme);
  }, [theme]);

  useEffect(() => {
    const client = injectedClient ?? new StreamlitClient();
    clientRef.current = client;
    client.onMessage((envelope: Envelope) => {
      if (envelope.type === 'session_init') {
        setRoot(envelope.root);
        setSessionId(envelope.sessionId);
      } else if (envelope.type === 'render_delta') {
        setRoot((prev) => applyPatches(prev, envelope.patches));
      } else if (envelope.type === 'reload' && typeof window !== 'undefined') {
        window.location.reload();
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

  useEffect(() => {
    if (!sessionId || !clientRef.current) return;
    const syncPageFromHash = () => {
      const hash = window.location.hash.replace(/^#/, '');
      if (hash) {
        clientRef.current?.sendWidgetEvent(sessionId, '__page__', hash);
      }
    };
    syncPageFromHash();
    window.addEventListener('hashchange', syncPageFromHash);
    return () => window.removeEventListener('hashchange', syncPageFromHash);
  }, [sessionId]);

  const handleWidgetChange = useCallback(
    (widgetId: string, value: unknown) => {
      if (!clientRef.current || !sessionId) {
        return;
      }
      if (widgetId === '__page__' && typeof value === 'string') {
        window.location.hash = value;
      }
      if (value instanceof File) {
        void clientRef.current.sendFileUpload(sessionId, widgetId, value);
        return;
      }
      clientRef.current.sendWidgetEvent(sessionId, widgetId, value);
    },
    [sessionId],
  );

  if (!root) {
    return <p role="status">Connecting…</p>;
  }
  return (
    <div className="streamlit4j-shell">
      <div className="streamlit4j-shell__toolbar">
        <button
          type="button"
          aria-label="Toggle theme"
          onClick={() => setTheme((t) => (t === 'light' ? 'dark' : 'light'))}
        >
          {theme === 'light' ? '🌙' : '☀️'}
        </button>
      </div>
      <main className="streamlit4j-shell__main">
        <RenderTree root={root} onWidgetChange={handleWidgetChange} />
      </main>
    </div>
  );
}

function defaultWebsocketUrl(): string {
  if (typeof window === 'undefined') {
    return 'ws://localhost/ws';
  }
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${proto}//${window.location.host}/ws`;
}
