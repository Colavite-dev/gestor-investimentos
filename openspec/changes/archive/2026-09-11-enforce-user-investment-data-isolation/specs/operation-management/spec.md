## ADDED Requirements

### Requirement: Operações respeitam o owner da carteira

O sistema SHALL criar e consultar operações somente quando a carteira associada pertence ao usuário autenticado. A validação de saldo e a ordenação SHALL considerar exclusivamente as operações daquela carteira autorizada, preservando todas as regras financeiras existentes.

#### Scenario: Compra na própria carteira

- **WHEN** USER_A registra uma compra válida em sua própria carteira
- **THEN** a operação é persistida e pode ser consultada por USER_A

#### Scenario: Compra em carteira alheia

- **WHEN** USER_B tenta registrar uma compra ou venda na carteira de USER_A
- **THEN** a API responde `404 Not Found`, não executa a regra financeira sobre dados de USER_A e não persiste a operação

#### Scenario: Consulta isolada de operações

- **WHEN** USER_B consulta por ID uma operação ou lista operações da carteira de USER_A
- **THEN** a API responde `404 Not Found` e não retorna fatos transacionais de USER_A
