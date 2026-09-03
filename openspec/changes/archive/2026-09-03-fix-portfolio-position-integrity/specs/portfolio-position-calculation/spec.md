# Delta Specification: portfolio-position-calculation

## MODIFIED Requirements

### Requirement: Calcular posição por ativo

O sistema MUST calcular, para cada ação com operações válidas na carteira, a quantidade líquida como compras menos vendas na ordem `dataOperacao ASC, id ASC`. O preço médio e o custo representam somente a quantidade positiva ainda mantida. O cálculo MUST NOT truncar, ignorar, compensar ou ocultar uma venda; a integridade da sequência é garantida no registro da operação e operações não devem ser alteradas pela consulta.

#### Scenario: Compras e vendas

- **WHEN** uma carteira possui várias compras e vendas válidas de uma ação
- **THEN** a posição apresenta quantidade líquida, custo e preço médio corretos sem alterar registros originais

#### Scenario: Ativo totalmente vendido

- **WHEN** as vendas igualam exatamente a quantidade comprada
- **THEN** a ação tem saldo final zero e não aparece entre as posições atuais nem gera patrimônio positivo

#### Scenario: Empate de timestamp

- **WHEN** duas ou mais operações persistidas do mesmo ativo possuem o mesmo timestamp
- **THEN** o cálculo as processa por identificador crescente, produzindo resultado determinístico

#### Scenario: Invariável violada em dados persistidos

- **WHEN** a consulta encontrar dados legados ou corrompidos cuja sequência resulte em saldo negativo
- **THEN** ela falha explicitamente e não reduz, trunca ou esconde a venda inválida
