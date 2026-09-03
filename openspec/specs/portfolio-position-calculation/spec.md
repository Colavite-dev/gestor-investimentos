# portfolio-position-calculation Specification

## Purpose

Transformar operações e cotações persistidas em uma visão determinística da composição e dos valores atuais de uma carteira.

## Requirements

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

### Requirement: Calcular patrimônio e resultado atual

O sistema MUST calcular patrimônio atual como quantidade líquida positiva multiplicada pela cotação atual persistida da ação, e lucro/prejuízo não realizado como patrimônio menos custo da posição.

#### Scenario: Cotação disponível

- **WHEN** a ação possui cotação atual válida
- **THEN** o resumo utiliza essa cotação sem consultar providers externos

#### Scenario: Cotação ausente

- **WHEN** uma ação não possui cotação válida
- **THEN** o sistema não inventa preço e representa o valor dependente da cotação como indisponível conforme contrato definido

### Requirement: Consultar posições da carteira

O sistema MUST disponibilizar consulta das posições de uma carteira e um resumo agregado, retornando DTOs e sem criar persistência derivada.

#### Scenario: Carteira com posições

- **WHEN** o cliente consulta `GET /carteiras/{id}/posicoes`
- **THEN** o sistema retorna posições agregadas por ação em ordem determinística

#### Scenario: Carteira sem operações

- **WHEN** o cliente consulta uma carteira existente sem operações
- **THEN** o sistema responde `200 OK` com coleção vazia e valores agregados zerados

#### Scenario: Carteira inexistente

- **WHEN** o cliente consulta posições de carteira não cadastrada
- **THEN** o sistema responde `404 Not Found`

### Requirement: Resumo da carteira

O sistema MUST disponibilizar `GET /carteiras/{id}/resumo` com quantidade de posições, valor investido, patrimônio atual e lucro/prejuízo agregado, preservando a moeda de cada ativo e documentando a agregação apenas quando as moedas forem compatíveis.

#### Scenario: Resumo com mercados distintos

- **WHEN** a carteira possui ativos BRL e USD
- **THEN** o sistema retorna os valores por moeda e não converte silenciosamente entre moedas

### Requirement: Escopo dos cálculos

Esta capacidade MUST ser somente leitura e não deve criar operações, alterar cotações, executar chamadas externas, implementar histórico ou dashboard.

#### Scenario: Consulta sem efeitos colaterais

- **WHEN** qualquer endpoint de posição ou resumo é chamado
- **THEN** nenhum registro de operação, ação ou carteira é alterado
