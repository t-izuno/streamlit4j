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

export interface FileUpload {
  v: number;
  type: 'file_upload';
  sessionId: string;
  widgetId: string;
  filename: string;
  mimeType: string;
  contentBase64: string;
}

export interface ReloadNotice {
  v: number;
  type: 'reload';
  sessionId: string;
  reason: string;
}

export type Envelope =
  | SessionInit
  | RenderDelta
  | WidgetEvent
  | ErrorMessage
  | FileUpload
  | ReloadNotice;

export const PROTOCOL_VERSION = 1;
