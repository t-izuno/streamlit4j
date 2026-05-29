import { Markdown } from './components/Markdown';
import { Slider } from './components/Slider';
import { Title } from './components/Title';
import { Write } from './components/Write';
import type { Patch, RenderNode } from './protocol';

export type WidgetChangeHandler = (widgetId: string, value: unknown) => void;

export function applyPatches(current: RenderNode | null, patches: Patch[]): RenderNode | null {
  let next = current;
  for (const patch of patches) {
    if (patch.op === 'replace' && patch.path === '/') {
      next = patch.node;
    }
  }
  return next;
}

export function RenderTree({
  root,
  onWidgetChange,
}: {
  root: RenderNode;
  onWidgetChange: WidgetChangeHandler;
}) {
  return <>{root.children.map((node) => renderNode(node, onWidgetChange))}</>;
}

function renderNode(node: RenderNode, onWidgetChange: WidgetChangeHandler) {
  switch (node.kind) {
    case 'title':
      return <Title key={node.id} text={String(node.props.text ?? '')} />;
    case 'markdown':
      return <Markdown key={node.id} body={String(node.props.body ?? '')} />;
    case 'write':
      return <Write key={node.id} value={String(node.props.value ?? '')} />;
    case 'slider':
      return (
        <Slider
          key={node.id}
          label={String(node.props.label ?? '')}
          min={Number(node.props.min ?? 0)}
          max={Number(node.props.max ?? 0)}
          value={Number(node.props.value ?? 0)}
          onChange={(v) => onWidgetChange(node.id, v)}
        />
      );
    default:
      return (
        <div key={node.id} data-unknown-kind={node.kind}>
          Unknown element: {node.kind}
        </div>
      );
  }
}
