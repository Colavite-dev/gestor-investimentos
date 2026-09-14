import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const page = readFileSync(new URL('../src/broker-page.tsx', import.meta.url), 'utf8');
const styles = readFileSync(new URL('../src/broker-page.css', import.meta.url), 'utf8');
const app = readFileSync(new URL('../src/App.tsx', import.meta.url), 'utf8');
const api = readFileSync(new URL('../src/api/modules.ts', import.meta.url), 'utf8');

test('rota Corretoras usa a página redesenhada e mantém os endpoints existentes', () => {
  assert.match(app, /path="\/corretoras" element=\{<BrokerPage\/>\}/);
  assert.match(page, /corretorasApi\.list\(\)/);
  assert.match(page, /corretorasApi\.create\(\{ cnpj \}\)/);
  assert.match(api, /get<Corretora\[]>\('\/corretoras'\)/);
  assert.match(api, /post<Corretora>\('\/corretoras', body\)/);
});

test('Corretoras exibe header, resumo real, vazio, loading e busca local', () => {
  for (const content of ['Gestão de corretoras', 'Nova corretora', 'Corretoras cadastradas', 'Nenhuma corretora cadastrada', 'Buscar por nome ou CNPJ']) assert.match(page, new RegExp(content));
  assert.match(page, /brokers\.filter\(broker => broker\.validadaNaCvm\)/);
  assert.match(page, /<BrokerLoading\/>/);
  assert.match(page, /useMemo/);
  assert.match(page, /!loading && brokers\.length > 0 && <header className="broker-page-header">/);
  assert.doesNotMatch(page, /mock|faker|Math\.random/);
});

test('CTAs de corretora são mutuamente exclusivos conforme a lista real carregada', () => {
  assert.match(page, /!loading && brokers\.length > 0 && <header className="broker-page-header">/);
  assert.match(page, /brokers\.length === 0 \? <EmptyState title="Nenhuma corretora cadastrada"/);
  assert.match(page, /Cadastrar corretora<\/button>/);
  assert.match(page, /Nova corretora<\/button><\/header>/);
  assert.match(page, /onClick=\{openModal\}/);
});

test('busca sem resultado preserva o contexto de lista existente e o CTA do header', () => {
  assert.match(page, /filteredBrokers\.length \? <BrokerTable/);
  assert.match(page, /: <EmptyState title="Nenhuma corretora encontrada"/);
  assert.doesNotMatch(page, /filteredBrokers\.length === 0 \? <EmptyState title="Nenhuma corretora cadastrada"/);
});

test('modal mantém o CNPJ e comunica uma única validação externa real', () => {
  assert.match(page, /id="broker-cnpj"/);
  assert.match(page, /placeholder="00\.000\.000\/0000-00"/);
  assert.match(page, /Validando dados da corretora…/);
  assert.match(page, /role="status" aria-live="polite"/);
  assert.match(page, /<ErrorAlert message=\{formError\}\/>/);
  assert.match(page, /Corretora cadastrada com sucesso/);
});

test('listagem troca tabela por cards responsivos e usa tokens do design system', () => {
  assert.match(page, /broker-table/);
  assert.match(page, /broker-card-list/);
  assert.match(styles, /@media\(max-width:720px\).*\.broker-table-wrap\{display:none\}/s);
  assert.match(styles, /var\(--color-surface\)/);
  assert.match(styles, /var\(--color-primary\)/);
});
