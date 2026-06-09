import DOMPurify from 'dompurify';
import type { JSX } from 'react';
import { findComponent } from './component-registry';
import { Chart } from './components/Chart';
import { Markdown } from './components/Markdown';
import { Slider } from './components/Slider';
import { Tabs } from './components/Tabs';
import { Select, TextArea, TextField } from './components/TextField';
import { Title } from './components/Title';
import { Write } from './components/Write';
import type { Patch, RenderNode } from './protocol';

export type WidgetChangeHandler = (widgetId: string, value: unknown) => void;

export function applyPatches(current: RenderNode | null, patches: Patch[]): RenderNode | null {
  let next = current;
  for (const patch of patches) {
    if (patch.path === '/') {
      next = patch.node;
    } else if (next) {
      next = applyAtPath(next, patch);
    }
  }
  return next;
}

function applyAtPath(root: RenderNode, patch: Patch): RenderNode {
  const segments = patch.path.split('/').filter((s) => s);
  if (segments[0] !== 'main' || segments.length !== 2) return root;
  const idx = parseInt(segments[1], 10);
  const children = [...root.children];
  if (patch.op === 'replace' && patch.node) {
    children[idx] = patch.node;
  } else if (patch.op === 'insert' && patch.node) {
    children.splice(idx, 0, patch.node);
  } else if (patch.op === 'remove') {
    children.splice(idx, 1);
  }
  return { ...root, children };
}

export function RenderTree({
  root,
  onWidgetChange,
}: {
  root: RenderNode;
  onWidgetChange: WidgetChangeHandler;
}) {
  const sidebars = root.children.filter((c) => c.kind === 'sidebar');
  const mainChildren = root.children.filter((c) => c.kind !== 'sidebar');
  return (
    <div className="streamlit4j-layout">
      {sidebars.map((node) => renderNode(node, onWidgetChange))}
      <div className="streamlit4j-layout__main">
        {mainChildren.map((node) => renderNode(node, onWidgetChange))}
      </div>
    </div>
  );
}

