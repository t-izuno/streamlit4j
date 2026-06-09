import {
  Area,
  AreaChart as RAreaChart,
  Bar,
  BarChart as RBarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart as RLineChart,
  ResponsiveContainer,
  Scatter,
  ScatterChart as RScatterChart,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

type Row = Record<string, unknown>;

interface ChartProps {
  kind: 'line_chart' | 'bar_chart' | 'area_chart' | 'scatter_chart';
  data: Row[];
}

const SERIES_COLORS = ['#4f46e5', '#ef4444', '#10b981', '#f59e0b', '#0ea5e9', '#a855f7'];

function classifyColumns(data: Row[]): {
  numericKeys: string[];
  stringKeys: string[];
} {
  if (data.length === 0) return { numericKeys: [], stringKeys: [] };
  const sample = data[0];
  const numericKeys: string[] = [];
  const stringKeys: string[] = [];
  for (const k of Object.keys(sample)) {
    const v = sample[k];
    if (typeof v === 'number') numericKeys.push(k);
    else if (typeof v === 'string') stringKeys.push(k);
  }
  return { numericKeys, stringKeys };
}

interface PreparedChart {
  rows: Row[];
  xKey: string;
  series: string[];
}

function prepare(data: Row[]): PreparedChart | null {
  if (data.length === 0) return null;
  const { numericKeys, stringKeys } = classifyColumns(data);

  // Case 1: <category-column> + 2 numerics → pivot category into series, plot first numeric as X.
  if (stringKeys.length === 1 && numericKeys.length >= 2) {
    const xKey = numericKeys[0];
    const yKey = numericKeys[1];
    const categoryKey = stringKeys[0];
    const xValues = [...new Set(data.map((r) => r[xKey] as number))].sort((a, b) => a - b);
    const categories = [...new Set(data.map((r) => r[categoryKey] as string))];
    const rows = xValues.map((x) => {
      const row: Row = { [xKey]: x };
      for (const cat of categories) {
        const match = data.find((r) => r[xKey] === x && r[categoryKey] === cat);
        row[cat] = match ? match[yKey] : null;
      }
      return row;
    });
    return { rows, xKey, series: categories };
  }

  // Case 2: 2+ numerics, no category → first numeric = X, rest = series.
  if (numericKeys.length >= 2) {
    return { rows: data, xKey: numericKeys[0], series: numericKeys.slice(1) };
  }

  // Case 3: single numeric → synthesize index column for X.
  if (numericKeys.length === 1) {
    const rows = data.map((r, i) => ({ ...r, index: i }));
    return { rows, xKey: 'index', series: numericKeys };
  }

  return null;
}

export function Chart({ kind, data }: ChartProps) {
  const prepared = prepare(data);
  if (!prepared) {
    return (
      <figure className={kind}>
        <figcaption>
          {kind} — no numeric columns to plot
        </figcaption>
      </figure>
    );
  }
  const { rows, xKey, series } = prepared;
  return (
    <figure className={kind} style={{ width: '100%', height: 280, margin: 0 }}>
      <ResponsiveContainer width="100%" height="100%">
        {renderInner(kind, rows, xKey, series)}
      </ResponsiveContainer>
    </figure>
  );
}

function renderInner(
  kind: ChartProps['kind'],
  rows: Row[],
  xKey: string,
  series: string[],
): React.ReactElement {
  switch (kind) {
    case 'bar_chart':
      return (
        <RBarChart data={rows}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
          <XAxis dataKey={xKey} />
          <YAxis />
          <Tooltip />
          <Legend />
          {series.map((s, i) => (
            <Bar key={s} dataKey={s} fill={SERIES_COLORS[i % SERIES_COLORS.length]} />
          ))}
        </RBarChart>
      );
    case 'area_chart':
      return (
        <RAreaChart data={rows}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
          <XAxis dataKey={xKey} />
          <YAxis />
          <Tooltip />
          <Legend />
          {series.map((s, i) => (
            <Area
              key={s}
              type="monotone"
              dataKey={s}
              stroke={SERIES_COLORS[i % SERIES_COLORS.length]}
              fill={SERIES_COLORS[i % SERIES_COLORS.length]}
              fillOpacity={0.2}
            />
          ))}
        </RAreaChart>
      );
    case 'scatter_chart':
      return (
        <RScatterChart>
          <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
          <XAxis type="number" dataKey={xKey} />
          <YAxis />
          <Tooltip cursor={{ strokeDasharray: '3 3' }} />
          <Legend />
          {series.map((s, i) => (
            <Scatter
              key={s}
              name={s}
              data={rows.map((r) => ({ [xKey]: r[xKey], [s]: r[s] }))}
              dataKey={s}
              fill={SERIES_COLORS[i % SERIES_COLORS.length]}
            />
          ))}
        </RScatterChart>
      );
    case 'line_chart':
    default:
      return (
        <RLineChart data={rows}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
          <XAxis dataKey={xKey} />
          <YAxis />
          <Tooltip />
          <Legend />
          {series.map((s, i) => (
            <Line
              key={s}
              type="monotone"
              dataKey={s}
              stroke={SERIES_COLORS[i % SERIES_COLORS.length]}
              dot={false}
            />
          ))}
        </RLineChart>
      );
  }
}
