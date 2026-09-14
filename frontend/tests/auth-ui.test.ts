import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const authPagesSource = readFileSync(new URL('../src/auth-pages.tsx', import.meta.url), 'utf8');
const authUiSource = readFileSync(new URL('../src/components/auth-ui.tsx', import.meta.url), 'utf8');
const authStyles = readFileSync(new URL('../src/auth.css', import.meta.url), 'utf8');
const designSystemStyles = readFileSync(new URL('../src/design-system.css', import.meta.url), 'utf8');

test('login preserva credenciais e estados do fluxo existente', () => {
  assert.match(authPagesSource, /await login\(username, password\)/);
  assert.match(authPagesSource, /autoComplete="username"/);
  assert.match(authPagesSource, /autoComplete="current-password"/);
  assert.match(authPagesSource, /loading=\{sending\}/);
  assert.match(authPagesSource, /<AuthMessage>\{error\}<\/AuthMessage>/);
  assert.match(authPagesSource, /to="\/cadastro"/);
});

test('cadastro preserva exatamente os quatro campos públicos obrigatórios', () => {
  assert.match(authPagesSource, /nome: '', username: '', email: '', password: ''/);
  assert.match(authPagesSource, /await register\(form\.nome, form\.username, form\.email, form\.password\)/);
  assert.doesNotMatch(authPagesSource, /role/);
  assert.match(authPagesSource, /autoComplete="name"/);
  assert.match(authPagesSource, /autoComplete="email"/);
  assert.match(authPagesSource, /autoComplete="new-password"/);
  assert.match(authPagesSource, /to="\/login"/);
});

test('campo de senha possui toggle acessível sem alterar seu valor', () => {
  assert.match(authUiSource, /type=\{visible \? 'text' : 'password'\}/);
  assert.match(authUiSource, /aria-label=\{visible \? 'Ocultar senha' : 'Mostrar senha'\}/);
  assert.match(authUiSource, /aria-pressed=\{visible\}/);
});

test('layout usa os assets Adapt Invest e decoração sem dados', () => {
  assert.match(authUiSource, /adapt-invest-logo\.png/);
  assert.match(authUiSource, /adapt-invest-symbol\.png/);
  assert.match(authUiSource, /className="auth-market-art" aria-hidden="true"/);
  assert.doesNotMatch(authUiSource, /hero\.png/);
});

test('tokens, breakpoints e movimento reduzido estão definidos', () => {
  assert.match(designSystemStyles, /--color-primary:/);
  assert.match(designSystemStyles, /--color-surface:/);
  assert.match(designSystemStyles, /--motion-normal:/);
  assert.match(designSystemStyles, /prefers-reduced-motion: reduce/);
  assert.match(authStyles, /max-width:1100px/);
  assert.match(authStyles, /max-width:820px/);
  assert.match(authStyles, /max-width:560px/);
});

test('painel usa o novo texto e dimensionamento dinâmico sem bloquear scroll móvel', () => {
  assert.match(authUiSource, /Invista com visão\. Gerencie com controle\./);
  assert.match(authUiSource, /Centralize seus investimentos, acompanhe sua carteira e tome decisões com base em dados reais\./);
  assert.match(authStyles, /height:100dvh/);
  assert.match(authStyles, /max-height:720px/);
  assert.match(authStyles, /max-width:820px\)\{\.auth-shell\{height:auto/);
  assert.doesNotMatch(authStyles, /body\{[^}]*overflow:hidden/);
});
