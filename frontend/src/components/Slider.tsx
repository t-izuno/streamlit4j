import { useEffect, useState } from 'react';

interface SliderProps {
  label: string;
  min: number;
  max: number;
  value: number;
  onChange: (next: number) => void;
}

export function Slider({ label, min, max, value, onChange }: SliderProps) {
  // Local state so dragging is visually instant. Server-pushed value updates
  // merge back via the effect below.
  const [local, setLocal] = useState(value);
  useEffect(() => {
    setLocal(value);
  }, [value]);

  return (
    <label className="slider">
      <span className="slider__label">{label}</span>
      <div className="slider__row">
        <input
          type="range"
          min={min}
          max={max}
          value={local}
          onChange={(e) => {
            const next = parseInt(e.target.value, 10);
            setLocal(next);
            onChange(next);
          }}
        />
        <span className="slider__value">{local}</span>
      </div>
    </label>
  );
}
