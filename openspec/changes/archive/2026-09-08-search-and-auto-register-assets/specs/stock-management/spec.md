## ADDED Requirements

### Requirement: Pesquisa remota de sugestões de ações sem persistência
O sistema SHALL disponibilizar `GET /acoes/pesquisar?q={termo}` para pesquisar, sem escrita, sugestões de ações suportadas nos mercados Brasil e Estados Unidos. O parâmetro `q` SHALL sofrer trim e comparação case-insensitive, MUST conter entre dois e vinte caracteres compatíveis com busca e SHALL produzir uma lista limitada de sugestões. Cada sugestão SHALL expor ticker, nome da empresa quando disponível, mercado e moeda, mas MUST NOT possuir nem exigir um identificador persistido. A pesquisa MUST NOT criar ou atualizar `Acao`, criar `CotacaoHistorica`, executar operação nem produzir qualquer efeito no banco, inclusive quando uma sugestão corresponda a ativo ainda ausente do cadastro local.

#### Scenario: Pesquisa encontra sugestão externa
- **WHEN** o cliente pesquisa um termo válido que corresponda a um ativo elegível em uma fonte suportada
- **THEN** o sistema responde `200 OK` com sugestão contendo ticker, mercado e moeda derivados da fonte, sem persistir ação

#### Scenario: Pesquisa não encontra ativos
- **WHEN** o termo válido não corresponde a nenhuma ação elegível nos mercados suportados
- **THEN** o sistema responde `200 OK` com lista vazia e não altera o cadastro de ações

#### Scenario: Termo localmente inválido
- **WHEN** o parâmetro de pesquisa não contém ao menos dois caracteres ou contém caracteres incompatíveis com a busca
- **THEN** o sistema responde `400 Bad Request`, não consulta provider e não persiste ação

### Requirement: Resolver sugestão em ação persistida
O sistema SHALL disponibilizar `POST /acoes/resolver` recebendo ticker e mercado para transformar uma sugestão escolhida em uma ação persistida. O sistema SHALL normalizar o ticker e usar `(ticker, mercado)` como identidade. Se a ação já existir, SHALL retorná-la sem nova consulta externa, atualização de cotação ou histórico. Se não existir, SHALL validar o ativo exclusivamente pelo provider selecionado pelo mercado recebido — Brasil somente pela fonte brasileira e Estados Unidos somente pela fonte americana — persistir atomicamente a ação e sua primeira observação de cotação e retornar a ação com ID. O endpoint SHALL responder `200 OK` para ação reutilizada ou criada pela resolução e MUST NOT repetir a busca agregada após a seleção.

#### Scenario: Sugestão corresponde a ação já cadastrada
- **WHEN** o cliente resolve ticker e mercado que já correspondem a uma ação persistida
- **THEN** o sistema retorna `200 OK` com a ação existente e não cria nova ação nem novo histórico

#### Scenario: Sugestão selecionada cria ação válida
- **WHEN** o cliente resolve ticker e mercado ainda ausentes e o provider confirma dados válidos
- **THEN** o sistema persiste a ação e sua observação inicial de cotação e retorna `200 OK` com o ID persistido

#### Scenario: Resolução concorrente da mesma identidade
- **WHEN** duas requisições de resolução válidas para o mesmo ticker normalizado e mercado concorrem sem ação prévia
- **THEN** o sistema mantém somente uma ação para a identidade e cada requisição bem-sucedida recebe a ação persistida, sem expor conflito de duplicidade como falha do fluxo de resolução

#### Scenario: Resolução não aceita ação em texto livre
- **WHEN** o cliente envia ticker ou mercado inválido para resolver
- **THEN** o sistema responde `400 Bad Request` e não persiste ação

### Requirement: Falhas de pesquisa e resolução seguem a semântica de ações
A pesquisa e a resolução SHALL manter o contrato centralizado de erros de ações: ativo inequivocamente inexistente, inválido, não suportado ou ambíguo SHALL resultar em `422 Unprocessable Content`; resposta externa malformada, incompatível ou sem semântica segura SHALL resultar em `502 Bad Gateway`; indisponibilidade, autenticação/configuração externa necessária, quota/rate limit, timeout, conexão ou `5xx` SHALL resultar em `503 Service Unavailable`. O sistema MUST NOT expor corpo bruto, URL, token, chave ou detalhes internos do provider.

#### Scenario: Resolução de ativo inexistente
- **WHEN** uma sugestão selecionada não pode ser confirmada como ativo elegível pelo provider
- **THEN** o sistema responde `422 Unprocessable Content` e não persiste ação

#### Scenario: Provider indisponível durante pesquisa ou resolução
- **WHEN** uma fonte necessária responde rate limit, timeout, falha de conexão, falha de autenticação/configuração ou `5xx`
- **THEN** o sistema responde `503 Service Unavailable` e não persiste ação por causa da pesquisa ou resolução

### Requirement: Pesquisa agregada tolera falha parcial sem ocultar sugestões válidas
O sistema SHALL consultar os providers Brasil e Estados Unidos para a pesquisa agregada e combinar somente sugestões elegíveis, deduplicadas por `(ticker, mercado)`. Quando um provider falhar, mas o outro retornar resposta utilizável, inclusive lista vazia válida, o sistema SHALL responder `200 OK` com os resultados disponíveis e MUST NOT inventar sugestões nem expor a falha interna. O sistema SHALL responder `503 Service Unavailable` somente quando nenhum provider puder produzir resposta utilizável por indisponibilidade, quota, timeout, conexão, `5xx`, autorização ou configuração externa; SHALL responder `502 Bad Gateway` quando nenhum provider puder produzir resposta utilizável por conteúdo externo malformado ou incompatível.

#### Scenario: Brasil indisponível e Estados Unidos utilizável
- **WHEN** a fonte brasileira falha operacionalmente e a fonte americana responde com dados utilizáveis
- **THEN** o sistema responde `200 OK` apenas com sugestões americanas disponíveis, sem persistência

#### Scenario: Estados Unidos indisponível e Brasil utilizável
- **WHEN** a fonte americana falha operacionalmente e a fonte brasileira responde com dados utilizáveis
- **THEN** o sistema responde `200 OK` apenas com sugestões brasileiras disponíveis, sem persistência

#### Scenario: Nenhum provider utilizável por indisponibilidade
- **WHEN** nenhum provider produz resposta utilizável por indisponibilidade operacional
- **THEN** o sistema responde `503 Service Unavailable` e não persiste ação

#### Scenario: Nenhum provider utilizável por conteúdo inválido
- **WHEN** nenhum provider produz resposta utilizável porque as respostas externas são malformadas ou incompatíveis
- **THEN** o sistema responde `502 Bad Gateway` e não persiste ação

### Requirement: Ordenação determinística das sugestões
O sistema SHALL deduplicar sugestões por `(ticker, mercado)` e ordená-las de modo determinístico, priorizando ticker exato, depois ticker que começa pelo termo, depois nome da empresa que contém o termo e, por fim, as demais correspondências elegíveis. Empates SHALL usar mercado e ticker normalizados; o sistema MUST NOT usar busca fuzzy, IA ou ranking não determinístico.

#### Scenario: Resultado exato é priorizado
- **WHEN** a pesquisa retorna uma sugestão cujo ticker é exatamente igual ao termo normalizado e outras correspondências elegíveis
- **THEN** a sugestão de ticker exato aparece antes das demais
