# portfolio-position-calculation Specification

## Purpose

Transformar operações e cotações persistidas em uma visão determinística da composição e dos valores atuais de uma carteira.

## Requirements

### Requirement: Calcular posição por ativo
O sistema MUST calcular, para cada ação com operações na carteira, a quantidade líquida como compras menos vendas, o preço médio pelo custo ponderado das compras ainda mantidas e o valor investido correspondente. Operações não devem ser alteradas.

#### Scenario: Compras e vendas
- **WHEN** uma carteira possui compras e vendas de uma ação
- **THEN** a posição apresenta quantidade líquida e preço médio calculados sem alterar os registros originais

#### Scenario: Ativo totalmente vendido
- **WHEN** as vendas igualam ou superam a quantidade comprada
- **THEN** a posição não apresenta quantidade mantida positiva e não gera patrimônio positivo para o ativo

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
