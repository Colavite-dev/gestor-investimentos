## Why

As carteiras e operações já estão persistidas, mas o sistema ainda não transforma esses fatos em uma visão útil da posição do investidor. Esta mudança adiciona cálculos determinísticos de quantidade, preço médio, valor investido e patrimônio atual sem alterar operações históricas.

## What Changes

- Calcular posições agregadas por ação dentro de uma carteira a partir de compras e vendas.
- Calcular quantidade líquida, preço médio de custo, valor investido, patrimônio atual e lucro/prejuízo não realizado.
- Expor consulta de posições e resumo da carteira.
- Usar a cotação atual já persistida em cada ação, sem chamadas externas novas.
- Tratar carteiras sem operações e ativos totalmente vendidos de forma determinística.
- Manter operações, cotações, autenticação, histórico e dashboard fora do escopo.

## Capabilities

### New Capabilities

- `portfolio-position-calculation`: cálculo e consulta de posições e resumo financeiro derivados das operações e cotações persistidas.

### Modified Capabilities

- `portfolio-management`: adicionar consultas derivadas de posições sem alterar o cadastro básico de carteiras.

## Impact

- Novos serviços de cálculo, DTOs de posição/resumo, endpoints REST e testes.
- Reutilização de `OperacaoRepository` e `Acao` sem nova integração externa.
- Nenhuma migration prevista; os dados necessários já existem.
- Documentação da API atualizada com as novas consultas.
