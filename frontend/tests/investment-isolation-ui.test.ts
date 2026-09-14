import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const apiSource = readFileSync(new URL('../src/api/modules.ts', import.meta.url), 'utf8');
const dashboardSource = readFileSync(new URL('../src/portfolio-pages.tsx', import.meta.url), 'utf8');
const operationsSource = readFileSync(new URL('../src/operations-page.tsx', import.meta.url), 'utf8');

test('cliente financeiro não envia identidade ou owner no payload', () => {
  assert.doesNotMatch(apiSource, /usuarioId|ownerId|username/);
  assert.match(apiSource, /post<Carteira>\('\/carteiras', body\)/);
  assert.match(apiSource, /post<Operacao>\('\/operacoes', body\)/);
});

test('dashboard e detalhe descartam respostas da identidade anterior', () => {
  assert.match(dashboardSource, /const \{ user \} = useAuth\(\)/);
  assert.match(dashboardSource, /\[user\?\.id\]/);
  assert.match(dashboardSource, /\[selected, user\?\.id\]/);
  assert.match(dashboardSource, /if \(active\)/);
  assert.match(dashboardSource, /return \(\) => \{ active = false; \}/);
});

test('operações reinicia estado e invalida requests entre identidades', () => {
  assert.match(operationsSource, /operationsRequestId/);
  assert.match(operationsSource, /setWallets\(\[\]\); setAssets\(\[\]\); setSelectedWallet\(undefined\); setOperations\(\[\]\)/);
  assert.match(operationsSource, /\[loadOperations, user\?\.id\]/);
  assert.match(operationsSource, /requestId === operationsRequestId\.current/);
});
