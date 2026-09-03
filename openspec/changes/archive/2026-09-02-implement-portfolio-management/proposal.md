## Why

O núcleo obrigatório de corretoras e ações está concluído, mas o produto ainda não oferece uma forma de organizar ativos em carteiras. Esta mudança inicia a evolução de produto prevista no PRD com um cadastro simples e persistente de carteiras, sem antecipar operações ou cálculos de patrimônio.

## What Changes

- Criar o cadastro mestre de carteiras do usuário no contexto atual de aplicação de usuário único.
- Expor criação, listagem e consulta de carteiras por ID.
- Validar nome obrigatório e descrição opcional, mantendo DTOs separados da entidade.
- Persistir carteiras em PostgreSQL por migration Flyway nova, sem alterar migrations existentes.
- Padronizar erros HTTP para requisições inválidas e carteira não encontrada.
- Deixar operações de compra/venda, vínculos com ativos e cálculos para mudanças posteriores.

## Capabilities

### New Capabilities

- `portfolio-management`: cadastro, listagem e consulta de carteiras persistidas.

### Modified Capabilities

Nenhuma capacidade estável existente terá seus requisitos alterados nesta mudança.

## Impact

- Novos componentes Java em entity, DTO, mapper, repository, service, controller e exceptions.
- Nova migration `V3__create_carteiras.sql` para a tabela de carteiras.
- Novos testes unitários, de controller e de persistência usando o padrão existente.
- README poderá documentar os endpoints de carteira.
- Nenhuma alteração em corretoras, ações, integrações externas ou autenticação.
