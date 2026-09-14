# frontend-application Specification

## Purpose

Oferecer uma interface web própria, legível e demonstrável para acompanhar visualmente os fluxos reais de investimentos do backend.

## Requirements

### Requirement: Histórico de cotações preserva registros e precisão temporal

O frontend SHALL apresentar todos os registros retornados por `GET /acoes/{id}/historico-cotacoes` em ordem cronológica, sem deduplicação ou filtragem visual. Para datas diferentes, os rótulos do eixo SHALL usar `DD/MM`; no mesmo dia, SHALL usar `HH:mm` quando não houver colisão no mesmo minuto e `HH:mm:ss` quando dois ou mais registros compartilharem o minuto. O tooltip SHALL exibir data e hora completas com segundos e a cotação na moeda real do ativo.

#### Scenario: Datas diferentes

- **WHEN** o histórico contém registros em dias distintos
- **THEN** o eixo apresenta rótulos compactos de dia e mês e mantém todos os registros

#### Scenario: Mesmo dia sem colisão

- **WHEN** os registros estão no mesmo dia e não compartilham hora e minuto
- **THEN** o eixo apresenta `HH:mm`

#### Scenario: Colisão no mesmo minuto

- **WHEN** dois ou mais registros compartilham o mesmo dia, hora e minuto
- **THEN** o eixo apresenta `HH:mm:ss` para distingui-los

#### Scenario: Tooltip detalhado

- **WHEN** o usuário inspeciona um ponto do histórico
- **THEN** o tooltip apresenta timestamp até segundos e o valor real formatado na moeda do ativo

#### Scenario: Registros iguais não são alterados

- **WHEN** vários registros retornados possuem a mesma cotação
- **THEN** todos permanecem visíveis com seus valores reais, sem dados sintéticos

### Requirement: Fluxos de acesso e rotas protegidas

O frontend SHALL oferecer `/login` e `/cadastro` fora do shell autenticado, manter o tema dark e a identidade visual Adapt Invest, e proteger Dashboard e todas as rotas de investimento contra acesso sem identidade válida. Ao recuperar uma identidade válida, o frontend SHALL renderizar o shell existente; ao receber `401`, SHALL limpar a sessão local e redirecionar para `/login` de forma controlada.

#### Scenario: Visitante acessa dashboard

- **WHEN** um visitante sem sessão válida abre uma rota protegida, inclusive `/`
- **THEN** o frontend o redireciona para `/login` sem renderizar dados de investimento

#### Scenario: Usuário efetua login

- **WHEN** um usuário conclui login válido
- **THEN** o frontend recupera a identidade autenticada e o redireciona para o dashboard ou rota protegida originalmente solicitada

### Requirement: Logout e acesso administrativo condicional

O shell autenticado SHALL oferecer logout que remova o estado de autenticação local e retorne o usuário à tela de login. A rota e a navegação `/admin` SHALL aparecer e ser acessíveis somente quando a identidade atual tiver papel `ADMIN`; um `USER` que tente abrir `/admin` SHALL receber uma tela de acesso negado ou redirecionamento controlado sem carregar dados administrativos.

#### Scenario: Usuário encerra sessão

- **WHEN** um usuário autenticado seleciona logout
- **THEN** o frontend remove o token e a identidade local e redireciona para `/login`

#### Scenario: USER tenta abrir área administrativa

- **WHEN** uma identidade `USER` navega diretamente para `/admin`
- **THEN** o frontend não solicita dados administrativos e apresenta acesso negado ou redirecionamento controlado

#### Scenario: ADMIN abre área administrativa

- **WHEN** uma identidade `ADMIN` abre `/admin`
- **THEN** o frontend apresenta a listagem não secreta de usuários e métricas simples retornadas pela API

### Requirement: Demonstration shell and navigation

The frontend SHALL provide a dark-only responsive application shell with a blue primary identity, compact sidebar, contextual topbar and navigation for Dashboard, Ações, Carteiras, Operações, Corretoras and Histórico.

