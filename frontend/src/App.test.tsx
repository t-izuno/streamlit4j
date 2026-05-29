import { act, fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { App } from './App';
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
