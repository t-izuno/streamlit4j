import { afterEach, describe, expect, it } from 'vitest';
import {
  clearComponents,
  findComponent,
  registerComponent,
  registeredNames,
  type CustomComponentRenderer,
} from './component-registry';

const NoopRenderer: CustomComponentRenderer = () => null;

describe('component registry', () => {
  afterEach(() => {
    clearComponents();
  });

  it('returns undefined for unknown names', () => {
    expect(findComponent('missing')).toBeUndefined();
  });

  it('registers and retrieves a renderer by name', () => {
    registerComponent('chart', NoopRenderer);
    expect(findComponent('chart')).toBe(NoopRenderer);
  });

  it('re-registering overwrites the previous renderer', () => {
    const First: CustomComponentRenderer = () => null;
    const Second: CustomComponentRenderer = () => null;
    registerComponent('picker', First);
    registerComponent('picker', Second);
    expect(findComponent('picker')).toBe(Second);
    expect(registeredNames()).toEqual(['picker']);
  });

  it('lists registered names', () => {
    registerComponent('a', NoopRenderer);
    registerComponent('b', NoopRenderer);
    expect(registeredNames().sort()).toEqual(['a', 'b']);
  });

  it('rejects empty names', () => {
    expect(() => registerComponent('', NoopRenderer)).toThrow();
  });

  it('clearComponents removes all entries', () => {
    registerComponent('x', NoopRenderer);
    clearComponents();
    expect(registeredNames()).toEqual([]);
  });
});
