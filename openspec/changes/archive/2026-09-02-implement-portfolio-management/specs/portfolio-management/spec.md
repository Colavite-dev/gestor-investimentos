## Purpose

Oferecer um cadastro persistente e simples de carteiras para organizar futuros investimentos, sem antecipar operações, posições ou cálculos patrimoniais.

## ADDED Requirements

### Requirement: Cadastrar carteira
O sistema MUST permitir criar uma carteira informando um nome não vazio e, opcionalmente, uma descrição. O sistema MUST atribuir identificador e data de cadastro, sem exigir usuário autenticado no contexto atual de usuário único.

#### Scenario: Cadastro válido
- **WHEN** o cliente envia `POST /carteiras` com nome válido
- **THEN** o sistema persiste a carteira e retorna `201 Created` com sua representação DTO

#### Scenario: Request inválido
- **WHEN** o cliente envia nome ausente, vazio ou somente espaços, ou descrição acima do limite definido
- **THEN** o sistema rejeita com `400 Bad Request` sem persistir a carteira

#### Scenario: Nome duplicado
- **WHEN** o cliente envia nome já utilizado por outra carteira, ignorando diferenças de maiúsculas/minúsculas e espaços externos
- **THEN** o sistema retorna `409 Conflict` sem criar uma segunda carteira

### Requirement: Consultar carteiras
O sistema MUST listar as carteiras cadastradas e permitir consultar uma carteira por ID, retornando somente DTOs.

#### Scenario: Listagem
- **WHEN** o cliente envia `GET /carteiras`
- **THEN** o sistema retorna `200 OK` com a coleção de carteiras cadastradas

#### Scenario: Consulta existente
- **WHEN** o cliente envia `GET /carteiras/{id}` para um ID existente
- **THEN** o sistema retorna `200 OK` com a carteira correspondente

#### Scenario: Consulta inexistente
- **WHEN** o cliente consulta ID não cadastrado
- **THEN** o sistema retorna `404 Not Found` sem revelar detalhes internos

### Requirement: Persistir carteiras com integridade
O sistema MUST persistir cada carteira com nome normalizado para comparação, descrição opcional e data de cadastro, protegendo no banco a unicidade do nome normalizado.

#### Scenario: Unicidade concorrente
- **WHEN** duas tentativas concorrentes criam o mesmo nome normalizado
- **THEN** no máximo uma carteira é persistida e a outra é convertida em `409 Conflict`

### Requirement: Manter escopo de carteira inicial
Esta capacidade MUST limitar-se ao cadastro e consulta da carteira. Não deve criar operações, vínculos com ativos, cálculo de posição, preço médio, patrimônio ou dashboard.

#### Scenario: Cadastro sem operações
- **WHEN** uma carteira é criada
- **THEN** ela não cria operações, posições ou relacionamentos obrigatórios com ações
