## MODIFIED Requirements

### Requirement: Propriedade privada deriva da identidade autenticada
Cada carteira e cada corretora cadastrada SHALL pertencer a exatamente um usuário e o backend SHALL determinar esse owner pelo ID estável presente na identidade autenticada. Requests de carteira, operação e corretora MUST NOT aceitar `usuarioId`, owner ou username como fonte de autorização.

#### Scenario: Usuário cria carteira própria

- **WHEN** um usuário autenticado envia `POST /carteiras` com payload válido
- **THEN** a carteira é associada ao ID daquele usuário sem receber owner do cliente

#### Scenario: Usuário cria corretora própria

- **WHEN** um usuário autenticado envia `POST /corretoras` com payload válido
- **THEN** a corretora é associada ao ID daquele usuário sem receber owner do cliente

#### Scenario: Cliente tenta forjar identidade

- **WHEN** um cliente acrescenta `usuarioId`, owner ou username ao payload de criação
- **THEN** o backend não atribui carteira, operação ou corretora a outra identidade e aplica o contrato estrito de entrada vigente

### Requirement: Autorização por objeto não revela recursos privados

Consulta ou mutação de carteira, operação e corretora SHALL ser limitada por owner no backend. Um recurso inexistente e um recurso pertencente a outro usuário SHALL produzir a mesma resposta `404 Not Found`, sem confirmar sua existência ou seu proprietário.

#### Scenario: Outro usuário consulta carteira por ID

- **WHEN** USER_B solicita `GET /carteiras/{id}` para uma carteira de USER_A
- **THEN** a API retorna `404 Not Found` sem dados da carteira e sem indicar que pertence a USER_A

#### Scenario: Outro usuário consulta derivados da carteira

- **WHEN** USER_B solicita operações, posições ou resumo pelo ID de uma carteira de USER_A
- **THEN** cada endpoint retorna `404 Not Found` sem dados financeiros de USER_A

#### Scenario: Outro usuário consulta operação por ID

- **WHEN** USER_B solicita `GET /operacoes/{id}` para uma operação cuja carteira pertence a USER_A
- **THEN** a API retorna `404 Not Found` sem dados da operação

#### Scenario: Outro usuário tenta registrar operação

- **WHEN** USER_B envia `POST /operacoes` apontando para uma carteira de USER_A
- **THEN** a API retorna `404 Not Found` e não persiste operação alguma

#### Scenario: Outro usuário lista corretoras

- **WHEN** USER_A possui uma corretora e USER_B solicita `GET /corretoras`
- **THEN** a resposta de USER_B não inclui a corretora de USER_A

#### Scenario: Outro usuário consulta corretora por ID ou CNPJ

- **WHEN** USER_B solicita `GET /corretoras/{id}` ou `GET /corretoras/cnpj/{cnpj}` para uma corretora pertencente a USER_A
- **THEN** a API retorna `404 Not Found` sem dados da corretora e sem indicar que pertence a USER_A

### Requirement: Migration não atribui nem remove legado automaticamente

Uma evolução de schema MUST NOT atribuir carteiras ou corretoras legadas a usuário arbitrário, executar backfill implícito, usar usuário fixo ou hardcoded como owner, ou apagar dados. Se existirem carteiras ou corretoras sem owner, a migration SHALL falhar transacionalmente; qualquer limpeza ou migração explícita de dados locais SHALL ocorrer somente após autorização explícita e fora da migration. Para corretoras legadas, a estratégia de tratamento SHALL ser definida deliberadamente no PLAN antes da implementação da V9, preservando integridade e isolamento entre usuários.

#### Scenario: Banco vazio recebe o novo schema

- **WHEN** a migration de ownership é aplicada sem carteiras ou corretoras legadas
- **THEN** o schema passa a exigir owner sem precisar de atribuição de legado

#### Scenario: Banco possui carteiras legadas

- **WHEN** a migration encontra qualquer carteira ou corretora criada antes da existência de ownership
- **THEN** ela falha de forma transacional e clara, sem escolher usuário, apagar dados ou deixar schema parcialmente aplicado

#### Scenario: Dados locais foram declarados descartáveis

- **WHEN** o usuário autoriza explicitamente remover um conjunto previamente inventariado de carteiras, corretoras ou operações locais de teste antes da migration
- **THEN** somente esses registros são removidos fora da migration, e a migration permanece sem comandos de exclusão ou atribuição de owner
