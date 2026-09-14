import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const apiSource = readFileSync(new URL('../src/api/modules.ts', import.meta.url), 'utf8');
const pageSource = readFileSync(new URL('../src/portfolio-pages.tsx', import.meta.url), 'utf8');

test('detalhe da carteira oferece refresh explícito e impede clique duplicado', () => {
  assert.match(apiSource, /put<CarteiraQuoteRefresh>\(`\/carteiras\/\$\{id\}\/atualizar-cotacoes`\)/);
  assert.match(pageSource, /Atualizar cotações/);
  assert.match(pageSource, /disabled=\{refreshing\}/);
  assert.match(pageSource, /if \(refreshing\) return/);
});

test('refresh apresenta estados e recarrega apenas resumo e posições', () => {
  assert.match(pageSource, /Cotações atualizadas com sucesso/);
  assert.match(pageSource, /Cotações atualizadas parcialmente/);
  assert.match(pageSource, /Não foi possível atualizar as cotações agora/);
  assert.match(pageSource, /Promise\.all\(\[carteirasApi\.summary\(walletId\), carteirasApi\.positions\(walletId\)\]\)/);
  assert.match(pageSource, /\['BRL', 'USD'\] as const/);
});
