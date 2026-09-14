import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const source = readFileSync(new URL('../src/market-assets.tsx', import.meta.url), 'utf8');
const styles = readFileSync(new URL('../src/stock-page.css', import.meta.url), 'utf8');

test('Ações prioriza descoberta remota sem restaurar cadastro manual', () => {
  assert.match(source, /Explorar ativos/);
  assert.match(source, /Pesquise ativos dos mercados brasileiro e americano/);
  assert.match(source, /acoesApi\.catalog/);
  assert.match(source, /query\.trim\(\) \? 350 : 0/);
  assert.match(source, /setPage\(0\)/);
  assert.match(source, /Próxima/);
  assert.doesNotMatch(source, /Cadastrar ação/);
  assert.doesNotMatch(source, /acoesApi\.create/);
});

test('catálogo mantém resolução automática, mercado e logos seguros', () => {
  assert.match(source, /acoesApi\.resolve/);
  assert.match(source, /asset\.mercado !== 'BRASIL'/);
  assert.match(source, /url\.protocol === 'https:'/);
  assert.match(source, /Ícone padrão/);
  assert.match(source, /ESTADOS_UNIDOS/);
  assert.match(source, /MarketBadge/);
});

test('provider error is not presented as an empty catalog', () => {
  assert.match(source, /<\/> : !catalogError \? <EmptyState title="Nenhum ativo encontrado"/);
});

test('ativos acompanhados preservam cotação individual, moedas e histórico', () => {
  assert.match(source, /Ativos acompanhados/);
  assert.match(source, /formatCurrency\(value, currency\)/);
  assert.match(source, /acoesApi\.updateQuote/);
  assert.match(source, /updatingId === asset\.id/);
  assert.match(source, /\/acoes\/\$\{asset\.id\}\/historico/);
  assert.match(source, /catalogError/);
  assert.match(source, /resolutionError/);
  assert.match(source, /localError/);
});

test('layout tem cartões mobile e tabela desktop sem overflow forçado', () => {
  assert.match(styles, /\.stock-table-wrap \{ overflow-x: auto; \}/);
  assert.match(styles, /@media \(max-width: 720px\)/);
  assert.match(styles, /\.stock-mobile-cards \{ display: grid;/);
  assert.match(styles, /prefers-reduced-motion: reduce/);
});
