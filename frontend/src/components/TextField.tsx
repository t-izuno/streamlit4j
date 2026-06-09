import { useEffect, useState, type ChangeEvent } from 'react';

interface TextFieldProps {
  type: 'text' | 'number' | 'password' | 'email' | 'date' | 'time' | 'color';
  label?: string;
  value: string;
  onChange: (next: string) => void;
}

/**
 * Text-like input with local state so typing is visually instant even before
 * the server-pushed value echoes back. Used for text / number / password /
 * date / time / color inputs.
 */
export function TextField({ type, label, value, onChange }: TextFieldProps) {
  const [local, setLocal] = useState(value);
  useEffect(() => {
    setLocal(value);
  }, [value]);

  const handle = (e: ChangeEvent<HTMLInputElement>) => {
    setLocal(e.target.value);
    onChange(e.target.value);
  };

  return (
    <label>
      {label !== undefined && <span>{label}</span>}
      <input type={type} value={local} onChange={handle} />
    </label>
  );
}

interface TextAreaProps {
  label?: string;
  value: string;
  onChange: (next: string) => void;
}

export function TextArea({ label, value, onChange }: TextAreaProps) {
  const [local, setLocal] = useState(value);
  useEffect(() => {
    setLocal(value);
  }, [value]);

  return (
    <label>
      {label !== undefined && <span>{label}</span>}
      <textarea
        value={local}
        onChange={(e) => {
          setLocal(e.target.value);
          onChange(e.target.value);
        }}
      />
    </label>
  );
}

interface SelectProps {
  label?: string;
  value: string;
  options: string[];
  onChange: (next: string) => void;
}

export function Select({ label, value, options, onChange }: SelectProps) {
  const [local, setLocal] = useState(value);
  useEffect(() => {
    setLocal(value);
  }, [value]);

  return (
    <label>
      {label !== undefined && <span>{label}</span>}
      <select
        value={local}
        onChange={(e) => {
          setLocal(e.target.value);
          onChange(e.target.value);
        }}
      >
        {options.map((o) => (
          <option key={o} value={o}>
            {o}
          </option>
        ))}
      </select>
    </label>
  );
}
