## ADDED Requirements

### Requirement: Identidade visual consistente e reutilizável

O frontend SHALL apresentar todas as rotas ativas com uma identidade dark premium própria do Adapt Invest, usando fundo preto ou azul-marinho muito escuro, superfícies escuras, azul elétrico como cor primária, bordas discretas, tipografia clara e uma paleta semântica consistente para estados e gráficos. Cores, espaçamentos, tipografia, raios, bordas, elevação e motion SHALL ser aplicados por tokens reutilizáveis em vez de valores arbitrários repetidos. A logo branca existente do Adapt Invest SHALL ser preservada e usada em formato adequado ao contexto.

#### Scenario: Usuário percorre páginas diferentes

- **WHEN** o usuário navega entre autenticação, Dashboard e páginas de gestão
- **THEN** marca, superfícies, tipografia, espaçamentos, controles e estados mantêm a mesma hierarquia visual

#### Scenario: Informação financeira possui semântica

- **WHEN** a interface apresenta valores positivos, negativos, neutros ou séries de gráfico
- **THEN** usa tokens semânticos consistentes, com rótulos ou contexto que não dependem apenas de cor

### Requirement: Autenticação profissional preserva o contrato atual

As rotas `/login` e `/cadastro` SHALL usar um layout responsivo próprio, com identidade Adapt Invest, logo branca existente, mensagem curta e decoração financeira abstrata distinta dos dados reais, além de formulário legível e centralizado. Login SHALL manter `username` e `password`; cadastro SHALL manter `nome`, `username`, `email` e `password`. Ambos SHALL oferecer mostrar ou ocultar senha, CTA principal, link para o fluxo alternativo e estados acessíveis de envio e erro, sem mudar autenticação, endpoints ou payloads.

#### Scenario: Usuário abre autenticação no desktop

- **WHEN** `/login` ou `/cadastro` é exibida em viewport desktop
- **THEN** a identidade e a decoração ocupam uma área distinta do formulário sem representar dados financeiros reais

#### Scenario: Usuário alterna a visibilidade da senha

- **WHEN** o usuário ativa o controle de mostrar ou ocultar senha
- **THEN** o valor permanece no mesmo campo, o estado do controle é anunciado de forma acessível e o contrato enviado não muda

#### Scenario: Usuário cadastra uma conta

- **WHEN** o usuário preenche o cadastro redesenhado
- **THEN** o frontend continua solicitando e enviando exatamente nome, username, email e password exigidos pela API atual

### Requirement: Shell autenticado responsivo e orientado por papel

O shell autenticado SHALL apresentar a logo Adapt Invest, navegação para Dashboard, Corretoras, Ações, Carteiras e Operações, acesso Admin somente para `ADMIN`, indicação clara do item ativo e região inferior com usuário e ações de sessão. Em telas menores, a navegação SHALL possuir comportamento compacto acionável sem cobrir permanentemente o conteúdo, preservar foco e permitir fechamento previsível.

#### Scenario: Usuário navega pelo desktop

- **WHEN** o usuário autenticado seleciona uma entrada da sidebar
- **THEN** a rota ativa é destacada e a página correspondente é exibida sem recarregamento completo

#### Scenario: Usuário abre a navegação no mobile

- **WHEN** o usuário ativa o menu em viewport pequena
- **THEN** a navegação torna-se utilizável como painel temporário, pode ser fechada por controle explícito, teclado ou seleção de rota e não deixa o conteúdo inacessível

#### Scenario: USER visualiza a navegação

- **WHEN** o shell é renderizado para uma identidade `USER`
- **THEN** nenhuma entrada administrativa é apresentada

### Requirement: Componentes e estados seguem padrões compartilhados

Botões, cards, campos, modais, badges, cabeçalhos de página, cards de métrica e estados de loading, vazio e erro SHALL seguir padrões visuais e comportamentais reutilizáveis. O botão primário SHALL possuir ênfase e microinteração discreta apenas em CTAs principais; controles secundários SHALL permanecer visualmente mais contidos. Componentes interativos SHALL expor estados hover, active, disabled e `focus-visible`, e modais SHALL permanecer contidos no viewport e operáveis por teclado.

