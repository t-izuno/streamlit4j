import { act, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { App } from './App';
import { clearComponents, registerChatComponent, registerComponent } from './component-registry';
import type { Envelope } from './protocol';
import type {
  ConnectionState,
  ConnectionStateHandler,
  ConnectionStateInfo,
  EnvelopeHandler,
} from './ws';
import { StreamlitClient } from './ws';

function createStubClient() {
  const handlers: EnvelopeHandler[] = [];
  const stateHandlers: ConnectionStateHandler[] = [];
  const sent: unknown[] = [];
  const stub = {
    connect: vi.fn(),
    close: vi.fn(),
    onMessage: (h: EnvelopeHandler) => handlers.push(h),
    onConnectionStateChange: (h: ConnectionStateHandler) => stateHandlers.push(h),
    sendWidgetEvent: vi.fn((sessionId: string, widgetId: string, value: unknown) => {
      sent.push({ sessionId, widgetId, value });
    }),
    sendFileUpload: vi.fn((sessionId: string, widgetId: string, file: File) => {
      sent.push({ sessionId, widgetId, file });
      return Promise.resolve();
    }),
    emit: (envelope: Envelope) => handlers.forEach((h) => h(envelope)),
    emitState: (state: ConnectionState, info: ConnectionStateInfo = {}) =>
      stateHandlers.forEach((h) => h(state, info)),
    sent,
  } as unknown as StreamlitClient & {
    emit: (e: Envelope) => void;
    emitState: (state: ConnectionState, info?: ConnectionStateInfo) => void;
    sent: unknown[];
  };
  return stub;
}

describe('App', () => {
  afterEach(() => {
    clearComponents();
    window.localStorage.clear();
    delete document.documentElement.dataset.theme;
  });

  it('shows connecting status before SessionInit arrives', () => {
    const client = createStubClient();
    render(<App client={client} />);
    expect(screen.getByRole('status')).toHaveTextContent(/connecting/i);
  });

  it('renders title element from SessionInit', () => {
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: {
          kind: 'root',
          id: 'root',
          props: {},
          children: [{ kind: 'title', id: 'w_t', props: { text: 'Hi' }, children: [] }],
        },
      });
    });
    expect(screen.getByRole('heading', { name: 'Hi' })).toBeInTheDocument();
  });

  it('switches and persists the built-in theme', () => {
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: { kind: 'root', id: 'root', props: {}, children: [] },
      });
    });

    fireEvent.click(screen.getByRole('radio', { name: 'Dark mode' }));
    expect(document.documentElement.dataset.theme).toBe('dark');
    expect(window.localStorage.getItem('streamlit4j.theme')).toBe('dark');
    fireEvent.click(screen.getByRole('radio', { name: 'Light mode' }));
    expect(document.documentElement.dataset.theme).toBe('light');
  });

  it('replaces tree from render_delta with op replace at /', () => {
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: {
          kind: 'root',
          id: 'root',
          props: {},
          children: [{ kind: 'title', id: 'w_t', props: { text: 'Before' }, children: [] }],
        },
      });
    });
    act(() => {
      client.emit({
        v: 1,
        type: 'render_delta',
        sessionId: 's-1',
        seq: 1,
        patches: [
          {
            op: 'replace',
            path: '/',
            node: {
              kind: 'root',
              id: 'root',
              props: {},
              children: [{ kind: 'title', id: 'w_t', props: { text: 'After' }, children: [] }],
            },
          },
        ],
      });
    });
    expect(screen.getByRole('heading', { name: 'After' })).toBeInTheDocument();
  });

  it('renders a registered custom component and forwards widget events', () => {
    registerComponent('color-badge', ({ args, value, onChange }) => (
      <button type="button" data-tone={String(args.tone ?? '')} onClick={() => onChange('clicked')}>
        badge:{String(value ?? 'none')}
      </button>
    ));
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: {
          kind: 'root',
          id: 'root',
          props: {},
          children: [
            {
              kind: 'component',
              id: 'w_c',
              props: { name: 'color-badge', args: { tone: 'warm' }, value: 'initial' },
              children: [],
            },
          ],
        },
      });
    });
    const badge = screen.getByRole('button', { name: /badge:initial/ });
    expect(badge).toHaveAttribute('data-tone', 'warm');
    fireEvent.click(badge);
    expect((client as unknown as { sent: unknown[] }).sent).toEqual([
      { sessionId: 's-1', widgetId: 'w_c', value: 'clicked' },
    ]);
  });

  it('falls back to placeholder for unregistered component names', () => {
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: {
          kind: 'root',
          id: 'root',
          props: {},
          children: [
            {
              kind: 'component',
              id: 'w_u',
              props: { name: 'unknown-comp', args: {} },
              children: [],
            },
          ],
        },
      });
    });
    const placeholder = document.querySelector('.component--unregistered');
    expect(placeholder).not.toBeNull();
    expect(placeholder?.getAttribute('data-component-name')).toBe('unknown-comp');
  });

  it('sends widget_event when slider changes', () => {
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: {
          kind: 'root',
          id: 'root',
          props: {},
          children: [
            {
              kind: 'slider',
              id: 'w_year',
              props: { label: 'Year', min: 2018, max: 2026, value: 2025 },
              children: [],
            },
          ],
        },
      });
    });
    const slider = screen.getByRole('slider');
    fireEvent.change(slider, { target: { value: '2024' } });
    expect((client as unknown as { sent: unknown[] }).sent).toEqual([
      { sessionId: 's-1', widgetId: 'w_year', value: 2024 },
    ]);
  });

  it('renders chat message and reveals streamed tokens incrementally', () => {
    vi.useFakeTimers();
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: {
          kind: 'root',
          id: 'root',
          props: {},
          children: [
            {
              kind: 'chat_message',
              id: 'w_msg',
              props: { role: 'assistant', content: 'Hello **there**' },
              children: [],
            },
            {
              kind: 'chat_stream',
              id: 'w_stream',
              props: { tokens: ['Hel', 'lo'] },
              children: [],
            },
          ],
        },
      });
    });
    expect(screen.getByText('assistant')).toBeInTheDocument();
    expect(screen.getByText('there')).toBeInTheDocument();
    expect(screen.getByRole('status', { name: 'streamed response' })).toHaveTextContent('Hel');
    expect(screen.getByRole('status', { name: 'streamed response' })).not.toHaveTextContent(
      'Hello',
    );
    act(() => {
      vi.runAllTimers();
    });
    expect(screen.getByRole('status', { name: 'streamed response' })).toHaveTextContent('Hello');
    vi.useRealTimers();
  });

  it('sends chat input value on enter', () => {
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: {
          kind: 'root',
          id: 'root',
          props: {},
          children: [
            {
              kind: 'chat_input',
              id: 'w_chat',
              props: { label: 'Ask', value: '' },
              children: [],
            },
          ],
        },
      });
    });
    const input = screen.getByLabelText('Ask');
    fireEvent.change(input, { target: { value: 'Explain SSE' } });
    fireEvent.keyDown(input, { key: 'Enter' });
    expect((client as unknown as { sent: unknown[] }).sent).toEqual([
      { sessionId: 's-1', widgetId: 'w_chat', value: 'Explain SSE' },
    ]);
  });

  it('sends chat control actions', () => {
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: {
          kind: 'root',
          id: 'root',
          props: {},
          children: [
            {
              kind: 'chat_controls',
              id: 'w_controls',
              props: {},
              children: [],
            },
          ],
        },
      });
    });

    fireEvent.click(screen.getByRole('button', { name: 'Stop' }));
    fireEvent.click(screen.getByRole('button', { name: 'Retry' }));
    const editInput = screen.getByLabelText('Edit prompt');
    fireEvent.change(editInput, { target: { value: 'Edited prompt' } });
    fireEvent.click(screen.getByRole('button', { name: 'Regenerate' }));

    expect((client as unknown as { sent: unknown[] }).sent).toEqual([
      { sessionId: 's-1', widgetId: 'w_controls', value: { action: 'stop' } },
      { sessionId: 's-1', widgetId: 'w_controls', value: { action: 'retry' } },
      {
        sessionId: 's-1',
        widgetId: 'w_controls',
        value: { action: 'edit_regenerate', value: 'Edited prompt' },
      },
    ]);
  });

  it('renders registered chat component overrides', () => {
    registerChatComponent('container', ({ children }) => (
      <section data-testid="custom-chat-container">{children}</section>
    ));
    registerChatComponent('message', ({ args }) => (
      <p data-testid="custom-chat-message">
        {String(args.role)}:{String(args.content)}
      </p>
    ));
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: {
          kind: 'root',
          id: 'root',
          props: {},
          children: [
            {
              kind: 'chat_container',
              id: 'w_chat_container',
              props: {},
              children: [
                {
                  kind: 'chat_message',
                  id: 'w_msg',
                  props: { role: 'assistant', content: 'Hi' },
                  children: [],
                },
              ],
            },
          ],
        },
      });
    });

    expect(screen.getByTestId('custom-chat-container')).toBeInTheDocument();
    expect(screen.getByTestId('custom-chat-message')).toHaveTextContent('assistant:Hi');
  });

  it('renders rich chat children and tool results', () => {
    registerComponent('answer-card', ({ args }) => (
      <aside data-testid="answer-card">{String(args.title)}</aside>
    ));
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: {
          kind: 'root',
          id: 'root',
          props: {},
          children: [
            {
              kind: 'chat_message',
              id: 'w_msg',
              props: { role: 'assistant' },
              children: [
                {
                  kind: 'code',
                  id: 'w_code',
                  props: { body: 'System.out.println(1);', language: 'java' },
                  children: [],
                },
                {
                  kind: 'file_uploader',
                  id: 'w_upload',
                  props: { label: 'Attach context' },
                  children: [],
                },
                {
                  kind: 'download_button',
                  id: 'w_download',
                  props: { label: 'Download answer', url: '/download/a1' },
                  children: [],
                },
                {
                  kind: 'tool_result',
                  id: 'w_tool',
                  props: { title: 'Search', status: 'success' },
                  children: [
                    {
                      kind: 'table',
                      id: 'w_table',
                      props: { rows: [{ name: 'streamlit4j' }] },
                      children: [],
                    },
                    {
                      kind: 'component',
                      id: 'w_component',
                      props: { name: 'answer-card', args: { title: 'Generated panel' } },
                      children: [],
                    },
                  ],
                },
              ],
            },
          ],
        },
      });
    });

    expect(screen.getByLabelText('assistant message')).toHaveTextContent('System.out.println(1);');
    const file = new File(['notes'], 'notes.txt', { type: 'text/plain' });
    fireEvent.change(screen.getByLabelText('Attach context'), { target: { files: [file] } });
    expect((client as unknown as { sent: unknown[] }).sent).toEqual([
      { sessionId: 's-1', widgetId: 'w_upload', file },
    ]);
    expect(screen.getByRole('link', { name: 'Download answer' })).toHaveAttribute(
      'href',
      '/download/a1',
    );
    expect(screen.getByText('Search')).toBeInTheDocument();
    expect(screen.getByText('success')).toBeInTheDocument();
    expect(screen.getByRole('table')).toHaveTextContent('streamlit4j');
    expect(screen.getByTestId('answer-card')).toHaveTextContent('Generated panel');
  });

  it('shows a script error banner with message and stack trace', () => {
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: { kind: 'root', id: 'root', props: {}, children: [] },
      });
    });
    act(() => {
      client.emit({
        v: 1,
        type: 'error',
        sessionId: 's-1',
        message: 'NullPointerException at app',
        stackTrace: 'at line 42\n  at line 43',
      });
    });
    const alert = screen.getByRole('alert');
    expect(alert).toHaveTextContent('Script error');
    expect(alert).toHaveTextContent('NullPointerException at app');
    expect(alert).toHaveTextContent('at line 42');
  });

  it('dismisses an error when the close button is clicked', () => {
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: { kind: 'root', id: 'root', props: {}, children: [] },
      });
    });
    act(() => {
      client.emit({
        v: 1,
        type: 'error',
        sessionId: 's-1',
        message: 'Boom',
        stackTrace: '',
      });
    });
    expect(screen.getByRole('alert')).toHaveTextContent('Boom');
    fireEvent.click(screen.getByRole('button', { name: /dismiss error/i }));
    expect(screen.queryByRole('alert')).toBeNull();
  });

  it('shows a connection-lost banner after the socket closes', () => {
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emit({
        v: 1,
        type: 'session_init',
        sessionId: 's-1',
        root: { kind: 'root', id: 'root', props: {}, children: [] },
      });
      client.emitState('open');
    });
    act(() => {
      client.emitState('closed', { code: 1006, reason: '' });
    });
    const alert = screen.getByRole('alert');
    expect(alert).toHaveTextContent('Connection lost');
    expect(alert).toHaveTextContent('code 1006');
  });

  it('shows a failed-to-connect banner if the socket closes without ever opening', () => {
    const client = createStubClient();
    render(<App client={client} />);
    act(() => {
      client.emitState('closed', { code: 1006 });
    });
    const alert = screen.getByRole('alert');
    expect(alert).toHaveTextContent('Failed to connect');
  });
});
