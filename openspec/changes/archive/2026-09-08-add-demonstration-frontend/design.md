## Context

O backend expõe 18 mappings REST para Corretoras, Ações, Histórico, Carteiras e Operações. `CarteiraResumoResponse` fornece valores investidos, patrimônio atual, lucro/prejuízo e quantidade de posições por moeda; `PosicaoResponse` fornece quantidade, preço médio, valores e lucro/prejuízo por ação. Não há CORS explícito, frontend, autenticação ou contrato de usuário.

## Goals / Non-Goals

**Goals:**

- Criar uma experiência desktop-first dark, técnica e legível para demonstrar o fluxo real do sistema.
- Consumir somente a API Spring, mantendo regras, precisão e validações financeiras no backend.
- Cobrir cadastro/consulta de Corretoras e Ações, histórico, Carteiras, Operações, posições, resumo e Dashboard.
- Manter acessibilidade básica, feedback assíncrono e uma fronteira de segredo estritamente server-side.

**Non-Goals:**

- Login, usuário fictício, dados mockados na demonstração, conversão cambial, dividendos, benchmark, rentabilidade histórica, IRPF ou metas.
- Alteração de endpoints, entidades, regras financeiras, providers, migrations ou autenticação.
- Light mode, mobile completo, Redux, Tailwind obrigatório, retry, cache ou chamadas diretas a providers externos.

## Decisions

### Stack e estrutura

Usar React + TypeScript + Vite em `frontend/`, com `react-router-dom` para as rotas. Um `apiClient` baseado em `fetch` evita dependência HTTP desnecessária; módulos `corretorasApi`, `acoesApi`, `carteirasApi` e `operacoesApi` tipam os DTOs reais. `lucide-react` é a opção leve para ícones lineares; `recharts` será usado somente se a primeira implementação confirmar que simplifica os gráficos de histórico e distribuição.

Estrutura inicial:

```text
frontend/src/
  api/ components/ hooks/ layouts/ pages/ types/ utils/ styles/
  App.tsx main.tsx
```

### Identidade visual

Dark mode only, com tokens próprios: `#0D1117` (fundo), `#161B22` (superfície), `#1C2128` (elevada), `#30363D` (borda), `#F0F6FC` (texto), `#8B949E` (secundário), `#2F81F7` (azul primário), `#388BFD` (hover), azul translúcido para seleção, `#3FB950` apenas para lucro/sucesso, `#F85149` para prejuízo/erro e amarelo para warning. Raios 6–10px, sombras mínimas, system font stack e fonte monoespaçada apenas para números/tickers.

O shell terá sidebar sticky escura com Dashboard, Ações, Carteiras, Operações e Corretoras, além de Histórico quando contextual; topbar discreta com breadcrumb, título, status de API e ação contextual. Não haverá avatar, login ou logo de terceiros.

### Rotas e telas

- `/`: Dashboard com seletor de Carteira.
- `/corretoras`: tabela e cadastro em modal/drawer usando somente CNPJ.
- `/acoes`: tabela, cadastro, atualização de cotação e links para histórico.
- `/acoes/:id/historico`: tabela e linha de cotações retornadas pelo backend.
- `/carteiras`: listagem e criação.
- `/carteiras/:id`: resumo, posições e operações da carteira selecionada.
- `/operacoes`: registro e consulta de operações.

O Dashboard exibirá cards derivados apenas de `CarteiraResumoResponse`: valores investidos, patrimônio atual, lucro/prejuízo e quantidade de posições por moeda. A distribuição será cálculo visual `patrimonioAtual` da posição dividido pelo total da mesma moeda. Não será apresentada rentabilidade ou série patrimonial sem dado real.

### Formulários, feedback e formatação

Formulários usarão os DTOs atuais: CNPJ; ticker/mercado; nome/descrição da carteira; carteira, ação, tipo, quantidade, preço unitário e `Instant` da operação. `COMPRA` e `VENDA` serão enviados exatamente como enums. Loading, skeleton, empty state, alerta de erro e toast de sucesso serão componentes compartilhados; não usar `alert()` como UX principal.

`Intl.NumberFormat` formatará BRL/USD e percentuais de apresentação; `Intl.DateTimeFormat` formatará timestamps. O frontend não arredonda nem recalcula preço médio ou saldo, apenas apresenta campos retornados e proporções visuais seguras.

### Integração browser ↔ backend

`VITE_API_BASE_URL` será a única configuração exposta ao navegador e não terá default hardcoded espalhado. O frontend nunca conterá `BRAPI_TOKEN`, `TWELVE_DATA_API_KEY`, `DB_PASSWORD` ou tokens. Erros 400/404/409/422/502/503 serão convertidos em mensagens amigáveis mantendo o status.

Como não existe CORS explícito, o APPLY deverá verificar a execução real. Se Vite e Spring forem origens diferentes, será adicionada apenas a permissão mínima para a origem local documentada; servir o frontend pela mesma origem continua uma alternativa. Nenhuma origem curinga será adotada.

### Estado, responsividade e acessibilidade

Estado local com `useState`/`useEffect` e hooks de consulta; Context somente para Carteira selecionada se a duplicação justificar. Desktop 1366px+ é prioridade, com sidebar colapsável em telas menores. Inputs terão labels, foco azul visível, contraste adequado, botões semânticos e navegação de teclado básica.

## Risks / Trade-offs

- [Resumo possui valores separados por BRL/USD] → Exibir por moeda e não somar moedas sem conversão.
- [Postman/backend não oferecem autenticação] → Não inventar login; proteger somente a fronteira de segredos.
- [CORS pode ser necessário para Vite] → Confirmar em integração local e liberar somente origem conhecida.
- [Gráficos podem sugerir métricas inexistentes] → Usar apenas histórico de cotação e proporções derivadas de posições; não criar patrimônio histórico.
- [API externa ou banco indisponível] → Exibir estados controlados e manter a demonstração dependente do backend real, sem mocks na versão final.

## Migration Plan

Nenhuma migration ou alteração de banco. O frontend será executado separadamente durante o desenvolvimento; a configuração de CORS, se necessária e aprovada no APPLY, será mínima e reversível.

## Open Questions

Nenhuma questão bloqueia a proposta. A necessidade efetiva de CORS e a inclusão final de `recharts` serão verificadas durante a implementação sem mudar os contratos funcionais.
