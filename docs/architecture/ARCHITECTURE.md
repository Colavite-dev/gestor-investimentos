# Arquitetura do Sistema

## Visão geral

O backend Spring Boot segue arquitetura em camadas:

```text
Controller → Service → Repository → PostgreSQL
Service → Port/Provider → Adapter → API externa
```

Controllers recebem e validam DTOs, services aplicam regras e orquestram fluxos, repositories persistem entidades JPA e adapters convertem contratos externos em modelos internos. Entidades não são contratos HTTP.

## Domínios atuais

- **Corretoras:** BrasilAPI, ViaCEP e CVM são consultadas antes da persistência transacional curta em `CorretoraPersistenceService`.
- **Ações e cotações:** brapi e Twelve Data são isoladas por providers/adapters; cadastro/atualização e histórico de cotação persistem atomicamente por `AcaoQuoteUpdatePersistenceService`.
- **Carteiras e operações:** operações relacionam Carteira e Acao; o serviço de posições calcula quantidade líquida, preço médio e resumo sem ocultar operações inválidas.

## Transações e persistência

PostgreSQL é o banco principal. Flyway controla V1–V6 e Hibernate usa `ddl-auto=validate`. Chamadas HTTP externas não ficam dentro de transações de escrita. A unicidade de CNPJ e `(ticker, mercado)` é protegida no banco; a regra de não permitir venda a descoberto é validada antes de persistir e também considera operações retroativas.

## Integrações externas

Os providers reais são BrasilAPI (CNPJ), ViaCEP (CEP), CVM (participantes intermediários), brapi (ações BR) e Twelve Data (ações EUA). Detalhes operacionais estão em [Integrações externas](../integrations/external-apis.md).

## Testes

Testes normais usam H2 em memória, Flyway e mocks/doubles para integrações. O teste PostgreSQL e os testes reais de providers são opt-in; o fluxo normal não depende de Docker ou internet.
