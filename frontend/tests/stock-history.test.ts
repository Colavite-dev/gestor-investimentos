import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { calculatePriceDomain, formatHistoryAxisDate, formatHistoryTooltipDate, formatPriceAxis, hasSameMinuteCollision, sortHistoryChronologically } from '../src/stock-history-model.ts';
import type { CotacaoHistorica } from '../src/types/api.ts';

const pageSource = readFileSync(new URL('../src/stock-history-page.tsx', import.meta.url), 'utf8');
const chartSource = readFileSync(new URL('../src/stock-history-chart.tsx', import.meta.url), 'utf8');
const styles = readFileSync(new URL('../src/stock-history.css', import.meta.url), 'utf8');
const appSource = readFileSync(new URL('../src/App.tsx', import.meta.url), 'utf8');
const apiSource = readFileSync(new URL('../src/api/modules.ts', import.meta.url), 'utf8');

function quote(id: number, cotacao: number, timestamp: string): CotacaoHistorica {
  return { id, cotacao, dataHoraCotacao: timestamp, dataRegistro: timestamp };
}

test('histórico real é ordenado cronologicamente sem alterar a entrada', () => {
  const input = [quote(3, 53.4, '2026-09-10T12:00:00Z'), quote(1, 52.8, '2026-09-08T12:00:00Z'), quote(2, 53.24, '2026-09-09T12:00:00Z')];
  const ordered = sortHistoryChronologically(input);
  assert.deepEqual(ordered.map(item => item.id), [1, 2, 3]);
  assert.deepEqual(input.map(item => item.id), [3, 1, 2]);
  assert.equal(ordered.at(-1)?.cotacao, 53.4);
});

test('domínio oferece margem segura para valores idênticos e ponto único', () => {
  const equalDomain = calculatePriceDomain([
    quote(1, 53.24, '2026-09-08T12:00:00Z'),
    quote(2, 53.24, '2026-09-09T12:00:00Z'),
  ]);
  assert.ok(equalDomain[0] < 53.24);
  assert.ok(equalDomain[1] > 53.24);

  const onePoint = [quote(1, 228.4, '2026-09-08T12:00:00Z')];
  const onePointDomain = calculatePriceDomain(onePoint);
  assert.equal(onePoint.length, 1);
  assert.ok(onePointDomain[0] < 228.4 && onePointDomain[1] > 228.4);
});

test('eixo preserva BRL e USD sem conversão', () => {
  assert.match(formatPriceAxis(53.24, 'BRL'), /R\$/);
  assert.match(formatPriceAxis(228.4, 'USD'), /US\$/);
});

test('gráfico usa AreaChart, moeda real, tooltip dark e dados persistidos', () => {
  assert.match(chartSource, /<AreaChart data=\{history\}/);
  assert.match(chartSource, /linearGradient/);
  assert.match(chartSource, /HistoryTooltip/);
  assert.match(chartSource, /formatCurrency\(item\.cotacao, currency\)/);
  assert.match(chartSource, /formatHistoryTooltipDate\(item\.dataHoraCotacao\)/);
  assert.match(chartSource, /hasSameMinuteCollision\(history\)/);
  assert.match(chartSource, /calculatePriceDomain\(history\)/);
  assert.doesNotMatch(chartSource + pageSource, /Math\.random|setInterval|Average|Peak|Data Points|Live/);
  assert.doesNotMatch(chartSource, /formatPercent|percentage|percentual/i);
  assert.match(styles, /background:\s*color-mix\(/);
});

test('página preserva último valor, tabela, vazio, loading, rota e endpoint', () => {
  assert.match(pageSource, /const latest = history\.at\(-1\)/);
  assert.match(pageSource, /DATA\/HORA|Data\/hora/i);
  assert.match(pageSource, /Nenhum histórico disponível/);
  assert.match(pageSource, /HistorySkeleton/);
  assert.match(pageSource, /\[\.\.\.history\]\.reverse\(\)\.map/);
  assert.match(appSource, /path="\/acoes\/:id\/historico" element=\{<StockHistoryPage\/>\}/);
  assert.match(apiSource, /`\/acoes\/\$\{id\}\/historico-cotacoes`/);
  assert.match(styles, /@media \(max-width: 430px\)/);
});

test('precisiona o eixo e tooltip com base nos timestamps reais', () => {
  const apart = [quote(1, 53.2, '2026-09-08T22:55:02Z'), quote(2, 53.3, '2026-09-08T22:56:21Z')];
  assert.equal(hasSameMinuteCollision(apart), false);
  assert.match(formatHistoryAxisDate(apart[0].dataHoraCotacao, true), /:55/);

  const collision = [quote(1, 53.2, '2026-09-08T22:55:02Z'), quote(2, 53.2, '2026-09-08T22:55:21Z')];
  assert.equal(hasSameMinuteCollision(collision), true);
  const precise = formatHistoryAxisDate(collision[0].dataHoraCotacao, true, true);
  assert.match(precise, /:55:02/);
  assert.match(formatHistoryTooltipDate(collision[0].dataHoraCotacao), /2026/);
  assert.match(formatHistoryTooltipDate(collision[0].dataHoraCotacao), /:55:02/);
});