#### Scenario: User navigates the application

- **WHEN** the user selects an item in the sidebar
- **THEN** the corresponding page is rendered without a full browser reload and the active item is visually identified

### Requirement: Real investment views

The frontend SHALL present Corretoras, Ações, Histórico de Cotações, Carteiras, Operações, Posições and Resumo using data returned by the backend API, without replacing demonstrable production flows with fabricated dashboard data.

#### Scenario: User selects a portfolio

- **WHEN** the user changes the selected Carteira in the dashboard
- **THEN** the displayed resumo, posições and operations are loaded for that carteira

#### Scenario: User opens quote history

- **WHEN** the user requests history for an existing Acao
- **THEN** the UI displays the returned timestamp and quote values in a table and may render a line chart from those same values

### Requirement: Financial presentation

The frontend SHALL format monetary values and dates according to their returned currency and timestamp, distinguish positive results with success styling and negative results with danger styling, and SHALL NOT reimplement or override backend financial rules.

#### Scenario: User views a position

- **WHEN** a position contains values from the API
- **THEN** quantity, average price, invested value, current value and profit/loss are shown using the returned values and presentation-only calculations

### Requirement: Accessible feedback states

The frontend SHALL provide visible loading, empty, success and controlled error states for asynchronous pages and forms, with labels, keyboard-focus indication and semantic controls.

#### Scenario: Provider-backed form is submitted

- **WHEN** the user submits a Corretora or Acao form
- **THEN** the UI shows progress, prevents duplicate submission and presents the API result or a recoverable error state

### Requirement: Seleção remota de ação para nova operação
No formulário de nova operação, o frontend SHALL permitir pesquisar ações remotas por ticker, nome da empresa ou mercado e SHALL consultar somente a API Spring. A pesquisa SHALL iniciar a partir de dois caracteres após debounce entre 300 e 500 ms, indicar carregamento, nenhum resultado e indisponibilidade de forma compreensível, e MUST NOT usar dados mockados nem chamar providers externos pelo browser.

#### Scenario: Usuário pesquisa uma ação ainda não cadastrada
- **WHEN** o usuário informa termo de pesquisa válido no campo Ação
- **THEN** o frontend apresenta as sugestões retornadas pela API com ticker, mercado, nome quando disponível e moeda sem cadastrar ativo automaticamente

#### Scenario: Nenhuma sugestão encontrada
- **WHEN** a API retorna lista vazia para a pesquisa
- **THEN** o frontend informa que não há ativo encontrado e não permite usar o texto livre como ação da operação

### Requirement: Seleção resolve ação antes do lançamento
O frontend SHALL enviar ticker e mercado da sugestão escolhida ao endpoint de resolução antes de habilitar o lançamento. Após sucesso, SHALL guardar o ID da `AcaoResponse` devolvida, mostrar a seleção válida e usar exclusivamente esse ID no `OperacaoRequest`. A moeda exibida para o preço unitário e o total SHALL derivar da ação resolvida, sem conversão cambial.

#### Scenario: Seleção de sugestão nova
- **WHEN** o usuário seleciona sugestão de ação ainda não cadastrada
- **THEN** o frontend aguarda a resolução, recebe ação persistida com ID e permite registrar a operação com esse ID

#### Scenario: Falha ao resolver sugestão
- **WHEN** a resolução devolve erro controlado da API
- **THEN** o frontend mostra mensagem amigável, preserva o campo sem ação válida e não envia operação

### Requirement: Autocomplete preserva interação acessível
O autocomplete SHALL manter clique, clique fora, Escape, setas e Enter para navegar ou selecionar sugestões. O campo MUST fechar a lista após seleção, clique fora ou Escape, MUST ignorar ou cancelar resposta assíncrona obsoleta quando o termo mudar e MUST impedir submissão enquanto não houver ação resolvida válida. Navegar, destacar ou abrir sugestões MUST NOT iniciar resolução nem persistência.

