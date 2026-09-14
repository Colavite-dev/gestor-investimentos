## ADDED Requirements

### Requirement: Refresh de carteira reutiliza atualização unitária de ação

Quando um refresh explícito de carteira solicitar a atualização de uma ação elegível, o sistema MUST aplicar integralmente as regras de `PUT /acoes/{id}/atualizar-cotacao`: selecionar o `StockDataProvider` pelo mercado persistido, consultar o ticker canônico uma única vez, validar ticker, moeda, valor e timestamp e persistir somente `cotacaoAtual` e `dataHoraCotacao` junto da observação histórica correspondente. `BRASIL` MUST continuar usando brapi e `ESTADOS_UNIDOS` MUST continuar usando Twelve Data, sem `symbol_search` para a cotação americana.

Cada sucesso MUST preservar a atomicidade existente entre a cotação atual e seu histórico. A falha de uma ação durante o refresh de carteira MUST NOT desfazer a atualização já bem-sucedida de outra ação e MUST NOT expor detalhes internos do provider.

#### Scenario: Ativo brasileiro no refresh

- **WHEN** o refresh explícito de carteira processa uma ação persistida de mercado `BRASIL`
- **THEN** o sistema consulta brapi uma vez pelo ticker persistido e persiste a cotação e a observação histórica válidas

#### Scenario: Ativo americano no refresh

- **WHEN** o refresh explícito de carteira processa uma ação persistida de mercado `ESTADOS_UNIDOS`
- **THEN** o sistema consulta Twelve Data uma vez pelo ticker persistido, sem `symbol_search`, e persiste a cotação e a observação histórica válidas

#### Scenario: Falha de provider no refresh

- **WHEN** um provider falha ou devolve dados inválidos para uma ação do refresh
- **THEN** a ação não é alterada nem recebe nova observação histórica, e o refresh continua os demais ativos elegíveis
