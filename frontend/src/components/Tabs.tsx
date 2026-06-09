import { useState, type ReactNode } from 'react';
import type { RenderNode } from '../protocol';

interface TabsProps {
  node: RenderNode;
  renderChild: (child: RenderNode) => ReactNode;
}

export function Tabs({ node, renderChild }: TabsProps) {
  const tabs = node.children.filter((c) => c.kind === 'tab');
  const [active, setActive] = useState(0);
  const safeIndex = Math.min(active, Math.max(0, tabs.length - 1));
  const current = tabs[safeIndex];
  return (
    <div className="tabs">
      <div className="tabs__list" role="tablist">
        {tabs.map((t, i) => (
          <button
            key={t.id}
            type="button"
            role="tab"
            id={`${t.id}__tab`}
            aria-selected={safeIndex === i}
            aria-controls={`${t.id}__panel`}
            tabIndex={safeIndex === i ? 0 : -1}
            className="tabs__tab"
            onClick={() => setActive(i)}
          >
            {String(t.props.label ?? '')}
          </button>
        ))}
      </div>
      {current && (
        <div
          className="tabs__panel"
          role="tabpanel"
          id={`${current.id}__panel`}
          aria-labelledby={`${current.id}__tab`}
        >
          {current.children.map((c) => renderChild(c))}
        </div>
      )}
    </div>
  );
}
