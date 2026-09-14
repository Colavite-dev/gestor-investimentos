import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { CATEGORICAL_CHART_COLORS, categoricalChartColor } from '../src/chart-theme.ts';

const dashboardSource = readFileSync(new URL('../src/portfolio-pages.tsx', import.meta.url), 'utf8');
const chartStyles = readFileSync(new URL('../src/dashboard-charts.css', import.meta.url), 'utf8');
const distributionStyles = readFileSync(new URL('../src/distribution.css', import.meta.url), 'utf8');

test('dashboard preserva gráfico de barras e separação entre BRL e USD', () => {
  assert.match(dashboardSource, /function DashboardValuationComparison/);
  assert.match(dashboardSource, /<BarChart/);
  assert.doesNotMatch(dashboardSource, /<LineChart|<AreaChart/);
  assert.match(dashboardSource, /\['BRL', 'USD'\] as const/);
  assert.match(dashboardSource, /dataKey="investido" name="Valor investido"/);
  assert.match(dashboardSource, /dataKey="atual" name="Valor atual"/);
  assert.match(dashboardSource, /valorInvestido/);
  assert.match(dashboardSource, /patrimonioAtual/);
});

test('barras são estreitas, espaçadas e usam cores semânticas', () => {
  assert.match(dashboardSource, /barCategoryGap="58%" barGap=\{8\}/);
  assert.equal((dashboardSource.match(/barSize=\{18\} maxBarSize=\{20\} radius=\{\[7, 7, 2, 2\]\}/g) ?? []).length, 2);
  assert.match(dashboardSource, /CHART_COLORS\.invested/);
  assert.match(dashboardSource, /CHART_COLORS\.current/);
});

test('tooltip e cursor do dashboard permanecem dark', () => {
  assert.match(dashboardSource, /content=\{<ValuationTooltip\/>\}/);
  assert.match(dashboardSource, /cursor=\{\{ fill: CHART_COLORS\.hover \}\}/);
  assert.doesNotMatch(chartStyles, /background:\s*(white|#fff(?:fff)?)/i);
  assert.match(chartStyles, /background:\s*color-mix\(/);
});

test('paleta categórica dos donuts é determinística e compartilhada por fatia e legenda', () => {
  assert.equal(CATEGORICAL_CHART_COLORS.length, 8);
  assert.equal(categoricalChartColor(0), categoricalChartColor(8));
  assert.notEqual(categoricalChartColor(0), categoricalChartColor(1));
  assert.equal((dashboardSource.match(/categoricalChartColor\(index\)/g) ?? []).length, 2);
  assert.match(dashboardSource, /content=\{<DistributionTooltip currency=\{currency\}\/>\}/);
});

test('donuts mantem segmentos reais, anel espesso e espacamento somente com multiplas categorias', () => {
  assert.match(dashboardSource, /const paddingAngle = data\.length > 1 \? 5 : 0/);
  assert.match(dashboardSource, /innerRadius=\{48\} outerRadius=\{80\} paddingAngle=\{paddingAngle\} cornerRadius=\{9\}/);
  assert.match(dashboardSource, /data\.map\(\(_, index\) => <Cell/);
  assert.match(dashboardSource, /Distribui..o pelo valor atual em \$\{currency\} com \$\{data\.length\}/);
  assert.match(dashboardSource, /Participa..o/);
});

test('legenda e layout do donut permanecem agrupados e responsivos', () => {
  assert.match(distributionStyles, /grid-template-columns:minmax\(168px,200px\) minmax\(160px,1fr\)/);
  assert.match(chartStyles, /\.distribution-body \{ grid-template-columns: 1fr; align-items: stretch; \}/);
  assert.match(distributionStyles, /\.legend-row\{display:grid;grid-template-columns:minmax\(0,1fr\) auto/);
});
