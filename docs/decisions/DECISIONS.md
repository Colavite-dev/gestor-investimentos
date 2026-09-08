# Decisões do Projeto

## Base técnica

- Projeto individual, com Spec-Driven Development e OpenSpec.
- PostgreSQL é o banco principal; H2 é usado em testes automatizados.
- Docker Compose executa o PostgreSQL localmente e Flyway versiona o schema.
- Hibernate usa `ddl-auto=validate`; não cria ou atualiza schema de produção.
- Serviços externos são isolados por ports/providers e adapters.

## Domínio e integrações

- O escopo inicial de ações é Brasil e Estados Unidos.
- BrasilAPI, ViaCEP e CVM validam/enriquecem Corretoras; brapi e Twelve Data fornecem dados de ações.
- Cotação atual e histórico são gravados atomicamente após a consulta externa, em transação curta.
- Corretora consulta fontes externas antes da escrita; `CorretoraPersistenceService` contém a transação curta e `uk_corretoras_cnpj` é a garantia final de unicidade.
- Não há venda a descoberto: o saldo cronológico por Carteira/Ação não pode ficar negativo, inclusive após operação retroativa.
- Valores financeiros usam `BigDecimal` e `NUMERIC(19,8)`; quantidade e preço aceitam até 11 dígitos inteiros e 8 fracionários.

## Identidade da ação

O projeto usa `(ticker, mercado)` como identidade lógica e persistida, protegida por `uk_acoes_ticker_mercado`.

**Pendência acadêmica:** o enunciado literalmente menciona não permitir duas ações com o mesmo ticker. Esta decisão arquitetural está pendente de confirmação acadêmica e não representa aprovação do professor.

## Bancos

PostgreSQL é usado em runtime e H2 nos testes; MySQL não é utilizado.

**Pendência acadêmica:** o enunciado cita H2, MySQL e PostgreSQL. A interpretação atual está pendente de confirmação acadêmica; não há evidência de autorização do professor para excluir MySQL.
