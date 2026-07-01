import { PROTOCOL_VERSION, type Envelope, type WidgetEvent } from './protocol';

export type EnvelopeHandler = (envelope: Envelope) => void;

export type ConnectionState = 'connecting' | 'open' | 'error' | 'closed';

export interface ConnectionStateInfo {
  reason?: string;
  code?: number;
}

export type ConnectionStateHandler = (state: ConnectionState, info: ConnectionStateInfo) => void;

export class StreamlitClient {
  private socket: WebSocket | null = null;
  private eventSource: EventSource | null = null;
  private eventsUrl: string | null = null;
  private handlers: EnvelopeHandler[] = [];
  private stateHandlers: ConnectionStateHandler[] = [];

  connect(url: string): void {
    if (url.startsWith('http://') || url.startsWith('https://')) {
      this.connectSse(url);
      return;
    }
    if (typeof WebSocket === 'undefined') {
      return;
    }
    this.notifyState('connecting', {});
    this.socket = new WebSocket(url);
    this.socket.addEventListener('open', () => {
      this.notifyState('open', {});
    });
    this.socket.addEventListener('message', (event) => {
      try {
        const envelope = JSON.parse(event.data) as Envelope;
        this.handlers.forEach((handler) => handler(envelope));
      } catch (err) {
        console.error('Failed to parse envelope', err);
        this.notifyState('error', { reason: String(err) });
      }
    });
    this.socket.addEventListener('error', () => {
      this.notifyState('error', {});
    });
    this.socket.addEventListener('close', (event) => {
      this.notifyState('closed', { reason: event.reason, code: event.code });
    });
  }

  onMessage(handler: EnvelopeHandler): void {
    this.handlers.push(handler);
  }

  onConnectionStateChange(handler: ConnectionStateHandler): void {
    this.stateHandlers.push(handler);
  }

  sendWidgetEvent(sessionId: string, widgetId: string, value: unknown): void {
    if (this.eventsUrl) {
      void this.postEnvelope({
        v: PROTOCOL_VERSION,
        type: 'widget_event',
        sessionId,
        widgetId,
        value,
      });
      return;
    }
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

  async sendFileUpload(sessionId: string, widgetId: string, file: File): Promise<void> {
    const buffer = await file.arrayBuffer();
    const contentBase64 = bytesToBase64(new Uint8Array(buffer));
    const upload = {
      v: PROTOCOL_VERSION,
      type: 'file_upload' as const,
      sessionId,
      widgetId,
      filename: file.name,
      mimeType: file.type,
      contentBase64,
    };
    if (this.eventsUrl) {
      await this.postEnvelope(upload);
      return;
    }
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      return;
    }
    this.socket.send(JSON.stringify(upload));
  }

  close(): void {
    this.socket?.close();
    this.socket = null;
    this.eventSource?.close();
    this.eventSource = null;
    this.eventsUrl = null;
  }

  private connectSse(url: string): void {
    if (typeof EventSource === 'undefined') {
      return;
    }
    this.notifyState('connecting', {});
    this.eventsUrl = url;
    this.eventSource = new EventSource(url);
    this.eventSource.onopen = () => this.notifyState('open', {});
    this.eventSource.onmessage = (event) => {
      try {
        const envelope = JSON.parse(event.data) as Envelope;
        this.handlers.forEach((handler) => handler(envelope));
      } catch (err) {
        console.error('Failed to parse envelope', err);
        this.notifyState('error', { reason: String(err) });
      }
    };
    this.eventSource.onerror = () => this.notifyState('error', {});
  }

  private async postEnvelope(envelope: Envelope): Promise<void> {
    if (!this.eventsUrl || typeof fetch === 'undefined') {
      return;
    }
    await fetch(this.eventsUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(envelope),
    });
  }

  private notifyState(state: ConnectionState, info: ConnectionStateInfo): void {
    this.stateHandlers.forEach((handler) => handler(state, info));
  }
}

function bytesToBase64(bytes: Uint8Array): string {
  let binary = '';
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}
