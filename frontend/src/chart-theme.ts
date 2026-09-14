export const CHART_COLORS = {
  invested: 'var(--color-chart-invested)',
  current: 'var(--color-chart-current)',
  hover: 'var(--color-chart-hover)',
  grid: 'var(--color-chart-grid)',
  historyLine: 'var(--color-chart-history)',
  historyCursor: 'var(--color-chart-history-cursor)'
} as const;

export const CATEGORICAL_CHART_COLORS = [
  'var(--color-chart-category-blue)',
  'var(--color-chart-category-cyan)',
  'var(--color-chart-category-green)',
  'var(--color-chart-category-violet)',
  'var(--color-chart-category-amber)',
  'var(--color-chart-category-magenta)',
  'var(--color-chart-category-teal)',
  'var(--color-chart-category-coral)'
] as const;

export function categoricalChartColor(index: number) {
  const normalizedIndex = Math.abs(Math.trunc(index)) % CATEGORICAL_CHART_COLORS.length;
  return CATEGORICAL_CHART_COLORS[normalizedIndex];
}
