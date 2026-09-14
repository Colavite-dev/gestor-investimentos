## Why

O frontend já cobre os fluxos reais do Adapt Invest, mas a apresentação visual cresceu de forma incremental: estilos e cores estão distribuídos entre arquivos, componentes básicos não formam ainda um sistema consistente e os layouts de autenticação, navegação, dados financeiros e estados de interface não possuem a mesma hierarquia. Um redesign exclusivamente frontend tornará a demonstração acadêmica mais profissional, responsiva e acessível sem alterar contratos, dados ou regras de negócio.

## What Changes

- Estabelecer uma identidade dark premium do Adapt Invest baseada em tokens reutilizáveis para cores, tipografia, espaçamento, bordas, elevação, motion e paleta de gráficos.
- Redesenhar login e cadastro com composição responsiva em duas áreas no desktop, logo branca existente, decoração financeira abstrata sem dados fictícios, controles de senha acessíveis e formulários compatíveis com os contratos atuais.
- Redesenhar o shell autenticado com sidebar responsiva, marca Adapt Invest, navegação por papel, sessão no rodapé e item ativo claramente identificado.
- Padronizar componentes visuais reutilizáveis para ações, cards, campos, modais, badges, cabeçalhos, métricas e estados de loading, vazio e erro.
- Aplicar a linguagem visual ao Dashboard, Corretoras, Ações, Carteiras, detalhe de Carteira, Operações, Histórico de cotações e Admin, preservando integralmente seus fluxos e dados reais.
- Apresentar no Dashboard as métricas reais de patrimônio atual, valor investido, resultado e rentabilidade, além dos gráficos existentes de custo versus valor atual e distribuição, sem fabricar dados ou mudar seus significados.
- Adicionar animações leves e progressivas apenas onde ajudam a compreensão, com alternativa estática para `prefers-reduced-motion`.
- Adaptar sidebar, tabelas, grids, formulários e modais para desktop, tablet e mobile, mantendo navegação por teclado, foco visível, contraste e mensagens de estado.
- Remover apenas código frontend legado comprovadamente fora das rotas ativas quando isso for necessário para evitar duplicidade durante o redesign; nenhuma regra funcional será reescrita por essa limpeza.

## Capabilities

### New Capabilities

- Nenhuma.

### Modified Capabilities

- `frontend-application`: definir a identidade visual reutilizável, os layouts de autenticação e shell responsivo, a apresentação coerente das páginas financeiras e os requisitos de motion e acessibilidade do frontend.

## Impact

- Afeta exclusivamente `frontend/src`, os assets existentes consumidos pelo frontend e testes frontend focados na apresentação/estrutura.
- Reutiliza React, TypeScript, Vite, React Router, Lucide e Recharts já instalados; não prevê Tailwind, shadcn, troca de stack ou dependência visual obrigatória nova.
- Preserva endpoints, payloads, autenticação JWT, controle USER/ADMIN, providers externos, banco, migrations e regras de negócio.
- O cadastro continuará enviando `nome`, `username`, `email` e `password`, pois esse é o contrato atual; a descrição visual resumida não remove campos obrigatórios.
