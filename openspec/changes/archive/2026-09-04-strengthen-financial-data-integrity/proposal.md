## Why

As cotações usam hoje duas capacidades incompatíveis: `acoes.cotacao_atual` e seu mapeamento JPA aceitam quatro casas decimais, enquanto o histórico e os preços de operação aceitam oito. Em PostgreSQL, isso pode arredondar a cotação atual e preservar outro valor no histórico; além disso, valores numéricos fora da capacidade das colunas chegam à persistência sem validação de entrada e podem resultar em erro interno.

## What Changes

- Definir `NUMERIC(19,8)` como a representação única de cotações e preços unitários, alinhando Java/JPA, a coluna da cotação atual e o histórico já existente.
- Planejar uma migration V6 nova para ampliar `acoes.cotacao_atual` e adicionar a proteção de banco que falta para cotação atual estritamente positiva, sem modificar V1--V5.
- Validar, antes da persistência, quantidade e preço unitário de operações quanto a positividade e capacidade decimal; payload inválido deve resultar em `400 Bad Request`.
- Validar que cotações recebidas de providers são positivas e cabem na precisão contratada; resposta externa incompatível deve continuar sendo tratada como dado externo inválido, sem persistência parcial.
- Preservar a aceitação atual de quantidade fracionária até oito casas, a identidade `(ticker, mercado)`, a normalização de ticker e o vínculo derivado entre mercado e moeda.
- Cobrir a migração, a precisão de oito casas, as fronteiras inválidas e a consistência entre cotação atual e histórico em H2 e PostgreSQL real.

## Capabilities

### New Capabilities

_Nenhuma._

### Modified Capabilities

- `stock-management`: a cotação externa aceita no cadastro e na atualização passa a exigir precisão decimal compatível com a representação persistida única.
- `stock-quote-history`: cotação atual e observação histórica passam a preservar a mesma precisão monetária de até oito casas, sem arredondamento divergente.
- `operation-management`: quantidade e preço unitário recebidos pelo endpoint passam a ser rejeitados com `400 Bad Request` quando não couberem na precisão decimal persistida.

## Impact

- Afeta as entidades `Acao`, `CotacaoHistorica` e `Operacao`, os DTOs de operação, a validação de dados de cotação vindos dos providers, a migration Flyway V6 e testes de controller, service, repository e integração.
- Não altera endpoints, contratos de moeda/mercado, histórico transacional, carteira, providers externos, concorrência de cotações ou migrations V1--V5.
- Exige validação da migration e do `ddl-auto=validate` no perfil H2 e contra PostgreSQL via Docker, pois semânticas de `NUMERIC`, `CHECK` e `ALTER TABLE` são relevantes ao banco principal.
