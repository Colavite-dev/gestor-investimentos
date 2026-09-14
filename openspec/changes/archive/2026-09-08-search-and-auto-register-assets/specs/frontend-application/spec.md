## ADDED Requirements

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
