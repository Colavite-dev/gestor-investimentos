# Decisões do Projeto

## Base técnica

- Projeto executado individualmente, com Spec-Driven Development e OpenSpec.
- PostgreSQL é o banco principal; H2 é usado em testes automatizados.
- Docker Compose executa PostgreSQL localmente; Flyway versiona V1–V8 e Hibernate usa `ddl-auto=validate`.
- Serviços externos são isolados por ports/providers e adapters.

## Acesso e propriedade de dados

- A API usa JWT Bearer stateless com BCrypt para credenciais persistidas.
- Registros públicos geram somente `USER`; `ADMIN` protege os endpoints `/admin/**`.
- `Usuario 1:N Carteira`; V8 tornou o owner obrigatório e a unicidade de nome é por usuário.
- Dados financeiros são privados: a identidade é extraída do JWT/SecurityContext, não de um `usuarioId` enviado pelo cliente. Recursos de outro usuário retornam `404`, e `ADMIN` não possui acesso implícito a eles.
- Ações e dados de mercado são globais e compartilhados.

## Domínio e integrações

- O escopo de ativos é Brasil e Estados Unidos. BrasilAPI, ViaCEP e CVM atendem corretoras; brapi e Twelve Data fornecem dados de ativos.
- Cotação atual e histórico são gravados atomicamente após consulta externa, em transação curta.
- Não há venda a descoberto: o saldo cronológico por carteira/ação não pode ficar negativo, inclusive após operação retroativa.
- Valores financeiros usam `BigDecimal` e `NUMERIC(19,8)`.

## Ambiguidades acadêmicas registradas

O sistema usa `(ticker, mercado)` como identidade lógica/persistida, enquanto o enunciado menciona ticker duplicado literalmente. PostgreSQL é usado em runtime, H2 nos testes e MySQL não é utilizado. O projeto foi executado individualmente. Nenhum desses fatos representa autorização do professor; as três interpretações permanecem pendentes de orientação acadêmica.
