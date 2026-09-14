## Context

O frontend atual é uma SPA React 19 + TypeScript 6 + Vite 8. React Router 7 controla as rotas, Lucide fornece ícones e Recharts renderiza gráficos. Não há Tailwind, shadcn nem biblioteca de componentes. O CSS está dividido entre `styles.css`, `App.css`, `auth.css`, `session.css`, `distribution.css` e `market-enhancements.css`; há tokens iniciais, porém cores e padrões ainda aparecem diretamente em CSS e configuração de gráficos.

As rotas ativas usam páginas extraídas para autenticação, catálogo, operações, Dashboard/detalhe de carteira e Admin, enquanto Corretoras, Carteiras, Histórico e o shell permanecem em `App.tsx`. O mesmo arquivo também contém versões legadas de Dashboard, detalhe de Carteira e Operações que não são usadas pelas rotas. `components/ui.tsx` já oferece Modal, Badge, Loading, Empty, Error e Toast, mas com contratos e acessibilidade mínimos.

Os assets existentes incluem `adapt-invest-logo.png`, a marca branca horizontal, `adapt-invest-symbol.png`, o símbolo branco, e `hero.png`, uma ilustração abstrata. O cadastro atual exige `nome`, `username`, `email` e `password`; portanto, o redesign deve preservar todos esses campos. O repositório tem testes frontend pequenos baseados em Node para invariantes funcionais, sem framework de renderização de componentes.

Os documentos de arquitetura e decisões ainda descrevem principalmente o backend, e o PRD contém uma seção antiga que declara frontend e autenticação fora de escopo, embora as stable specs e a implementação já os definam como entregues. Esta change segue as stable specs vigentes e não tenta corrigir documentação de produto fora de seu escopo.

## Goals / Non-Goals

**Goals:**

- Consolidar tokens e primitives suficientes para uma linguagem visual coerente sem introduzir framework de CSS.
- Redesenhar somente os componentes e rotas ativas, preservando contratos e estado funcional.
- Tornar autenticação, shell, dados financeiros, integrações e estados de página consistentes em desktop, tablet e mobile.
- Melhorar acessibilidade estrutural e controlar motion com preferência do usuário.
- Permitir validação automatizada das invariantes estruturais e validação visual/manual dos comportamentos responsivos.

**Non-Goals:**

- Alterar backend, endpoints, DTOs, autenticação, autorização, providers, banco ou migrations.
- Inventar séries financeiras, métricas ou conteúdo para preencher espaços.
- Adicionar Tailwind, shadcn, nova biblioteca de gráficos, biblioteca de animação ou pacote de design system.
- Redesenhar a marca ou criar nova logo.
- Reescrever regras funcionais existentes durante a reorganização visual.
- Corrigir PRD, documentos acadêmicos ou outros débitos documentais nesta change.

## Decisions

### 1. CSS nativo com tokens semânticos em camadas

Uma camada global definirá tokens CSS para superfícies (`background`, `surface`, `surface-raised`), texto, bordas, azul primário, estados, paleta de gráficos, espaçamento, radius, shadow, duração e easing. Componentes consumirão nomes semânticos, não a cor concreta. Tokens permanecerão no CSS global e os arquivos poderão ser reorganizados por fundação, componentes e páginas sem introduzir CSS-in-JS.

Alternativas consideradas: Tailwind/shadcn aumentariam dependências e exigiriam migração sem benefício proporcional; manter arquivos incrementais sem fundação perpetuaria duplicidade. CSS Modules exigiria migração ampla dos seletores atuais e não é necessário para cumprir o escopo.

### 2. Primitives incrementais, não uma biblioteca abstrata completa

`components/ui.tsx` será evoluído e, quando a separação melhorar legibilidade, poderá ser dividido em componentes como `Button`, `Card`, `InputField`, `Modal`, `Badge`, `PageHeader`, `MetricCard`, `LoadingState`/`Skeleton`, `EmptyState` e `ErrorState`. Variantes serão limitadas às combinações usadas pelas rotas ativas. Links que parecem botões usarão estilos compartilhados sem esconder semântica de navegação.

