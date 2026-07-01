import { describe, expect, it, vi } from 'vitest';
import { StreamlitClient } from './ws';

class FakeEventSource {
  static instances: FakeEventSource[] = [];
  onopen: (() => void) | null = null;
  onmessage: ((event: MessageEvent<string>) => void) | null = null;
  onerror: (() => void) | null = null;
  closed = false;

  constructor(public readonly url: string) {
    FakeEventSource.instances.push(this);
  }

  close() {
    this.closed = true;
  }
}

describe('StreamlitClient SSE transport', () => {
  it('receives envelopes from EventSource and posts widget events over HTTP', async () => {
    vi.stubGlobal('EventSource', FakeEventSource);
    const fetchMock = vi.fn().mockResolvedValue({ ok: true });
    vi.stubGlobal('fetch', fetchMock);
    const client = new StreamlitClient();
    const envelopes: unknown[] = [];
    const states: string[] = [];
    client.onMessage((envelope) => envelopes.push(envelope));
    client.onConnectionStateChange((state) => states.push(state));

    client.connect('http://localhost/events');
    const source = FakeEventSource.instances[0];
    source.onopen?.();
    source.onmessage?.(
      new MessageEvent('message', {
        data: JSON.stringify({ v: 1, type: 'session_init', sessionId: 's-1', root: {} }),
      }),
    );
    client.sendWidgetEvent('s-1', 'w-1', 'hello');

    expect(source.url).toBe('http://localhost/events');
    expect(states).toContain('open');
    expect(envelopes).toHaveLength(1);
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost/events',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          v: 1,
          type: 'widget_event',
          sessionId: 's-1',
          widgetId: 'w-1',
          value: 'hello',
        }),
      }),
    );
  });
});
