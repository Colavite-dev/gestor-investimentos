# investment-data-isolation Specification

## Purpose

Garantir que patrimônio e fatos financeiros privados sejam acessíveis somente pelo proprietário autenticado, preservando dados mestres de mercado como recursos compartilhados.

## Requirements

### Requirement: Propriedade privada deriva da identidade autenticada

Cada carteira SHALL pertencer a exatamente um usuário e o backend SHALL determinar esse owner pelo ID estável presente na identidade autenticada. Requests de carteira e operação MUST NOT aceitar `usuarioId`, owner ou username como fonte de autorização.

#### Scenario: Usuário cria carteira própria

- **WHEN** um usuário autenticado envia `POST /carteiras` com payload válido
- **THEN** a carteira é associada ao ID daquele usuário sem receber owner do cliente

#### Scenario: Cliente tenta forjar identidade

- **WHEN** um cliente acrescenta `usuarioId`, owner ou username ao payload de criação
- **THEN** o backend não atribui a carteira ou operação a outra identidade e aplica o contrato estrito de entrada vigente

### Requirement: Autorização por objeto não revela recursos privados

Consulta ou mutação de carteira e operação SHALL ser limitada por owner no backend. Um recurso inexistente e um recurso pertencente a outro usuário SHALL produzir a mesma resposta `404 Not Found`, sem confirmar sua existência ou seu proprietário.

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

### Requirement: Papel administrativo não concede ownership financeiro implícito

O papel `ADMIN` SHALL continuar autorizando somente os recursos administrativos já especificados e MUST NOT conceder acesso automático às carteiras, operações, posições ou resumos de outros usuários pelos endpoints comuns de investimento.

#### Scenario: ADMIN tenta acessar carteira alheia

- **WHEN** um ADMIN autenticado solicita por ID uma carteira pertencente a outro usuário
- **THEN** a API aplica a mesma política de owner e retorna `404 Not Found`

### Requirement: Dados de mercado permanecem globais

`Acao`, catálogo, cotação atual e histórico de cotação SHALL permanecer globais e compartilháveis. Compartilhar a mesma `Acao` entre usuários MUST NOT compartilhar carteiras, operações, quantidades, custos, posições, resumos ou valuation.

#### Scenario: Dois usuários utilizam PETR3

- **WHEN** USER_A e USER_B registram compras da mesma `Acao` PETR3 em suas próprias carteiras
- **THEN** ambos referenciam o mesmo ativo mestre e obtêm operações, quantidades, custos médios e valuation calculados independentemente

### Requirement: Migration não atribui nem remove legado automaticamente

Uma evolução de schema MUST NOT atribuir carteiras legadas a usuário arbitrário, executar backfill implícito ou apagar dados. Se existirem carteiras sem owner, a migration SHALL falhar transacionalmente; qualquer limpeza de dados locais descartáveis SHALL ocorrer somente após autorização explícita e fora da migration.

#### Scenario: Banco vazio recebe o novo schema

- **WHEN** a migration de ownership é aplicada sem carteiras legadas
- **THEN** o schema passa a exigir owner sem precisar de atribuição de legado

#### Scenario: Banco possui carteiras legadas

- **WHEN** a migration encontra qualquer carteira criada antes da existência de ownership
- **THEN** ela falha de forma transacional e clara, sem escolher usuário, apagar dados ou deixar schema parcialmente aplicado

#### Scenario: Dados locais foram declarados descartáveis

- **WHEN** o usuário autoriza explicitamente remover um conjunto previamente inventariado de carteiras e operações locais de teste antes da migration
- **THEN** somente esses registros são removidos, e a migration permanece sem comandos de exclusão ou atribuição de owner