#### Scenario: Seleção por teclado
- **WHEN** o usuário percorre uma lista aberta com setas e confirma com Enter
- **THEN** o frontend seleciona a sugestão destacada e inicia sua resolução

### Requirement: Tela de Ações apresenta catálogo externo

A rota autenticada `/acoes` SHALL permitir distinguir ativos persistidos e catálogo de mercado, filtrar por Brasil ou Estados Unidos, pesquisar e navegar progressivamente. Cada item SHALL exibir ticker, empresa, mercado, moeda, exchange quando disponível e cotação quando fornecida sem enriquecimento excessivo. Ações BR SHALL exibir a logo HTTPS da brapi quando válida e fallback quando ausente ou quebrada; ações US SHALL exibir sempre o ícone padrão local. A interface MUST NOT oferecer fluxo manual de “Cadastrar ação”.

#### Scenario: Logo ausente ou quebrada
- **WHEN** uma ação BR não possui logo utilizável ou o carregamento da imagem falha
- **THEN** a interface mostra um fallback estável com identificação textual do ativo

#### Scenario: Ação US exibida
- **WHEN** uma ação US é apresentada
- **THEN** a interface usa o ícone padrão local e não solicita logo externo

#### Scenario: Provider indisponível
- **WHEN** o catálogo externo falha
- **THEN** a tela apresenta erro recuperável e preserva o acesso à lista local sem criar ativos

#### Scenario: Usuário navega pelo catálogo
- **WHEN** o usuário pesquisa, muda de página ou visualiza ativos no catálogo
- **THEN** a interface não cria nem cadastra ativos automaticamente

### Requirement: Formulário de operação apresenta cotação e preço editável

O modal SHALL mostrar o estado da cotação após a seleção, o rótulo “Cotação atual do mercado”, o preço unitário editável e o total calculado com o preço digitado. A moeda SHALL corresponder ao ativo selecionado. Erros de pesquisa, resolução e cotação SHALL ser independentes.

#### Scenario: Cotação obtida
- **WHEN** a cotação atual é carregada
- **THEN** ela é exibida como referência e preenche o preço unitário sem bloquear edição posterior

#### Scenario: Resposta tardia
- **WHEN** o usuário edita manualmente o preço antes de uma resposta assíncrona de cotação
- **THEN** a resposta não sobrescreve o preço manual nem o total derivado dele

#### Scenario: Cotação indisponível
- **WHEN** não é possível obter a cotação de uma ação selecionável
- **THEN** a interface informa a indisponibilidade específica da cotação e permite preço manual válido

### Requirement: Identidade visual consistente e reutilizável
O frontend SHALL apresentar todas as rotas ativas com identidade visual Adapt Invest consistente, usando tokens reutilizáveis para cores, espaçamentos, tipografia, raios, bordas, elevação e movimento, preservando a logo existente.

#### Scenario: Usuário percorre páginas diferentes
- **WHEN** o usuário navega entre autenticação, Dashboard e páginas de gestão
- **THEN** marca, superfícies, tipografia, espaçamentos, controles e estados mantêm a mesma hierarquia visual

### Requirement: Autenticação profissional preserva o contrato atual
As rotas `/login` e `/cadastro` SHALL manter campos, payloads, endpoints e autenticação atuais, com layout responsivo, senha alternável, CTA, links e estados acessíveis de envio e erro.

#### Scenario: Usuário cadastra uma conta
- **WHEN** o usuário preenche o cadastro
- **THEN** o frontend solicita e envia exatamente nome, username, email e password

### Requirement: Shell autenticado responsivo e orientado por papel
O shell SHALL apresentar navegação, identidade, item ativo, sessão e Admin somente para ADMIN; em telas menores, a navegação SHALL ser temporária, fechável por controle, teclado ou rota, preservando foco.

#### Scenario: Usuário abre a navegação no mobile
- **WHEN** o usuário ativa o menu em viewport pequena
- **THEN** o painel pode ser fechado por controle explícito, teclado ou seleção de rota e não deixa o conteúdo inacessível

