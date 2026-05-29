export interface RenderNode {
  kind: string;
  id: string;
  props: Record<string, unknown>;
  children: RenderNode[];
}

export interface Patch {
  op: string;
  path: string;
  node: RenderNode;
}

export interface SessionInit {
  v: number;
  type: 'session_init';
  sessionId: string;
  root: RenderNode;
}

export interface RenderDelta {
  v: number;
  type: 'render_delta';
  sessionId: string;
  seq: number;
  patches: Patch[];
}

export interface WidgetEvent {
  v: number;
  type: 'widget_event';
  sessionId: string;
  widgetId: string;
  value: unknown;
}

export interface ErrorMessage {
  v: number;
  type: 'error';
  sessionId: string;
  message: string;
  stackTrace: string;
}

export type Envelope = SessionInit | RenderDelta | WidgetEvent | ErrorMessage;

export const PROTOCOL_VERSION = 1;
