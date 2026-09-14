## Why

O backend já possui um fluxo completo de corretoras, ações, carteiras, operações, posições e histórico, mas ainda não há uma interface visual para demonstrar esse percurso ponta a ponta. Esta change planeja um frontend próprio para apresentação acadêmica, consumindo a API Spring real e mantendo as regras financeiras no backend.

## What Changes

- Criar uma aplicação web em `frontend/` com React, TypeScript e Vite, em dark mode e identidade azul própria do Gestor de Investimentos.
- Implementar shell visual desktop-first com sidebar, topbar, rotas e estados de loading, vazio, erro e sucesso.
- Expor visualmente os 18 mappings existentes, com telas de Corretoras, Ações, Histórico de Cotações, Carteiras, detalhe da carteira, Operações e Dashboard.
- Usar dados reais da API Spring para cards de resumo, posições, distribuição derivada e histórico de cotação, sem mocks na versão demonstrável.
- Centralizar o cliente HTTP, tipos DTO, formatação BRL/USD/percentuais e tratamento dos status de erro existentes.
- Avaliar configuração mínima de CORS para a origem local do Vite somente se necessária à execução browser ↔ backend; nunca liberar `*` sem justificativa.

## Capabilities

### New Capabilities

- `frontend-application`: interface web demonstrável, suas rotas, layout, componentes e visualizações baseadas nos dados reais do backend.
- `frontend-backend-integration`: contrato de consumo browser ↔ API Spring, configuração de base URL, erros, CORS mínimo e segurança de credenciais.

### Modified Capabilities

Nenhuma. As regras financeiras e os endpoints existentes não serão alterados.

## Impact

Prevê novo diretório `frontend/` e, se a execução local exigir, uma configuração mínima de CORS no backend. Poderá adicionar somente dependências frontend justificadas (`react-router-dom`, `lucide-react`, `recharts`), sem banco, migration, integração externa ou segredo no navegador.