O CTA primário terá borda/shimmer discreto via pseudo-elementos e transições CSS. O efeito será reservado a Entrar, Criar conta e ações principais como Nova operação. Botões secundários, ícones e paginação não herdarão o brilho.

Alternativa considerada: criar uma biblioteca genérica com dezenas de variantes aumentaria abstração e testes sem necessidade observável.

### 3. Autenticação usa composição compartilhada e assets existentes

Login e cadastro compartilharão um `AuthLayout` dividido no desktop. A área de marca usará `adapt-invest-logo.png`; o símbolo poderá aparecer em composições compactas. Linha de crescimento, barras e glow serão CSS/SVG decorativos com `aria-hidden="true"`, sem números ou afirmação de dados reais. O formulário manterá campos atuais e terá controle de visibilidade por campo com Lucide e `aria-pressed`/rótulo dinâmico.

Em tablet/mobile, a área de marca será compactada para cabeçalho visual e o formulário ocupará a largura confortável disponível. Não será apenas uma redução proporcional das duas colunas.

Alternativas consideradas: usar `hero.png` como peça central poderia disputar atenção com a marca; ele só será reutilizado se a composição se mantiver discreta. Criar nova arte ou logo está fora do escopo.

### 4. Shell desktop fixo e navegação mobile temporária

No desktop, a sidebar terá largura estável, logo horizontal, item ativo azul e região inferior com identidade do usuário, status essencial e logout. A versão recolhida usará o símbolo existente e tooltips/rótulos acessíveis. Em telas pequenas, a sidebar se tornará um drawer com backdrop, botão de abertura e fechamento por botão, Escape, seleção de rota ou clique no backdrop. O conteúdo principal não ficará permanentemente comprimido.

A lista de navegação será derivada de uma única configuração e continuará filtrando Admin pela role. Nenhuma autorização será delegada à aparência do menu; a guarda existente permanece a proteção funcional.

Alternativa considerada: bottom navigation não comporta confortavelmente todas as áreas e a região de sessão/admin.

### 5. Páginas são reorganizadas sem alterar suas fontes de dados

- Dashboard: cards por moeda ou agrupamento já suportado destacarão Patrimônio atual, Valor investido, Resultado e Rentabilidade. Os gráficos existentes de investido versus atual e distribuição serão tematizados por tokens Recharts e manterão dados da API.
- Corretoras: listagem e modal destacarão fases locais de envio/validação já observáveis; não será inferida uma etapa que a API não informa. Mensagens de integração existentes serão apresentadas em states coerentes.
- Ações: catálogo e ativos persistidos manterão abas, busca, mercado, paginação, logo BR/fallback e ações de histórico/atualização, com cards e toolbar responsivos; o cadastro manual continuará ausente.
- Carteiras/detalhe: cards, resumo, posições, comparação de valuation e operações recentes ganharão hierarquia comum sem recomputar valores.
- Operações: tabela e modal manterão o state machine de cotação/preço atual, separando visualmente cotação, preço editável e total.
- Histórico: gráfico e tabela usarão tokens, tooltip legível e estado vazio; cotação atual não será tratada como ponto histórico novo.
- Admin: métricas e usuários usarão primitives compartilhadas, preservando guarda e campos não secretos.

### 6. Responsividade é definida por comportamento, não por dispositivos específicos

Serão usados breakpoints baseados em pressão de layout: desktop com sidebar e grids amplos, tablet com grids reduzidos e navegação adaptada, mobile com uma coluna, toolbar empilhada, tabelas roláveis e modais com altura máxima/scroll interno. Larguras, gaps e tipografia poderão usar `clamp()` e containers, evitando valores por modelo de aparelho.

Tabelas financeiras manterão cabeçalhos e rolagem horizontal com indicação visual; somente onde não houver perda semântica poderão virar linhas empilhadas. Modais usarão largura máxima, `max-height` relativa ao viewport e safe-area padding.

