import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const shellSource = readFileSync(new URL('../src/components/app-shell.tsx', import.meta.url), 'utf8');
const shellStyles = readFileSync(new URL('../src/components/app-shell.css', import.meta.url), 'utf8');
const appSource = readFileSync(new URL('../src/App.tsx', import.meta.url), 'utf8');

test('shell mantém os itens atuais e condiciona Admin à role', () => {
  for (const label of ['Dashboard', 'Corretoras', 'Ações', 'Carteiras', 'Operações', 'Admin']) assert.match(shellSource, new RegExp(`label: '${label}'`));
  assert.match(shellSource, /adminOnly: true/);
  assert.match(shellSource, /!item\.adminOnly \|\| role === 'ADMIN'/);
});

test('navegação ativa, sessão e logout pertencem à sidebar', () => {
  assert.match(shellSource, /isActive \? 'is-active'/);
  assert.match(shellSource, /user\?\.username/);
  assert.match(shellSource, /user\?\.role/);
  assert.match(shellSource, /onClick=\{logout\}/);
  assert.match(shellSource, /Adapt Invest — Dashboard/);
});

test('drawer mobile abre e fecha de modo acessível', () => {
  assert.match(shellSource, /aria-expanded=\{drawerOpen\}/);
  assert.match(shellSource, /aria-controls="primary-sidebar"/);
  assert.match(shellSource, /event\.key === 'Escape'/);
  assert.match(shellSource, /setDrawerOpen\(false\)/);
  assert.match(shellStyles, /max-width:760px/);
  assert.match(shellStyles, /translateX\(-102%\)/);
});

test('drawer mobile mantém o foco e o devolve ao acionador ao fechar', () => {
  assert.match(shellSource, /event\.key !== 'Tab'/);
  assert.match(shellSource, /firstFocusable\.focus\(\)/);
  assert.match(shellSource, /lastFocusable\.focus\(\)/);
  assert.match(shellSource, /menuButtonRef\.current\?\.focus\(\)/);
});

test('rotas existentes continuam dentro do shell autenticado', () => {
  assert.match(appSource, /<AuthenticatedShell><Routes>/);
  for (const path of ['/', '/corretoras', '/acoes', '/carteiras', '/operacoes', '/admin']) assert.match(appSource, new RegExp(`path="${path.replace('/', '\\/')}"`));
});

test('sidebar renderiza um único asset de marca conforme o estado', () => {
  const brandMarkup = shellSource.slice(shellSource.indexOf('<NavLink className="sidebar-brand"'), shellSource.indexOf('</NavLink>', shellSource.indexOf('<NavLink className="sidebar-brand"')));
  assert.equal((brandMarkup.match(/<img/g) ?? []).length, 1);
  assert.match(brandMarkup, /compact && !drawerOpen \? adaptInvestSymbol : adaptInvestLogo/);
  assert.match(brandMarkup, /compact && !drawerOpen \? 'sidebar-logo-symbol' : 'sidebar-logo-full'/);
});