function renderNode(node: RenderNode, onChange: WidgetChangeHandler): JSX.Element {
  const props = node.props;
  switch (node.kind) {
    case 'title':
      return <Title key={node.id} text={String(props.text ?? '')} />;
    case 'header':
      return <h2 key={node.id}>{String(props.text ?? '')}</h2>;
    case 'subheader':
      return <h3 key={node.id}>{String(props.text ?? '')}</h3>;
    case 'caption':
      return <small key={node.id}>{String(props.text ?? '')}</small>;
    case 'markdown':
      return <Markdown key={node.id} body={String(props.body ?? '')} />;
    case 'write':
      return <Write key={node.id} value={String(props.value ?? '')} />;
    case 'code':
      return (
        <pre key={node.id} className="code" data-language={String(props.language ?? '')}>
          <code>{String(props.body ?? '')}</code>
        </pre>
      );
    case 'json':
      return (
        <pre key={node.id} className="json">
          {String(props.body ?? '')}
        </pre>
      );
    case 'latex':
      return (
        <span key={node.id} className="latex">
          {String(props.body ?? '')}
        </span>
      );
    case 'html': {
      const sanitized = DOMPurify.sanitize(String(props.body ?? ''));
      return <div key={node.id} dangerouslySetInnerHTML={{ __html: sanitized }} />;
    }
    case 'divider':
      return <hr key={node.id} />;
    case 'metric':
      return (
        <div key={node.id} className="metric">
          <div className="metric__label">{String(props.label ?? '')}</div>
          <div className="metric__value">{String(props.value ?? '')}</div>
          {props.delta !== undefined && <div className="metric__delta">{String(props.delta)}</div>}
        </div>
      );
    case 'dataframe':
    case 'table':
    case 'data_editor':
      return renderTable(node);
    case 'image':
      return <img key={node.id} src={String(props.src ?? '')} alt="" />;
    case 'audio':
      return <audio key={node.id} src={String(props.src ?? '')} controls />;
    case 'video':
      return <video key={node.id} src={String(props.src ?? '')} controls />;
    case 'toast':
      return (
        <div key={node.id} role="status" className="toast">
          {String(props.text ?? '')}
        </div>
      );
    case 'progress':
      return <progress key={node.id} value={Number(props.value ?? 0)} max={1} />;
    case 'spinner':
      return (
        <div key={node.id} className="spinner" role="status" aria-busy>
          {String(props.text ?? '')}
        </div>
      );
    case 'status':
      return (
        <div key={node.id} role="status">
          {String(props.text ?? '')}
        </div>
      );
    case 'line_chart':
    case 'bar_chart':
    case 'area_chart':
    case 'scatter_chart':
      return renderChart(node);
    case 'slider':
      return (
        <Slider
          key={node.id}
          label={String(props.label ?? '')}
          min={Number(props.min ?? 0)}
          max={Number(props.max ?? 0)}
          value={Number(props.value ?? 0)}
          onChange={(v) => onChange(node.id, v)}
        />
      );
    case 'text_input':
      return (
        <TextField
          key={node.id}
          type="text"
          label={String(props.label ?? '')}
          value={String(props.value ?? '')}
          onChange={(v) => onChange(node.id, v)}
        />
      );
    case 'number_input':
      return (
        <TextField
          key={node.id}
          type="number"
          label={String(props.label ?? '')}
          value={String(props.value ?? 0)}
          onChange={(v) => onChange(node.id, parseFloat(v))}
        />
      );
    case 'text_area':
      return (
        <TextArea
          key={node.id}
          label={String(props.label ?? '')}
          value={String(props.value ?? '')}
          onChange={(v) => onChange(node.id, v)}
        />
      );
    case 'selectbox':
      return (
        <Select
          key={node.id}
          label={String(props.label ?? '')}
          value={String(props.value ?? '')}
          options={(props.options as string[] | undefined) ?? []}
          onChange={(v) => onChange(node.id, v)}
        />
      );
    case 'multiselect': {
      const selected = (props.value as string[] | undefined) ?? [];
      return (
        <fieldset key={node.id}>
          <legend>{String(props.label ?? '')}</legend>
          {((props.options as string[] | undefined) ?? []).map((o) => (
            <label key={o}>
              <input
                type="checkbox"
                checked={selected.includes(o)}
                onChange={(e) => {
                  const next = e.target.checked
                    ? [...selected, o]
                    : selected.filter((s) => s !== o);
                  onChange(node.id, next);
                }}
              />
              {o}
            </label>
          ))}
        </fieldset>
      );
    }
    case 'checkbox':
      return (
        <label key={node.id}>
          <input
            type="checkbox"
            checked={Boolean(props.value)}
            onChange={(e) => onChange(node.id, e.target.checked)}
          />
          {String(props.label ?? '')}
        </label>
      );
    case 'radio': {
      const value = String(props.value ?? '');
      return (
        <fieldset key={node.id}>
          <legend>{String(props.label ?? '')}</legend>
          {((props.options as string[] | undefined) ?? []).map((o) => (
            <label key={o}>
              <input
                type="radio"
                name={node.id}
                value={o}
                checked={value === o}
                onChange={() => onChange(node.id, o)}
              />
              {o}
            </label>
          ))}
        </fieldset>
      );
    }
    case 'button':
      return (
        <button key={node.id} type="button" onClick={() => onChange(node.id, true)}>
          {String(props.label ?? '')}
        </button>
      );
    case 'date_input':
      return (
        <TextField
          key={node.id}
          type="date"
          label={String(props.label ?? '')}
          value={String(props.value ?? '')}
          onChange={(v) => onChange(node.id, v)}
        />
      );
    case 'time_input':
      return (
        <TextField
          key={node.id}
          type="time"
          label={String(props.label ?? '')}
          value={String(props.value ?? '')}
          onChange={(v) => onChange(node.id, v)}
        />
      );
    case 'color_picker':
      return (
        <TextField
          key={node.id}
          type="color"
          label={String(props.label ?? '')}
          value={String(props.value ?? '#000000')}
          onChange={(v) => onChange(node.id, v)}
        />
      );
    case 'select_slider':
      return (
        <Select
          key={node.id}
          label={String(props.label ?? '')}
          value={String(props.value ?? '')}
          options={(props.options as string[] | undefined) ?? []}
          onChange={(v) => onChange(node.id, v)}
        />
      );
    case 'file_uploader':
      return labeled(
        node,
        props.label,
        <input
          type="file"
          onChange={(e) => {
            const f = e.target.files?.[0];
            if (f) onChange(node.id, f);
          }}
        />,
      );
    case 'download_button':
      return (
        <a key={node.id} className="download-button" href={String(props.url ?? '')} download>
          {String(props.label ?? '')}
        </a>
      );
    case 'columns':
      return (
        <div key={node.id} className="columns" data-count={Number(props.count ?? 0)}>
          {node.children.map((c) => renderNode(c, onChange))}
        </div>
      );
    case 'column':
      return (
        <div key={node.id} className="column">
          {node.children.map((c) => renderNode(c, onChange))}
        </div>
      );
    case 'container':
      return (
        <div key={node.id} className="container">
          {node.children.map((c) => renderNode(c, onChange))}
        </div>
      );
    case 'expander':
      return (
        <details key={node.id} className="expander">
          <summary>{String(props.label ?? '')}</summary>
          {node.children.map((c) => renderNode(c, onChange))}
        </details>
      );
    case 'tabs':
      return <Tabs key={node.id} node={node} renderChild={(c) => renderNode(c, onChange)} />;
    case 'tab':
      // Rendered by <Tabs> only — orphan tab nodes (defensive) collapse here.
      return (
        <div key={node.id} className="tab" role="tabpanel">
          {node.children.map((c) => renderNode(c, onChange))}
        </div>
      );
    case 'sidebar':
      return (
        <aside key={node.id} className="sidebar">
          {node.children.map((c) => renderNode(c, onChange))}
        </aside>
      );
    case 'empty':
      return <div key={node.id} className="empty" />;
    case 'form':
      return (
        <form key={node.id} onSubmit={(e) => e.preventDefault()}>
          {node.children.map((c) => renderNode(c, onChange))}
        </form>
      );
    case 'form_submit_button':
      return (
        <button key={node.id} type="submit" onClick={() => onChange(node.id, true)}>
          {String(props.label ?? '')}
        </button>
      );
    case 'component': {
      const name = String(props.name ?? '');
      const args = (props.args as Record<string, unknown> | undefined) ?? {};
      const value = props.value;
      const Renderer = findComponent(name);
      if (!Renderer) {
        return (
          <div
            key={node.id}
            className="component component--unregistered"
            data-component-name={name}
          >
            <strong>Unregistered component:</strong> {name}
          </div>
        );
      }
      return (
        <Renderer key={node.id} args={args} value={value} onChange={(v) => onChange(node.id, v)} />
      );
    }
    case 'pages': {
      const pages = (props.pages as Array<{ name: string; path: string }> | undefined) ?? [];
      const current = String(props.current ?? '');
      return (
        <nav key={node.id} className="pages" role="navigation">
          {pages.map((p) => (
            <button
              key={p.path}
              type="button"
              className={current === p.path ? 'pages__current' : ''}
              onClick={() => onChange('__page__', p.path)}
            >
              {p.name}
            </button>
          ))}
        </nav>
      );
    }
    default:
      return (
        <div key={node.id} data-unknown-kind={node.kind}>
          Unknown element: {node.kind}
        </div>
      );
  }
}

