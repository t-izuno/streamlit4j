import { act, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { App } from './App';
import { clearComponents, registerComponent } from './component-registry';
import type { Envelope } from './protocol';
import type { EnvelopeHandler } from './ws';
import { StreamlitClient } from './ws';

function createStubClient() {
  const handlers: EnvelopeHandler[] = [];
  const sent: unknown[] = [];
  const stub = {
    connect: vi.fn(),
    close: vi.fn(),
    onMessage: (h: EnvelopeHandler) => handlers.push(h),
    sendWidgetEvent: vi.fn((sessionId: string, widgetId: string, value: unknown) => {
      sent.push({ sessionId, widgetId, value });
    }),
    emit: (envelope: Envelope) => handlers.forEach((h) => h(envelope)),
    sent,
  } as unknown as StreamlitClient & {
    emit: (e: Envelope) => void;
    sent: unknown[];
  };
  return stub;
}

describe('App', () => {
  afterEach(() => {
    clearComponents();
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

  it('renders an iframe-hosted component with sandbox attribute', () => {
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
              id: 'w_i',
              props: {
                name: 'remote-widget',
                iframeSrc: 'https://example.com/widget.html',
                args: { theme: 'dark' },
              },
              children: [],
            },
          ],
        },
      });
    });
    const iframe = document.querySelector(
      'iframe[data-component-name="remote-widget"]',
    ) as HTMLIFrameElement | null;
    expect(iframe).not.toBeNull();
    expect(iframe?.getAttribute('src')).toBe('https://example.com/widget.html');
    expect(iframe?.getAttribute('sandbox')).toBe('allow-scripts');
  });

  it('forwards iframe widget events from the iframe window filtered by component name', () => {
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
              id: 'w_remote',
              props: {
                name: 'remote-widget',
                iframeSrc: 'https://example.com/widget.html',
                args: {},
              },
              children: [],
            },
          ],
        },
      });
    });
    const iframe = document.querySelector(
      'iframe[data-component-name="remote-widget"]',
    ) as HTMLIFrameElement;
    const iframeWindow = iframe.contentWindow;
    act(() => {
      window.dispatchEvent(
        new MessageEvent('message', {
          source: iframeWindow,
          origin: 'https://example.com',
          data: { type: 'streamlit4j:widget_event', name: 'other-widget', value: 'ignored' },
        }),
      );
    });
    act(() => {
      window.dispatchEvent(
        new MessageEvent('message', {
          source: iframeWindow,
          origin: 'https://example.com',
          data: { type: 'streamlit4j:widget_event', name: 'remote-widget', value: 'accepted' },
        }),
      );
    });
    expect((client as unknown as { sent: unknown[] }).sent).toEqual([
      { sessionId: 's-1', widgetId: 'w_remote', value: 'accepted' },
    ]);
  });

  it('rejects iframe widget events that violate payload boundaries', () => {
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
              id: 'w_remote',
              props: {
                name: 'remote-widget',
                iframeSrc: 'https://example.com/widget.html',
                args: {},
              },
              children: [],
            },
          ],
        },
      });
    });
    const iframe = document.querySelector(
      'iframe[data-component-name="remote-widget"]',
    ) as HTMLIFrameElement;
    act(() => {
      window.dispatchEvent(
        new MessageEvent('message', {
          source: iframe.contentWindow,
          origin: 'https://example.com',
          data: {
            type: 'streamlit4j:widget_event',
            name: 'remote-widget',
            // JSON.parse creates __proto__ as an own property, simulating
            // a structured-clone postMessage payload from a malicious iframe.
            value: JSON.parse('{"__proto__": {"polluted": true}}'),
          },
        }),
      );
    });
    act(() => {
      window.dispatchEvent(
        new MessageEvent('message', {
          source: iframe.contentWindow,
          origin: 'https://example.com',
          data: {
            type: 'streamlit4j:widget_event',
            name: 'remote-widget',
            value: { huge: 'x'.repeat(300_000) },
          },
        }),
      );
    });
    expect((client as unknown as { sent: unknown[] }).sent).toEqual([]);
  });

  it('rejects iframe widget events from unrelated windows or origins', () => {
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
              id: 'w_remote',
              props: {
                name: 'remote-widget',
                iframeSrc: 'https://example.com/widget.html',
                args: {},
              },
              children: [],
            },
          ],
        },
      });
    });
    const iframe = document.querySelector(
      'iframe[data-component-name="remote-widget"]',
    ) as HTMLIFrameElement;
    // Reject: wrong source (not the iframe window)
    act(() => {
      window.dispatchEvent(
        new MessageEvent('message', {
          source: window,
          origin: 'https://example.com',
          data: { type: 'streamlit4j:widget_event', name: 'remote-widget', value: 'spoofed' },
        }),
      );
    });
    // Reject: wrong origin (correct source but different domain)
    act(() => {
      window.dispatchEvent(
        new MessageEvent('message', {
          source: iframe.contentWindow,
          origin: 'https://attacker.example',
          data: { type: 'streamlit4j:widget_event', name: 'remote-widget', value: 'spoofed' },
        }),
      );
    });
    expect((client as unknown as { sent: unknown[] }).sent).toEqual([]);
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
});
