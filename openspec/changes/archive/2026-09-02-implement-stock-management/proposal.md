## Why

O sistema já valida corretoras, mas ainda não possui o cadastro mestre de ativos exigido pelo escopo acadêmico. A criação desse módulo estabelece uma base persistente e consistente para ações brasileiras e americanas antes das integrações reais de resolução de ticker e cotação.

## What Changes

- Adiciona o cadastro mestre de Ações com ticker, empresa, mercado, moeda, cotação atual e data/hora da cotação.
- Define a identidade lógica por `(ticker, mercado)`, evitando colisões entre ativos de mercados diferentes.
- Disponibiliza cadastro, listagem e consultas por ID e ticker, com DTOs, validação, tratamento de erros e testes automatizados.
- Cria migration Flyway para a tabela de ações, com restrições de domínio e unicidade compatíveis com PostgreSQL e H2.
- Define um contrato transitório de cadastro manual para dados ainda dependentes de integrações futuras, sem simular providers de ticker ou cotação.
- Reserva a atualização real de cotação e as integrações BR/EUA para changes posteriores; não expõe `PUT /acoes/{id}/atualizar-cotacao` sem comportamento real.

## Capabilities

### New Capabilities

- `stock-management`: cadastro mestre persistente de ações dos mercados Brasil e Estados Unidos.

### Modified Capabilities

- Nenhuma.

## Impact

- Novas classes de entidade, enums, DTOs, mapper, repository, service, controller, validações e exceções do módulo de ações.
- Nova migration Flyway e testes com H2 para persistência, regras de negócio e contrato HTTP.
- A arquitetura futura poderá incluir `TickerResolver` e `QuoteProvider` sem alterar o domínio ou o contrato de leitura desta capability.
