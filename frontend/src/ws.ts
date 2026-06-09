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
  private handlers: EnvelopeHandler[] = [];
  private stateHandlers: ConnectionStateHandler[] = [];

  connect(url: string): void {
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
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      return;
    }
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
    this.socket.send(JSON.stringify(upload));
  }

  close(): void {
    this.socket?.close();
    this.socket = null;
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
