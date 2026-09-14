## 1. BATCH 1 — Fundação visual e técnica
- [x] 1.1 Scaffold Vite React TypeScript em `frontend/`.
- [x] 1.2 Tokens CSS próprios, dark mode e responsividade desktop-first.
- [x] 1.3 Layout, sidebar, topbar e rotas principais.
- [x] 1.4 `apiClient`, tipos compartilhados e módulos de API com `VITE_API_BASE_URL`.

## 2. BATCH 2 — Corretoras e Ações
- [x] 2.1 Listagem/cadastro de corretoras com estados de UI.
- [x] 2.2 Listagem/cadastro de ações e atualização de cotação.
- [x] 2.3 Histórico em tabela e gráfico com dados da API.

## 3. BATCH 3 — Carteiras e Operações
- [x] 3.1 Listagem/criação e detalhe de carteiras.
- [x] 3.2 Posições e resumo separados por moeda.
- [x] 3.3 Listagem/cadastro de operações com enums reais.
- [x] 3.4 Erros de venda e regras de negócio apresentados pelo backend.

## 4. BATCH 4 — Dashboard
- [x] 4.1 Dashboard com seletor e cards por moeda.
- [x] 4.2 Distribuição visual por posição, separada por moeda, com percentuais calculados sobre `patrimonioAtual`.
- [x] 4.3 Recharts usado para histórico com dados reais.

## 5. BATCH 5 — UX, acessibilidade e segurança
- [x] 5.1 Loading, empty, erro, toast e modal compartilhados.
- [x] 5.2 Formatação Intl para moedas, números e datas.
- [x] 5.3 Foco, labels, semântica e sidebar responsiva.
- [x] 5.4 Sem segredos nem chamadas diretas a providers.

## 6. BATCH 6 — Integração e entrega
- [x] 6.1 Validação ponta a ponta concluída no navegador: corretora, ações BR/EUA, carteira, compra, posição, atualização, histórico, venda válida e rejeição de venda inválida.
- [x] 6.2 CORS global restrito a `http://localhost:5173`, com preflight e origem não autorizada testados.
- [x] 6.3 Build TypeScript/Vite executado com sucesso; lint não existe no scaffold.
- [x] 6.4 Integração real sem mocks validada com backend Spring em `http://localhost:8080` e frontend Vite em `http://localhost:5173`.
- [x] 6.5 Instruções básicas adicionadas ao README.
- [x] 6.6 Regressão backend concluída com `mvn verify` (BUILD SUCCESS), OpenSpec strict e revisão final de arquivos.
