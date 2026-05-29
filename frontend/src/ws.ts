import { PROTOCOL_VERSION, type Envelope, type WidgetEvent } from './protocol';

export type EnvelopeHandler = (envelope: Envelope) => void;

export class StreamlitClient {
  private socket: WebSocket | null = null;
  private handlers: EnvelopeHandler[] = [];

  connect(url: string): void {
    if (typeof WebSocket === 'undefined') {
      return;
    }
    this.socket = new WebSocket(url);
    this.socket.addEventListener('message', (event) => {
      try {
        const envelope = JSON.parse(event.data) as Envelope;
        this.handlers.forEach((handler) => handler(envelope));
      } catch (err) {
        console.error('Failed to parse envelope', err);
      }
    });
  }

  onMessage(handler: EnvelopeHandler): void {
    this.handlers.push(handler);
  }

  sendWidgetEvent(sessionId: string, widgetId: string, value: unknown): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      return;
    }
    const event: WidgetEvent = {
      v: PROTOCOL_VERSION,
      type: 'widget_event',
      sessionId,
      widgetId,
      value,
    };
    this.socket.send(JSON.stringify(event));
  }

  close(): void {
    this.socket?.close();
    this.socket = null;
  }
}
