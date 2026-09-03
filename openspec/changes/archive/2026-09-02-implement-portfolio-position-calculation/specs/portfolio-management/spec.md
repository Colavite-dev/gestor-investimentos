## MODIFIED Requirements

### Requirement: Manter escopo de carteira inicial
Esta capacidade MUST limitar seus próprios endpoints de cadastro e consulta básica, mas a carteira MAY expor consultas derivadas de posições e resumo pela capacidade `portfolio-position-calculation`. A carteira não deve executar cálculos dentro de sua entidade nem alterar dados cadastrais durante essas consultas.

#### Scenario: Cadastro sem operações
- **WHEN** uma carteira é criada
- **THEN** ela não cria operações, posições ou cálculos derivados automaticamente

#### Scenario: Consulta derivada
- **WHEN** o cliente consulta posições ou resumo de uma carteira existente
- **THEN** o sistema calcula a resposta a partir das operações e ações persistidas sem alterar a carteira

#### Scenario: Carteira referenciada por operação
- **WHEN** uma operação válida é cadastrada para uma carteira existente
- **THEN** a operação mantém referência à carteira sem alterar os dados cadastrais dela
