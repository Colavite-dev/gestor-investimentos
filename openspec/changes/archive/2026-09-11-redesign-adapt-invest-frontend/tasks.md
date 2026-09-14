## 1. Fundação visual

- [x] 1.1 Inventariar seletores e componentes efetivamente consumidos pelas rotas ativas, registrar o mapa de migração visual e verificar por busca de imports/rotas que nenhum fluxo ativo foi classificado como legado.
- [x] 1.2 Consolidar tokens CSS semânticos de superfícies, texto, azul primário, bordas, estados, gráficos, espaçamento, radius, shadow e motion, e verificar por busca estática que as novas primitives não repetem valores de tema desnecessariamente.
- [x] 1.3 Evoluir os componentes compartilhados Button, Card, Input, Modal, Badge, PageHeader, MetricCard, Loading/Skeleton, EmptyState e ErrorState somente nas variantes usadas, verificando estados hover, active, disabled e focus-visible.
- [x] 1.4 Tornar Modal e feedbacks compartilhados acessíveis, verificando nome/título associado, `role="dialog"`, `aria-modal`, Escape, contenção/retorno de foco e anúncios assíncronos pertinentes.

## 2. Autenticação e shell

- [x] 2.1 Implementar o layout compartilhado de autenticação com a logo branca existente, decoração financeira abstrata e composição desktop responsiva, verificando que elementos decorativos são ocultos de tecnologias assistivas e não exibem dados fictícios.
- [x] 2.2 Redesenhar login com controle acessível de mostrar/ocultar senha, CTA primário e estados de envio/erro, verificando que o request continua contendo somente username e password.
- [x] 2.3 Redesenhar cadastro com controle acessível de mostrar/ocultar senha, CTA e estados de envio/erro, verificando que nome, username, email e password continuam presentes e que role não é introduzida.
- [x] 2.4 Redesenhar o shell desktop com logo, item ativo, status/sessão no rodapé e navegação derivada de configuração única, verificando que Admin aparece somente para identidade ADMIN.
- [x] 2.5 Implementar sidebar/drawer mobile com backdrop e fechamento por botão, Escape, rota e interação externa, verificando navegação por teclado, retorno de foco e ausência de bloqueio permanente do conteúdo.

## 3. Páginas financeiras

- [x] 3.1 Redesenhar Dashboard e detalhe de Carteira com MetricCards reais para patrimônio, valor investido, resultado e rentabilidade, verificando que nenhum valor é fabricado e que estados loading/empty/error permanecem corretos.
- [x] 3.2 Tematizar gráficos de valuation, distribuição e histórico usando a paleta compartilhada, verificando tooltips/legendas legíveis e que custo, valor atual e histórico mantêm seus significados atuais.
- [x] 3.3 Redesenhar Corretoras e seu modal, verificando que CNPJ, validações externas, CVM, endereço e mensagens de loading/sucesso/erro preservam o fluxo vigente.
- [x] 3.4 Redesenhar Ações e Histórico de cotações, verificando catálogo BR/US, busca, paginação, logo BR/fallback, fallback US, resolução sob demanda, atualização e ausência de “Cadastrar ação”.
- [x] 3.5 Redesenhar Carteiras, cards e listagens, verificando criação, navegação ao detalhe e estados sem dados sem alterar payloads.
- [x] 3.6 Redesenhar Operações e o modal de nova operação, verificando seleção/resolução, cotação atual, sugestão editável, proteção contra resposta tardia, tipo, quantidade, moeda, total e submissão existentes.
- [x] 3.7 Redesenhar Admin, verificando métricas/lista reais, ausência de dados secretos e preservação das guardas USER/ADMIN e do comportamento 403.

## 4. Responsividade, motion e limpeza

- [x] 4.1 Aplicar layouts de desktop, tablet e mobile a grids, toolbars, tabelas, formulários e modais, verificando as rotas ativas nos três tamanhos sem rolagem horizontal indevida fora de tabelas intencionalmente roláveis.
- [x] 4.2 Implementar microinterações e shimmer discreto somente nos CTAs principais, além de entrada e transição breve onde previstas, verificando que não há animação longa ou contínua em grandes áreas.
- [x] 4.3 Implementar `prefers-reduced-motion: reduce`, verificando no modo reduzido que shimmer, desenho decorativo, transições e deslocamentos não essenciais ficam desativados.
- [x] 4.4 Remover somente componentes, imports, handlers, estados e estilos comprovadamente legados após a migração, verificando por referências, TypeScript e lint que nenhum símbolo ativo foi removido.

## 5. Testes e validação final

- [x] 5.1 Preservar os testes funcionais frontend existentes e adicionar checks de baixo custo para contratos de autenticação, navegação por papel, assets e invariantes visuais extraídas, verificando que a suíte frontend passa sem instalar framework novo desnecessário.
- [x] 5.2 Executar roteiro visual e de acessibilidade em login, cadastro, Dashboard, Corretoras, Ações, Carteiras, detalhe, Operações, Histórico e Admin nos layouts desktop/tablet/mobile, incluindo teclado, foco, estados assíncronos e movimento reduzido, e registrar o resultado.
- [x] 5.3 Executar `npm run build`, `npm run lint` e os scripts de teste frontend existentes/adicionados, corrigindo somente falhas causadas pela change e registrando os resultados.
- [x] 5.4 Executar `openspec validate redesign-adapt-invest-frontend --strict` e `git diff --check`, comparar a implementação com todos os cenários aprovados e confirmar que backend, migrations, banco e contratos não foram alterados.