#### Scenario: CTA principal recebe interação

- **WHEN** um CTA principal como Entrar, Criar conta ou Nova operação recebe hover, foco ou ativação
- **THEN** a interface fornece feedback coerente e mantém foco visível sem prejudicar a legibilidade

#### Scenario: Página aguarda dados reais

- **WHEN** uma página está carregando, não possui dados ou recebe erro controlado
- **THEN** apresenta respectivamente loading ou skeleton, empty state ou error state consistente sem inserir dados fictícios

#### Scenario: Modal é aberto em tela pequena

- **WHEN** um formulário modal é aberto em viewport mobile
- **THEN** seu conteúdo cabe ou pode rolar dentro do viewport e os controles essenciais permanecem alcançáveis

### Requirement: Páginas financeiras mantêm significado e ganham hierarquia visual

Dashboard, Corretoras, Ações, Carteiras, detalhe de Carteira, Operações, Histórico de cotações e Admin SHALL adotar a identidade compartilhada sem alterar dados, permissões ou regras de negócio. O Dashboard SHALL priorizar os dados reais disponíveis de patrimônio atual, valor investido, resultado e rentabilidade e manter gráficos que distinguem custo, valor atual e distribuição. Catálogo de ações, integrações de corretoras, sugestão editável de preço da operação, tabelas administrativas e demais fluxos atuais SHALL continuar funcionalmente equivalentes.

#### Scenario: Dashboard possui dados de carteira

- **WHEN** o backend retorna resumo e posições de uma carteira
- **THEN** a interface destaca patrimônio atual, valor investido, resultado e rentabilidade e desenha gráficos somente a partir desses dados reais

#### Scenario: Página não possui dado suficiente para um gráfico

- **WHEN** a API não fornece dados reais suficientes para determinada visualização
- **THEN** o frontend apresenta estado vazio ou omite a visualização em vez de fabricar valores

#### Scenario: Usuário abre nova operação

- **WHEN** o modal redesenhado de operação é exibido
- **THEN** seleção do ativo, cotação atual, tipo, quantidade, preço unitário editável, proteção contra resposta tardia e total preservam o comportamento vigente

#### Scenario: Usuário consulta ações

- **WHEN** o usuário pesquisa ou pagina o catálogo de ações
- **THEN** busca, paginação, logo BR quando válida, fallback visual e resolução sob demanda permanecem disponíveis sem fluxo manual de cadastro na tela

### Requirement: Responsividade, acessibilidade e movimento reduzido

As rotas ativas SHALL permanecer utilizáveis em desktop, tablet e mobile. Layouts em duas áreas SHALL se reorganizar em vez de apenas comprimir colunas; grids SHALL reduzir colunas; tabelas SHALL manter leitura por rolagem ou apresentação adaptada; e formulários SHALL manter alvos e espaçamento confortáveis. A interface SHALL preservar labels, contraste, navegação por teclado, foco visível, atributos ARIA quando necessários e SHALL desativar ou reduzir animações não essenciais quando `prefers-reduced-motion: reduce` estiver ativo.

#### Scenario: Autenticação é aberta no mobile

- **WHEN** login ou cadastro é exibido em viewport pequena
- **THEN** a área decorativa é reduzida ou reorganizada e o formulário permanece confortável, legível e sem rolagem horizontal indevida

#### Scenario: Usuário solicita movimento reduzido

- **WHEN** o sistema operacional sinaliza `prefers-reduced-motion: reduce`
- **THEN** shimmer, entrada, desenho decorativo, transição de modal e movimentos de hover não executam animações não essenciais

#### Scenario: Usuário navega por teclado

- **WHEN** o usuário percorre links, botões, campos, menu e modal sem mouse
- **THEN** a ordem é previsível, o foco permanece visível e todas as ações essenciais são operáveis
