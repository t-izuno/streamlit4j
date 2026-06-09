import { useCallback, useEffect, useRef, useState } from 'react';
import { ErrorBanner, type AppError } from './components/ErrorBanner';
import type { Envelope, RenderNode } from './protocol';
import { applyPatches, RenderTree } from './render';
import { applyTheme, readStoredTheme, type Theme } from './theme';
import { StreamlitClient } from './ws';

let errorIdCounter = 0;
function nextErrorId(): string {
  errorIdCounter += 1;
  return `err_${errorIdCounter}`;
}

interface AppProps {
  websocketUrl?: string;
  client?: StreamlitClient;
}

export function App({ websocketUrl, client: injectedClient }: AppProps = {}) {
  const [root, setRoot] = useState<RenderNode | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [theme, setTheme] = useState<Theme>(readStoredTheme());
  const [errors, setErrors] = useState<AppError[]>([]);
  const clientRef = useRef<StreamlitClient | null>(null);
  const closingByUnmountRef = useRef(false);
  const everOpenedRef = useRef(false);

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
      } else if (envelope.type === 'error') {
        setErrors((prev) => [
          ...prev,
          {
            id: nextErrorId(),
            kind: 'script',
            title: 'Script error',
            message: envelope.message || 'Unknown error',
            stackTrace: envelope.stackTrace || undefined,
          },
        ]);
      } else if (envelope.type === 'reload' && typeof window !== 'undefined') {
        window.location.reload();
      }
    });
    client.onConnectionStateChange((state, info) => {
      if (state === 'open') {
        everOpenedRef.current = true;
        return;
      }
      if (state === 'error') {
        setErrors((prev) => [
          ...prev,
          {
            id: nextErrorId(),
            kind: 'connection',
            title: 'Connection error',
            message: info.reason || 'WebSocket error',
          },
        ]);
        return;
      }
      if (state === 'closed') {
        if (closingByUnmountRef.current) return;
        const codeLabel = info.code !== undefined ? `code ${info.code}` : 'closed';
        const message = info.reason || `WebSocket ${codeLabel}`;
        setErrors((prev) => [
          ...prev,
          {
            id: nextErrorId(),
            kind: 'connection',
            title: everOpenedRef.current ? 'Connection lost' : 'Failed to connect',
            message,
          },
        ]);
      }
    });
    if (!injectedClient) {
      const url = websocketUrl ?? defaultWebsocketUrl();
      client.connect(url);
    }
    return () => {
      if (!injectedClient) {
        closingByUnmountRef.current = true;
        client.close();
      }
    };
  }, [injectedClient, websocketUrl]);

  const dismissError = useCallback((id: string) => {
    setErrors((prev) => prev.filter((e) => e.id !== id));
  }, []);

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
    return (
      <div className="streamlit4j-shell streamlit4j-shell--loading">
        <ErrorBanner errors={errors} onDismiss={dismissError} />
        <p role="status">Connecting…</p>
      </div>
    );
  }
  return (
    <div className="streamlit4j-shell">
      <div className="streamlit4j-shell__toolbar">
        <div className="theme-switcher" role="radiogroup" aria-label="Theme">
          <button
            type="button"
            role="radio"
            aria-checked={theme === 'light'}
            aria-label="Light mode"
            className="theme-switcher__btn"
            data-active={theme === 'light'}
            onClick={() => setTheme('light')}
          >
            <svg
              className="theme-switcher__icon"
              viewBox="0 0 24 24"
              aria-hidden="true"
              focusable="false"
            >
              <circle cx="12" cy="12" r="4" />
              <g strokeLinecap="round">
                <line x1="12" y1="2.5" x2="12" y2="5.5" />
                <line x1="12" y1="18.5" x2="12" y2="21.5" />
                <line x1="2.5" y1="12" x2="5.5" y2="12" />
                <line x1="18.5" y1="12" x2="21.5" y2="12" />
                <line x1="5.1" y1="5.1" x2="7.2" y2="7.2" />
                <line x1="16.8" y1="16.8" x2="18.9" y2="18.9" />
                <line x1="5.1" y1="18.9" x2="7.2" y2="16.8" />
                <line x1="16.8" y1="7.2" x2="18.9" y2="5.1" />
              </g>
            </svg>
          </button>
          <button
            type="button"
            role="radio"
            aria-checked={theme === 'dark'}
            aria-label="Dark mode"
            className="theme-switcher__btn"
            data-active={theme === 'dark'}
            onClick={() => setTheme('dark')}
          >
            <svg
              className="theme-switcher__icon theme-switcher__icon--filled"
              viewBox="0 0 24 24"
              aria-hidden="true"
              focusable="false"
            >
              <path d="M19.5 14.5A8 8 0 0 1 9.5 4.5a8 8 0 1 0 10 10z" />
            </svg>
          </button>
        </div>
      </div>
      <ErrorBanner errors={errors} onDismiss={dismissError} />
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
