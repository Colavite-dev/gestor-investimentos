import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { applyOperationQuote, beginOperationPricing, calculateOperationTotal, editOperationPrice, failOperationQuote } from '../src/operation-pricing.ts';

const operationPageSource = readFileSync(new URL('../src/operations-page.tsx', import.meta.url), 'utf8');

test('cotacao valida preenche o preco e nao mantem erro de cotacao', () => {
  const selected = beginOperationPricing(3, 1, 53.24, true);
  const updated = applyOperationQuote(selected, 3, 1, 53.24);
  assert.equal(updated.marketQuote, 53.24); assert.equal(updated.unitPrice, '53.24'); assert.equal(updated.error, ''); assert.equal(updated.notice, '');
});

test('edicao manual e total nao sao sobrescritos por resposta tardia', () => {
  const selected = beginOperationPricing(3, 7, 53.24, true);
  const edited = editOperationPrice(selected, '50');
  const lateResponse = applyOperationQuote(edited, 3, 7, 53.24);
  assert.equal(lateResponse.marketQuote, 53.24); assert.equal(lateResponse.unitPrice, '50'); assert.equal(calculateOperationTotal('10', lateResponse.unitPrice), 500);
});

test('falha obsoleta nao descarta cotacao nem sugestao validas', () => {
  const selected = beginOperationPricing(3, 8, 53.24, true); const updated = applyOperationQuote(selected, 3, 8, 53.24); const unrelatedOldFailure = failOperationQuote(updated, 2, 7, 'Falha de pesquisa');
  assert.deepEqual(unrelatedOldFailure, updated); assert.equal(unrelatedOldFailure.unitPrice, '53.24'); assert.equal(unrelatedOldFailure.error, '');
});

test('falha real sem cotacao preserva entrada manual editavel', () => {
  const selected = beginOperationPricing(3, 9, undefined, true); const failed = failOperationQuote(selected, 3, 9, 'Fonte indisponivel'); const manuallyPriced = editOperationPrice(failed, '50');
  assert.match(failed.error, /Informe um preço manualmente/); assert.equal(manuallyPriced.unitPrice, '50'); assert.equal(calculateOperationTotal('10', manuallyPriced.unitPrice), 500);
});

test('controles de operação e combobox expõem semântica e teclado acessíveis', () => {
  assert.match(operationPageSource, /aria-pressed=\{form\.tipo === 'COMPRA'\}/);
  assert.match(operationPageSource, /aria-pressed=\{form\.tipo === 'VENDA'\}/);
  assert.match(operationPageSource, /role="combobox"/);
  assert.match(operationPageSource, /aria-controls=\{listboxId\}/);
  assert.match(operationPageSource, /aria-activedescendant=\{open && activeIndex >= 0/);
  for (const key of ['ArrowDown', 'ArrowUp', 'Enter', 'Escape']) assert.match(operationPageSource, new RegExp(`event\\.key === '${key}'`));
});