### Requirement: Componentes e estados seguem padrões compartilhados
Botões, cards, campos, modais, badges, cabeçalhos, métricas e estados assíncronos SHALL seguir padrões reutilizáveis, com estados interativos visíveis; modais SHALL caber ou rolar no viewport e operar por teclado.

#### Scenario: CTA principal recebe interação
- **WHEN** um CTA principal recebe hover, foco ou ativação
- **THEN** fornece feedback coerente e mantém foco visível

### Requirement: Páginas financeiras mantêm significado e ganham hierarquia visual
Dashboard, Corretoras, Ações, Carteiras, Operações, Histórico e Admin SHALL adotar a identidade compartilhada sem alterar dados, permissões, contratos ou regras de negócio; seleção de ativo, cotação e preço editável SHALL permanecer equivalentes.

#### Scenario: Usuário abre nova operação
- **WHEN** o modal de operação é exibido
- **THEN** seleção, cotação, tipo, quantidade, preço editável, proteção contra resposta tardia e total preservam o comportamento vigente

### Requirement: Responsividade, acessibilidade e movimento reduzido
As rotas ativas SHALL permanecer utilizáveis em desktop, tablet e mobile, com layouts adaptativos, tabelas roláveis quando necessário, labels, foco visível, navegação por teclado, ARIA pertinente e redução de animações não essenciais conforme `prefers-reduced-motion`.

#### Scenario: Usuário navega por teclado
- **WHEN** o usuário percorre links, botões, campos, menu e modal sem mouse
- **THEN** a ordem é previsível, o foco permanece visível e ações essenciais são operáveis

### Requirement: Carteira distingue custo de valor de mercado

O dashboard e o detalhe da carteira SHALL exibir rótulos claros para valor investido, preço médio, cotação atual, patrimônio atual, resultado e rentabilidade quando aplicáveis. A distribuição por ativo SHALL continuar baseada no patrimônio atual, e uma comparação compacta SHALL distinguir custo e patrimônio sem alterar o tema visual existente.

#### Scenario: Usuário consulta uma posição valorizada
- **WHEN** patrimônio atual excede o valor investido
- **THEN** tabela, resumo e gráfico apresentam valores coerentes e identificam o resultado positivo sem tratar a cotação como preço da operação

### Requirement: Atualização explícita de cotações no detalhe da carteira

O frontend SHALL apresentar o botão `Atualizar cotações` no detalhe autenticado de uma carteira e SHALL chamar somente `PUT /carteiras/{id}/atualizar-cotacoes` para essa ação. O botão SHALL ter estados normal e carregando; durante a requisição, MUST ficar desabilitado e impedir clique duplicado. A interface MUST apresentar feedback acessível de sucesso, sucesso parcial e erro controlado, sem expor detalhes internos de provider.

Quando a resposta indicar pelo menos uma atualização, o frontend MUST recarregar as posições e o resumo da carteira e atualizar os cards, gráfico comparativo e distribuição dependentes. A apresentação de BRL e USD MUST permanecer separada, sem conversão implícita. Em resposta sem atualização bem-sucedida, erro HTTP ou erro de rede, a interface SHALL manter os dados atuais e oferecer feedback recuperável.

#### Scenario: Sucesso total

- **WHEN** o endpoint retorna `quantidadeAtualizada` positiva e `quantidadeComFalha` zero
- **THEN** a interface informa sucesso e recarrega posições, resumo e visualizações dependentes

#### Scenario: Sucesso parcial

- **WHEN** o endpoint retorna quantidades positivas de atualização e falha
- **THEN** a interface informa sucesso parcial, pode identificar os tickers retornados e recarrega posições, resumo e visualizações dependentes

#### Scenario: Erro controlado do lote

- **WHEN** o endpoint retorna zero atualizações e uma ou mais falhas, ou a requisição falha
- **THEN** a interface informa erro recuperável, reabilita o botão e não substitui os dados exibidos por dados artificiais
