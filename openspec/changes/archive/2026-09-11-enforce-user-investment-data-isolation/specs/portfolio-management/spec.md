## ADDED Requirements

### Requirement: Carteiras são isoladas por usuário autenticado

O sistema SHALL associar cada nova carteira ao usuário autenticado, listar somente carteiras daquele owner e permitir nomes normalizados iguais para owners diferentes, mantendo unicidade de nome dentro de cada usuário.

#### Scenario: Listagem isolada

- **WHEN** USER_B envia `GET /carteiras` após USER_A criar uma carteira
- **THEN** a coleção não contém a carteira de USER_A

#### Scenario: Mesmo nome em contas diferentes

- **WHEN** USER_A e USER_B criam carteiras com o mesmo nome normalizado
- **THEN** ambas são aceitas e associadas aos respectivos owners

#### Scenario: Mesmo nome no mesmo owner

- **WHEN** um usuário tenta criar duas carteiras com o mesmo nome normalizado
- **THEN** a segunda tentativa retorna `409 Conflict`
