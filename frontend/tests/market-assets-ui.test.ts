import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const marketAssetsSource = readFileSync(new URL('../src/market-assets.tsx', import.meta.url), 'utf8');
const operationsSource = readFileSync(new URL('../src/operations-page.tsx', import.meta.url), 'utf8');
const appSource = readFileSync(new URL('../src/App.tsx', import.meta.url), 'utf8');

test('tela de ações usa apenas o catálogo, sem fluxo manual de cadastro', () => {
  assert.doesNotMatch(marketAssetsSource, /Cadastrar ação/);
  assert.doesNotMatch(marketAssetsSource, /acoesApi\.create/);
  assert.doesNotMatch(appSource, /Cadastrar ação/);
  assert.match(marketAssetsSource, /acoesApi\.catalog/);
  assert.match(marketAssetsSource, /setPage\(0\)/);
  assert.match(marketAssetsSource, /Próxima/);
  assert.match(marketAssetsSource, /Cotação ao acompanhar/);
  assert.match(marketAssetsSource, /catalog \/>/);
});

test('seleção para operação continua resolvendo ativo e criando operação pelos fluxos existentes', () => {
  assert.match(operationsSource, /acoesApi\.resolve/);
  assert.match(operationsSource, /acoesApi\.updateQuote/);
  assert.match(operationsSource, /operacoesApi\.create/);
});
