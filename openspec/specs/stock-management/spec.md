# stock-management Specification

## Purpose

Disponibilizar o cadastro mestre persistente de ações brasileiras e americanas, com identidade de mercado explícita, para servir de base às futuras integrações de ticker e cotação.

## Requirements

### Requirement: Registrar histórico nas operações de cotação

Após um cadastro ou atualização de ação concluído com dados externos válidos, o sistema SHALL registrar uma observação histórica contendo a cotação e o timestamp aceitos. A observação MUST ser persistida na mesma transação da ação correspondente; falhas de validação, duplicidade ou indisponibilidade MUST NOT criar observação.

#### Scenario: Cadastro registra observação inicial

- **WHEN** o cliente cadastra uma ação válida
- **THEN** o sistema persiste o ativo e sua primeira observação histórica correspondente

#### Scenario: Atualização registra observação

- **WHEN** o cliente atualiza a cotação de uma ação existente com sucesso
- **THEN** o sistema altera apenas os campos de cotação da ação e acrescenta uma observação histórica, preservando as anteriores

#### Scenario: Erro não registra observação

- **WHEN** a resolução ou atualização falha por resposta externa inválida, ticker não consultável ou indisponibilidade
- **THEN** o sistema não altera a ação nem persiste uma nova observação

### Requirement: Cadastro mestre de ação

O sistema SHALL representar uma ação como ativo mestre, e não como uma compra ou posição de usuário. Cada ação SHALL possuir identificador persistente, ticker, nome da empresa, mercado, moeda, cotação atual e data/hora da cotação. O cadastro MUST NOT possuir relacionamento obrigatório ou opcional com corretora nesta capability.

#### Scenario: Um ativo representa o ticker negociado

- **WHEN** PETR4 é cadastrado no mercado Brasil
- **THEN** o sistema mantém um único ativo mestre que poderá ser referenciado por operações futuras

### Requirement: Mercado, moeda e identidade lógica controlados

O sistema SHALL suportar somente os mercados Brasil e Estados Unidos. A moeda SHALL ser definida pelo sistema como BRL para Brasil e USD para Estados Unidos. O sistema SHALL normalizar o ticker para letras maiúsculas e SHALL considerar `(ticker, mercado)` como a identidade lógica do ativo.

#### Scenario: Cadastro em cada mercado suportado

- **WHEN** um cliente cadastra um ticker no mercado Brasil ou Estados Unidos
- **THEN** o sistema armazena o ticker normalizado e associa respectivamente BRL ou USD

#### Scenario: Mesmo ticker em mercados distintos

- **WHEN** o mesmo ticker normalizado é cadastrado uma vez no Brasil e uma vez nos Estados Unidos
- **THEN** o sistema permite os dois ativos por representarem identidades lógicas distintas

### Requirement: Cadastro brasileiro resolvido por provider

O sistema SHALL disponibilizar `POST /acoes` para cadastro de uma ação recebendo exclusivamente `ticker` e `mercado`. O mercado SHALL ser obrigatório e selecionar deterministicamente a fonte externa: Brasil usa a fonte brasileira e Estados Unidos usa a fonte americana. O sistema SHALL normalizar o ticker e preencher exclusivamente a partir da resolução externa o ticker persistido, nome da empresa, moeda, cotação atual e data/hora da cotação; o mercado persistido SHALL corresponder ao mercado solicitado e validado pela fonte. O cliente MUST NOT informar nem sobrescrever moeda, nome da empresa, cotação ou data/hora, e o endpoint MUST NOT aceitar dados manuais novamente.

#### Scenario: Cadastro válido

- **WHEN** o cliente envia ticker válido, um mercado suportado e o ativo ainda não está cadastrado
- **THEN** o sistema seleciona a fonte correspondente, persiste o ativo resolvido, responde `201 Created`, devolve os dados obtidos e derivados e informa o URI `/acoes/{id}` no header `Location`

#### Scenario: Cadastro americano válido

- **WHEN** o cliente envia ticker americano válido, mercado `ESTADOS_UNIDOS` e o ativo ainda não está cadastrado
- **THEN** o sistema seleciona a fonte americana, persiste o ativo resolvido com moeda USD, responde `201 Created`, devolve os dados obtidos e derivados e informa o URI `/acoes/{id}` no header `Location`

#### Scenario: Dados de cadastro inválidos

- **WHEN** o body não contém ticker ou mercado válidos ou contém atributos adicionais de cadastro manual
- **THEN** o sistema responde `400 Bad Request`, não consulta a fonte externa quando a falha é local e não persiste o ativo

### Requirement: Unicidade de ativo por ticker e mercado

O sistema MUST NOT persistir mais de uma ação com o mesmo ticker normalizado no mesmo mercado, inclusive quando o ticker recebido variar somente em maiúsculas, minúsculas ou espaços externos.