function labeled(node: RenderNode, label: unknown, control: JSX.Element): JSX.Element {
  return (
    <label key={node.id}>
      <span>{String(label ?? '')}</span>
      {control}
    </label>
  );
}

function renderTable(node: RenderNode): JSX.Element {
  const rows = (node.props.rows as Array<Record<string, unknown>> | undefined) ?? [];
  if (rows.length === 0) {
    return (
      <table key={node.id} className={node.kind}>
        <tbody>
          <tr>
            <td>(empty)</td>
          </tr>
        </tbody>
      </table>
    );
  }
  const columns = Object.keys(rows[0]);
  return (
    <table key={node.id} className={node.kind}>
      <thead>
        <tr>
          {columns.map((c) => (
            <th key={c}>{c}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.map((row, i) => (
          <tr key={i}>
            {columns.map((c) => (
              <td key={c}>{String(row[c] ?? '')}</td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function renderChart(node: RenderNode): JSX.Element {
  const data = (node.props.data as Array<Record<string, unknown>> | undefined) ?? [];
  const kind = node.kind as
    | 'line_chart'
    | 'bar_chart'
    | 'area_chart'
    | 'scatter_chart';
  return (
    <div key={node.id}>
      <Chart kind={kind} data={data} />
    </div>
  );
}
