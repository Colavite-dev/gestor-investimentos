import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const portfolioSource = readFileSync(new URL('../src/portfolio-list-page.tsx', import.meta.url), 'utf8');
const detailSource = readFileSync(new URL('../src/portfolio-pages.tsx', import.meta.url), 'utf8');
const styles = readFileSync(new URL('../src/styles.css', import.meta.url), 'utf8');

test('campo Descrição da nova carteira não permite redimensionamento manual', () => {
  assert.match(portfolioSource, /<textarea className="wallet-description-field" value=\{form\.descricao\}/);
  assert.match(styles, /\.wallet-description-field\{[^}]*width:100%[^}]*max-width:100%[^}]*min-height:120px[^}]*box-sizing:border-box[^}]*resize:none[^}]*\}/);
});

test('carteiras usa CTA único por estado e detalhe preserva moedas e bar chart', () => {
  assert.match(portfolioSource, /hasWallets &&/);
  assert.match(portfolioSource, /Nenhuma carteira cadastrada/);
  assert.match(portfolioSource, /carteirasApi\.list\(\)/);
  assert.match(detailSource, /PremiumValuationComparison/);
  assert.match(detailSource, /<BarChart/);
  assert.match(detailSource, /\['BRL', 'USD'\] as const/);
  assert.match(detailSource, /Distribution positions=\{positions\}/);
});
