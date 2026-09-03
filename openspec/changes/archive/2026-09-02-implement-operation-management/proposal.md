## Why

Carteiras já podem ser cadastradas, mas ainda não registram compras e vendas que expliquem sua composição. Esta mudança cria o registro transacional mínimo para ligar uma carteira a um ativo mestre e permitir cálculos de posição em evoluções posteriores.

## What Changes

- Criar o registro persistente de operações de compra e venda.
- Associar cada operação a uma carteira e a uma ação previamente cadastradas.
- Validar tipo, quantidade, preço unitário e data da operação.
- Permitir cadastro e consulta de operações, incluindo listagem por carteira.
- Persistir com migration Flyway nova, sem modificar versões já aplicadas.
- Manter cálculos de posição, preço médio, patrimônio, autenticação e dashboard fora do escopo.

## Capabilities

### New Capabilities

- `operation-management`: registro e consulta de operações de compra e venda associadas a carteiras e ações.

### Modified Capabilities

- `portfolio-management`: permitir que operações futuras sejam associadas à carteira, sem alterar seus endpoints de cadastro e consulta.

## Impact

- Novos componentes Java de entidade, DTO, mapper, repository, service, controller e exceções.
- Nova migration `V4__create_operacoes.sql` com chaves estrangeiras para `carteiras` e `acoes`.
- Delta na especificação de carteiras para refletir a associação transacional.
- Testes unitários, de controller e persistência sem dependências externas.
- Nenhuma integração externa ou alteração nas funcionalidades de corretoras e cotações.
