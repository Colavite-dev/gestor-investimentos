## Why

O sistema atualmente mantém somente a última cotação de cada ação, impedindo consultas da evolução de preços e a demonstração do histórico exigido como diferencial do projeto. Esta mudança cria um histórico imutável a partir das cotações reais já obtidas pelos providers existentes.

## What Changes

- Criar persistência append-only para observações históricas de cotação vinculadas a uma ação.
- Registrar uma observação quando o cadastro ou a atualização de cotação obtiver dados externos válidos.
- Expor consulta REST do histórico de uma ação em ordem cronológica determinística.
- Permitir filtros simples por intervalo de data/hora sem alterar os dados registrados.
- Manter o histórico separado da cotação atual e sem chamadas externas adicionais.
- Preservar moedas e dados cadastrais da ação; não implementar séries intraday, agregações ou gráficos.

## Capabilities

### New Capabilities

- `stock-quote-history`: persistência e consulta de observações históricas de cotação.

### Modified Capabilities

- `stock-management`: registrar que cadastros e atualizações de cotação bem-sucedidos também originam observação histórica, sem alterar o contrato da cotação atual.

## Impact

- Nova entidade, repository, DTOs, service e controller de histórico.
- Nova migration Flyway para a tabela de histórico, sem alterar migrations existentes.
- Ajuste mínimo no fluxo atual de cadastro/atualização de ações para gravar o snapshot validado na mesma operação de persistência.
- Novos testes unitários, de integração e de contrato HTTP; nenhuma chamada externa adicional.
- README atualizado com o endpoint e suas limitações.
