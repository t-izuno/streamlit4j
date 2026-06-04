import { describe, expect, it } from 'vitest';
import { MAX_DEPTH, MAX_PAYLOAD_BYTES, validateIframePayload } from './iframe-payload-validator';

describe('validateIframePayload', () => {
  it('accepts simple primitives', () => {
    expect(validateIframePayload(42)).toEqual({ ok: true });
    expect(validateIframePayload('hello')).toEqual({ ok: true });
    expect(validateIframePayload(true)).toEqual({ ok: true });
    expect(validateIframePayload(null)).toEqual({ ok: true });
  });

  it('accepts nested objects within depth limit', () => {
    let nested: unknown = 'leaf';
    for (let i = 0; i < MAX_DEPTH; i++) {
      nested = { child: nested };
    }
    expect(validateIframePayload(nested)).toEqual({ ok: true });
  });

  it('rejects objects nested beyond depth limit', () => {
    let nested: unknown = 'leaf';
    for (let i = 0; i < MAX_DEPTH + 5; i++) {
      nested = { child: nested };
    }
    expect(validateIframePayload(nested)).toEqual({ ok: false, reason: 'depth' });
  });

  it('rejects payloads exceeding size limit', () => {
    const huge = 'a'.repeat(MAX_PAYLOAD_BYTES + 1);
    expect(validateIframePayload({ huge })).toEqual({ ok: false, reason: 'size' });
  });

  it('rejects forbidden prototype-polluting keys', () => {
    // JSON.parse creates {__proto__} as an own property, matching how a
    // postMessage structured-clone payload would arrive.
    const polluted = JSON.parse('{"__proto__": {"polluted": true}}');
    expect(validateIframePayload(polluted)).toEqual({
      ok: false,
      reason: 'forbidden-key',
    });
    expect(validateIframePayload({ a: { constructor: 'bad' } })).toEqual({
      ok: false,
      reason: 'forbidden-key',
    });
    expect(validateIframePayload([{ prototype: 'no' }])).toEqual({
      ok: false,
      reason: 'forbidden-key',
    });
  });

  it('rejects unserializable values', () => {
    const circular: Record<string, unknown> = {};
    circular.self = circular;
    expect(validateIframePayload(circular)).toEqual({ ok: false, reason: 'unserializable' });
    expect(validateIframePayload(undefined)).toEqual({ ok: false, reason: 'unserializable' });
  });

  it('accepts arrays of plain primitives', () => {
    expect(validateIframePayload([1, 2, 3, 'four'])).toEqual({ ok: true });
  });
});