#### Scenario: Ativo duplicado no mesmo mercado

- **WHEN** já existe uma ação com o mesmo ticker normalizado e mercado
- **THEN** o sistema responde `409 Conflict` e preserva somente o cadastro existente

### Requirement: Consulta de ações

O sistema SHALL disponibilizar `GET /acoes` para listar ações em ordem crescente de id e `GET /acoes/{id}` para consultar uma ação persistida.

#### Scenario: Consulta existente

- **WHEN** o cliente lista ações ou consulta um id existente
- **THEN** o sistema responde `200 OK` com os dados persistidos do ativo

#### Scenario: Consulta inexistente

- **WHEN** o cliente consulta um id positivo sem ação correspondente
- **THEN** o sistema responde `404 Not Found`

### Requirement: Consulta determinística por ticker

O sistema SHALL disponibilizar `GET /acoes/ticker/{ticker}` e normalizar o ticker informado. O endpoint SHALL aceitar opcionalmente o parâmetro `mercado`; com ele, retornará a ação correspondente a `(ticker, mercado)`. Sem esse parâmetro, o sistema SHALL retornar a única ação encontrada para o ticker, responder `404 Not Found` se não houver nenhuma e responder `400 Bad Request` se houver mais de uma ação em mercados distintos.

#### Scenario: Ticker sem ambiguidade

- **WHEN** somente uma ação corresponde ao ticker normalizado solicitado sem parâmetro de mercado
- **THEN** o sistema responde `200 OK` com essa ação

#### Scenario: Ticker ambíguo

- **WHEN** existem ações com o mesmo ticker normalizado em Brasil e Estados Unidos e o cliente não informa o parâmetro `mercado`
- **THEN** o sistema responde `400 Bad Request` indicando que o mercado é necessário

### Requirement: Atualização real de cotação adiada

O sistema MUST expor `PUT /acoes/{id}/atualizar-cotacao`, sem corpo de requisição, para atualizar uma ação já persistida quando houver provider real para o mercado salvo. O sistema MUST buscar a ação pelo ID e retornar `404` quando ela não existir.

O sistema MUST selecionar o `StockDataProvider` exclusivamente pelo `mercado` já persistido, por meio de `StockDataProviderSelector`, e consultar diretamente o ticker canônico persistido. A atualização MUST NOT redescobrir o mercado, criar uma ação, nem repetir a descoberta de instrumento já concluída no cadastro. Para `ESTADOS_UNIDOS`, a atualização MUST consultar somente o endpoint de cotação da Twelve Data, sem chamar `symbol_search`.

Antes de persistir, o sistema MUST validar que a resposta é compatível com a ação salva: ticker normalizado compatível, moeda esperada para o mercado, cotação positiva e timestamp válido. A atualização MUST modificar somente `cotacaoAtual` e `dataHoraCotacao`; `id`, `ticker`, `mercado`, `nomeEmpresa` e `moeda` MUST permanecer inalterados. Dados externos incompatíveis MUST resultar em `502`; ticker não encontrado ou não consultável MUST resultar em `422`; indisponibilidade, timeout, falha de conexão, rate limit, erro `5xx` ou configuração/autenticação externa necessária MUST resultar em `503`, sem expor detalhes internos do provider.

#### Scenario: Atualização de cotação brasileira

- **WHEN** o cliente chama `PUT /acoes/{id}/atualizar-cotacao` para uma ação existente de mercado `BRASIL`
- **THEN** o sistema seleciona o provider brasileiro, consulta diretamente o ticker persistido em uma única chamada externa, atualiza somente a cotação e seu timestamp e retorna `200` com a ação atualizada

#### Scenario: Atualização de cotação americana

- **WHEN** o cliente chama `PUT /acoes/{id}/atualizar-cotacao` para uma ação existente de mercado `ESTADOS_UNIDOS`
- **THEN** o sistema seleciona o provider da Twelve Data, consulta diretamente a cotação do ticker persistido em uma única chamada externa, sem executar `symbol_search`, atualiza somente a cotação e seu timestamp e retorna `200` com a ação atualizada

#### Scenario: Tentativa de atualização sem integração real

- **WHEN** o cliente chama `PUT /acoes/{id}/atualizar-cotacao` para uma ação existente
- **THEN** o sistema usa o provider real já configurado para o mercado persistido e não aceita cotação falsa, manual ou sem timestamp externo válido

#### Scenario: Ação inexistente

- **WHEN** o cliente chama `PUT /acoes/{id}/atualizar-cotacao` com um ID sem ação persistida
- **THEN** o sistema retorna `404` e não consulta provider externo

#### Scenario: Resposta de cotação incompatível

- **WHEN** o provider retorna ticker, moeda, preço ou timestamp incompatível com a ação persistida
- **THEN** o sistema retorna `502` e não altera a ação

