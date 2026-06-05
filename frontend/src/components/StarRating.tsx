import { useMemo } from 'react';
import type { CustomComponentRenderProps } from '../component-registry';

const DEFAULT_MAX = 5;

function clampMax(raw: unknown): number {
  const max = typeof raw === 'number' && Number.isFinite(raw) ? Math.floor(raw) : DEFAULT_MAX;
  if (max < 1) return 1;
  if (max > 10) return 10;
  return max;
}

function clampValue(raw: unknown, max: number): number {
  const value = typeof raw === 'number' && Number.isFinite(raw) ? Math.floor(raw) : 0;
  if (value < 0) return 0;
  if (value > max) return max;
  return value;
}

export function StarRating({ args, value, onChange }: CustomComponentRenderProps) {
  const label = typeof args.label === 'string' ? args.label : 'Rating';
  const max = clampMax(args.max);
  const current = clampValue(value, max);
  const stars = useMemo(() => Array.from({ length: max }, (_, i) => i + 1), [max]);

  return (
    <fieldset className="component component--star-rating" data-value={current}>
      <legend>{label}</legend>
      {stars.map((n) => (
        <button
          key={n}
          type="button"
          aria-label={`${n} star${n === 1 ? '' : 's'}`}
          aria-pressed={n <= current}
          className={n <= current ? 'star star--filled' : 'star'}
          onClick={() => onChange(n === current ? 0 : n)}
        >
          {n <= current ? '★' : '☆'}
        </button>
      ))}
      <span className="component--star-rating__value">{current}</span>
    </fieldset>
  );
}
