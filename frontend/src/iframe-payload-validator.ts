/**
 * Boundary validator for values crossing the parent ↔ iframe component boundary.
 *
 * <p>The iframe is untrusted (TASK-101 / TASK-102), so any value it sends must
 * be checked before the parent forwards it as a widget event. This validator
 * enforces three hardening rules in addition to the same-source / same-origin /
 * matching-name checks already performed by {@code IframeComponent}:
 *
 * <ol>
 *   <li>Serialized JSON size is bounded to {@link MAX_PAYLOAD_BYTES}</li>
 *   <li>Object nesting depth is bounded to {@link MAX_DEPTH}</li>
 *   <li>Prototype-polluting keys ({@code __proto__}, {@code constructor},
 *       {@code prototype}) are rejected anywhere in the structure</li>
 * </ol>
 *
 * <p>Outbound (parent → iframe) values are also validated so a buggy app cannot
 * exfiltrate huge state into a third-party iframe.
 */

export const MAX_PAYLOAD_BYTES = 256 * 1024;
export const MAX_DEPTH = 32;
const FORBIDDEN_KEYS = new Set(['__proto__', 'constructor', 'prototype']);

export type ValidationResult =
  | { ok: true }
  | { ok: false; reason: 'size' | 'depth' | 'forbidden-key' | 'unserializable' };

export function validateIframePayload(value: unknown): ValidationResult {
  let serialized: string;
  try {
    serialized = JSON.stringify(value);
  } catch {
    return { ok: false, reason: 'unserializable' };
  }
  if (serialized === undefined) {
    return { ok: false, reason: 'unserializable' };
  }
  const byteSize = new TextEncoder().encode(serialized).length;
  if (byteSize > MAX_PAYLOAD_BYTES) {
    return { ok: false, reason: 'size' };
  }
  return walk(value, 0);
}

function walk(value: unknown, depth: number): ValidationResult {
  if (depth > MAX_DEPTH) {
    return { ok: false, reason: 'depth' };
  }
  if (value === null || typeof value !== 'object') {
    return { ok: true };
  }
  if (Array.isArray(value)) {
    for (const item of value) {
      const result = walk(item, depth + 1);
      if (!result.ok) return result;
    }
    return { ok: true };
  }
  for (const key of Object.keys(value)) {
    if (FORBIDDEN_KEYS.has(key)) {
      return { ok: false, reason: 'forbidden-key' };
    }
    const result = walk((value as Record<string, unknown>)[key], depth + 1);
    if (!result.ok) return result;
  }
  return { ok: true };
}
