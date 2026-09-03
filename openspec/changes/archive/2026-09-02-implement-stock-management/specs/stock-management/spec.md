## Purpose

Disponibilizar o cadastro mestre persistente de ações brasileiras e americanas, com identidade de mercado explícita, para servir de base às futuras integrações de ticker e cotação.

## ADDED Requirements

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

### Requirement: Cadastro transitório sem provider de cotação
O sistema SHALL disponibilizar `POST /acoes` recebendo ticker, nome da empresa, mercado, cotação atual e data/hora da cotação. A moeda MUST NOT ser informada pelo cliente. Enquanto não houver provider real, esses dados serão fornecidos explicitamente pelo cliente e validados antes da persistência; o sistema MUST NOT inventar, simular ou consultar cotação externa nesta capability.

#### Scenario: Cadastro válido
- **WHEN** o cliente envia todos os dados obrigatórios e coerentes de um ativo ainda não cadastrado
- **THEN** o sistema persiste o ativo, responde `201 Created`, devolve a moeda derivada do mercado e informa o URI `/acoes/{id}` no header `Location`

#### Scenario: Dados de cadastro inválidos
- **WHEN** ticker, nome da empresa, mercado, cotação ou data/hora da cotação são ausentes ou inválidos
- **THEN** o sistema responde `400 Bad Request` e não persiste o ativo

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
O sistema MUST NOT expor `PUT /acoes/{id}/atualizar-cotacao` até que uma capability posterior forneça resolução de ticker e provider real de cotação. A ausência desse endpoint nesta capability MUST NOT ser substituída por cotação falsa ou atualização manual disfarçada.

#### Scenario: Tentativa de atualização sem integração real
- **WHEN** o cliente solicita `PUT /acoes/{id}/atualizar-cotacao` nesta capability
- **THEN** o sistema não oferece comportamento de atualização de cotação nesta rota

### Requirement: Erros HTTP centralizados para ações
O sistema SHALL representar request inválido, ação inexistente e ativo duplicado pelo contrato JSON centralizado existente, sem expor detalhes de persistência.

#### Scenario: Erro tratado de ação
- **WHEN** uma operação de ações produz erro de validação, inexistência ou duplicidade
- **THEN** o sistema responde respectivamente `400`, `404` ou `409` com corpo compatível com o contrato centralizado
