## ADDED Requirements

### Requirement: Posições e resumos são calculados no escopo do owner

O sistema SHALL calcular posições, custo, preço médio, patrimônio, resultado e rentabilidade somente após confirmar que a carteira pertence ao usuário autenticado e somente a partir das operações dessa carteira. Ações e cotações globais MAY alimentar o valuation, mas MUST NOT ampliar o conjunto de operações consultado.

#### Scenario: Posições independentes para o mesmo ativo

- **WHEN** USER_A compra PETR3 por preços e quantidades diferentes de USER_B em carteiras próprias
- **THEN** cada usuário recebe quantidade, custo médio, patrimônio e resultado derivados somente de suas operações

#### Scenario: Resumo de carteira alheia

- **WHEN** USER_B consulta `GET /carteiras/{id}/resumo` ou `/posicoes` para carteira de USER_A
- **THEN** a API retorna `404 Not Found` e nenhum valor do patrimônio de USER_A

#### Scenario: Dashboard isolado

- **WHEN** USER_B carrega as carteiras e seleciona uma delas no Dashboard
- **THEN** valor investido, valor atual, resultado, rentabilidade, posições e distribuições derivam somente de carteira pertencente a USER_B
