interface SliderProps {
  label: string;
  min: number;
  max: number;
  value: number;
  onChange: (next: number) => void;
}

export function Slider({ label, min, max, value, onChange }: SliderProps) {
  return (
    <label className="slider">
      <span className="slider__label">{label}</span>
      <input
        type="range"
        min={min}
        max={max}
        value={value}
        onChange={(e) => onChange(parseInt(e.target.value, 10))}
      />
      <span className="slider__value">{value}</span>
    </label>
  );
}
