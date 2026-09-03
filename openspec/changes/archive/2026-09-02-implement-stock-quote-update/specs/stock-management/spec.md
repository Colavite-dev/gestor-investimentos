## MODIFIED Requirements

### Requirement: Atualização real de cotação adiada

O sistema MUST expor `PUT /acoes/{id}/atualizar-cotacao`, sem corpo de requisição, para atualizar uma ação já persistida quando houver provider real para o mercado salvo. O sistema MUST buscar a ação pelo ID e retornar `404` quando ela não existir.

O sistema MUST selecionar o `StockDataProvider` exclusivamente pelo `mercado` já persistido, por meio de `StockDataProviderSelector`, e consultar diretamente o ticker canônico persistido. A atualização MUST NOT redescobrir o mercado, criar uma ação, nem repetir a descoberta de instrumento já concluída no cadastro. Para `ESTADOS_UNIDOS`, a atualização MUST consultar somente o endpoint de cotação da Twelve Data, sem chamar `symbol_search`.

Antes de persistir, o sistema MUST validar que a resposta é compatível com a ação salva: ticker normalizado compatível, moeda esperada para o mercado, cotação positiva e timestamp válido. A atualização MUST modificar somente `cotacaoAtual` e `dataHoraCotacao`; `id`, `ticker`, `mercado`, `nomeEmpresa` e `moeda` MUST permanecer inalterados. Dados externos incompatíveis MUST resultar em `502`; ticker não encontrado ou não consultável MUST resultar em `422`; indisponibilidade, timeout, falha de conexão, rate limit, erro `5xx` ou configuração/autenticação externa necessária MUST resultar em `503`, sem expor detalhes internos do provider.

#### Scenario: Atualização de cotação brasileira

- **WHEN** o cliente chama `PUT /acoes/{id}/atualizar-cotacao` para uma ação existente de mercado `BRASIL`
- **THEN** o sistema seleciona o provider brasileiro, consulta diretamente o ticker persistido em uma única chamada externa, atualiza somente a cotação e seu timestamp e retorna `200` com a ação atualizada

#### Scenario: Atualização de cotação americana

- **WHEN** o cliente chama `PUT /acoes/{id}/atualizar-cotacao` para uma ação existente de mercado `ESTADOS_UNIDOS`
- **THEN** o sistema seleciona o provider da Twelve Data, consulta diretamente a cotação do ticker persistido em uma única chamada externa, sem executar `symbol_search`, atualiza somente a cotação e seu timestamp e retorna `200` com a ação atualizada

#### Scenario: Tentativa de atualização sem integração real

- **WHEN** o cliente chama `PUT /acoes/{id}/atualizar-cotacao` para uma ação existente
- **THEN** o sistema usa o provider real já configurado para o mercado persistido e não aceita cotação falsa, manual ou sem timestamp externo válido

#### Scenario: Ação inexistente

- **WHEN** o cliente chama `PUT /acoes/{id}/atualizar-cotacao` com um ID sem ação persistida
- **THEN** o sistema retorna `404` e não consulta provider externo

#### Scenario: Resposta de cotação incompatível

- **WHEN** o provider retorna ticker, moeda, preço ou timestamp incompatível com a ação persistida
- **THEN** o sistema retorna `502` e não altera a ação

#### Scenario: Ticker não consultável no provider

- **WHEN** o provider informa que o ticker persistido não existe ou não é consultável
- **THEN** o sistema retorna `422` e não altera a ação

#### Scenario: Indisponibilidade da fonte de cotação

- **WHEN** o provider sofre timeout, falha de conexão, rate limit, erro `5xx` ou exige configuração/autenticação ausente
- **THEN** o sistema retorna `503` e não altera a ação
