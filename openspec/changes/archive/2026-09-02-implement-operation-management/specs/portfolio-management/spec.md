## MODIFIED Requirements

### Requirement: Manter escopo de carteira inicial
Esta capacidade MUST limitar seus próprios endpoints ao cadastro e consulta da carteira, mas a carteira MAY ser referenciada por operações registradas pela capacidade `operation-management`. A carteira não deve calcular posição, preço médio, patrimônio ou dashboard nesta mudança.

#### Scenario: Cadastro sem operações
- **WHEN** uma carteira é criada
- **THEN** ela não cria operações, posições ou cálculos derivados automaticamente

#### Scenario: Carteira referenciada por operação
- **WHEN** uma operação válida é cadastrada para uma carteira existente
- **THEN** a operação mantém referência à carteira sem alterar os dados cadastrais dela