### 7. Motion é CSS, breve e opt-out

Entradas usarão fade/translate curto, modais usarão transição breve, cards interativos terão deslocamento mínimo, o CTA terá shimmer limitado ao hover/foco e a decoração de autenticação poderá desenhar uma linha uma vez. Nenhuma grande superfície terá animação contínua. Uma regra global `prefers-reduced-motion: reduce` removerá animation/transition não essencial e scroll suave.

Alternativa considerada: biblioteca de animação não é necessária para esses efeitos e aumentaria bundle e superfície de manutenção.

### 8. Acessibilidade é parte dos componentes compartilhados

Contraste será conferido nos tokens; foco não será removido; ícones decorativos serão ocultos da árvore acessível; botões somente com ícone terão nome; feedback assíncrono relevante usará `aria-live` com parcimônia. O Modal deverá ganhar `role="dialog"`, `aria-modal`, associação com título, foco inicial previsível, contenção de foco e retorno ao acionador, além de Escape. O menu mobile seguirá padrão equivalente de foco e fechamento.

### 9. Limpeza de legado é restrita e verificável

Antes de remover código, as rotas e imports serão usados para provar quais versões estão ativas. As implementações legadas de Dashboard, detalhe de Carteira e Operações em `App.tsx` poderão ser removidas após confirmação de que as páginas extraídas são as únicas referenciadas. Corretoras, Carteiras, Histórico e Layout serão preservados ou extraídos sem mudança funcional. `@ts-nocheck` só será removido se a reorganização deixar o arquivo tipável sem ampliar o escopo; não é objetivo independente.

### 10. Validação combina checks existentes e revisão visual dirigida

Os testes Node existentes serão preservados. Serão adicionados testes de baixo custo para invariantes testáveis sem DOM, como presença do contrato de cadastro, ausência de fluxo manual de ação e helpers/configurações puras de navegação/variantes, quando extraídos. Não será instalada infraestrutura de testes de componentes apenas para o redesign. Build TypeScript, lint e testes existentes serão obrigatórios; a validação manual cobrirá desktop/tablet/mobile, teclado, motion reduzido, rotas/roles e estados de dados.

## Risks / Trade-offs

- [PNG branco pode perder detalhes em tamanhos muito pequenos] → usar a marca completa apenas acima de tamanho mínimo e o símbolo isolado em modo compacto, preservando proporção.
- [Muitos seletores globais podem causar regressões entre páginas] → migrar por primitives/rotas, manter tokens centralizados e validar todas as rotas ativas a cada etapa relevante.
- [Remover legado confundido com código ativo pode quebrar fluxos] → rastrear rotas/imports e remover somente símbolos sem referência após build e testes.
- [Recharts ainda requer valores de cor em propriedades] → resolver as cores a partir dos tokens compartilhados em um helper/constantes do tema, evitando paletas duplicadas.
- [Animação e glow podem reduzir legibilidade ou desempenho] → limitar duração/área, evitar loops grandes e fornecer redução total por media query.
- [Sem framework de teste de UI, responsividade e foco não ficam totalmente automatizados] → manter checks estruturais de baixo custo e exigir roteiro manual explícito antes de concluir APPLY.
- [PRD e arquitetura estão desatualizados sobre a existência do frontend/auth] → registrar como dívida documental fora desta change; stable specs e código atual orientam o redesign.

## Migration Plan

1. Consolidar tokens e primitives sem mudar páginas.
2. Redesenhar autenticação e shell, validando contratos e guards.
3. Migrar páginas ativas por grupos, mantendo chamadas e state machines existentes.
4. Aplicar responsividade, acessibilidade e reduced motion transversalmente.
5. Remover apenas legado comprovado e imports/estilos mortos.
6. Executar testes frontend existentes e adicionados, build, lint, validação OpenSpec e roteiro visual/manual.

Rollback é apenas de arquivos frontend: como não há backend, schema ou persistência envolvidos, cada grupo visual pode ser revertido sem migração de dados.