#### Scenario: Ticker não consultável no provider

- **WHEN** o provider informa que o ticker persistido não existe ou não é consultável
- **THEN** o sistema retorna `422` e não altera a ação

#### Scenario: Indisponibilidade da fonte de cotação

- **WHEN** o provider sofre timeout, falha de conexão, rate limit, erro `5xx` ou exige configuração/autenticação ausente
- **THEN** o sistema retorna `503` e não altera a ação

### Requirement: Erros HTTP centralizados para ações

O sistema SHALL representar request inválido, ação inexistente e ativo duplicado pelo contrato JSON centralizado existente, sem expor detalhes de persistência.

#### Scenario: Erro tratado de ação

- **WHEN** uma operação de ações produz erro de validação, inexistência ou duplicidade
- **THEN** o sistema responde respectivamente `400`, `404` ou `409` com corpo compatível com o contrato centralizado

### Requirement: Aceitar somente cotações externas representáveis

Antes de persistir uma cotação recebida de provider durante cadastro ou atualização, o sistema MUST aceitar somente valor positivo que caiba em `NUMERIC(19,8)`, isto é, no máximo 11 algarismos inteiros e 8 casas decimais. Uma resposta externa fora desse limite MUST ser tratada como dado externo inválido com `502 Bad Gateway`, sem criar ou alterar a ação.

#### Scenario: Cotação externa com oito casas decimais

- **WHEN** o provider retorna uma cotação positiva com até oito casas decimais e até onze algarismos inteiros
- **THEN** o sistema aceita a resposta e preserva o valor na ação cadastrada ou atualizada

#### Scenario: Cotação externa fora da precisão contratada

- **WHEN** o provider retorna cotação positiva com mais de oito casas decimais ou mais de onze algarismos inteiros
- **THEN** o sistema responde `502 Bad Gateway` e não persiste nem altera a ação

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

### Requirement: Catálogo e persistência sob demanda permanecem distintos

O sistema SHALL manter `GET /acoes` como listagem de ações persistidas e SHALL oferecer `GET /acoes/catalogo` como consulta externa separada. A identidade de qualquer item em ambos os fluxos SHALL continuar sendo `(ticker normalizado, mercado)`. `POST /acoes` e `POST /acoes/resolver` SHALL permanecer disponíveis para compatibilidade e persistência sob demanda.

#### Scenario: Ativo externo ainda não utilizado
- **WHEN** um ativo aparece no catálogo, mas não foi cadastrado, resolvido ou utilizado
- **THEN** ele não aparece em `GET /acoes` e não existe registro local criado por essa visualização

#### Scenario: Mesmo ticker em mercados diferentes
- **WHEN** providers retornam o mesmo ticker para Brasil e Estados Unidos
- **THEN** os itens permanecem distintos pelo campo `mercado`

#### Scenario: Ativo selecionado para uso
- **WHEN** o usuário seleciona uma sugestão válida para registrar uma operação
- **THEN** o sistema pode resolvê-la e persistí-la sob demanda sem depender de um cadastro manual na interface de Ações

### Requirement: Refresh de carteira reutiliza atualização unitária de ação

Quando um refresh explícito de carteira solicitar a atualização de uma ação elegível, o sistema MUST aplicar integralmente as regras de `PUT /acoes/{id}/atualizar-cotacao`: selecionar o `StockDataProvider` pelo mercado persistido, consultar o ticker canônico uma única vez, validar ticker, moeda, valor e timestamp e persistir somente `cotacaoAtual` e `dataHoraCotacao` junto da observação histórica correspondente. `BRASIL` MUST continuar usando brapi e `ESTADOS_UNIDOS` MUST continuar usando Twelve Data, sem `symbol_search` para a cotação americana.

Cada sucesso MUST preservar a atomicidade existente entre a cotação atual e seu histórico. A falha de uma ação durante o refresh de carteira MUST NOT desfazer a atualização já bem-sucedida de outra ação e MUST NOT expor detalhes internos do provider.

#### Scenario: Ativo brasileiro no refresh

- **WHEN** o refresh explícito de carteira processa uma ação persistida de mercado `BRASIL`
- **THEN** o sistema consulta brapi uma vez pelo ticker persistido e persiste a cotação e a observação histórica válidas

#### Scenario: Ativo americano no refresh

- **WHEN** o refresh explícito de carteira processa uma ação persistida de mercado `ESTADOS_UNIDOS`
- **THEN** o sistema consulta Twelve Data uma vez pelo ticker persistido, sem `symbol_search`, e persiste a cotação e a observação histórica válidas

#### Scenario: Falha de provider no refresh

- **WHEN** um provider falha ou devolve dados inválidos para uma ação do refresh
- **THEN** a ação não é alterada nem recebe nova observação histórica, e o refresh continua os demais ativos elegíveis
