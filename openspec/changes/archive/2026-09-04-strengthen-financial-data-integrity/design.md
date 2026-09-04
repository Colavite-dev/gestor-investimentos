## Context

Veja a motivação em `proposal.md`. A migration V2 e a entidade `Acao` usam `NUMERIC(19,4)`, enquanto V4, V5, `Operacao` e `CotacaoHistorica` usam `NUMERIC(19,8)`. Todos os valores são `BigDecimal`, mas JPA não transforma `precision` e `scale` em validação de payload; o `OperacaoRequest` só exige mínimo positivo. PostgreSQL pode arredondar o valor ajustado à escala da coluna, criando divergência entre cotação atual e histórico. O perfil H2 usa Flyway e Hibernate `validate`; produção usa PostgreSQL, também com Flyway e `validate`.

## Goals / Non-Goals

**Goals:**

- Unificar cotações e preços unitários em `NUMERIC(19,8)` e impedir que uma cotação atual arredondada divirja do histórico.
- Rejeitar na borda HTTP números de operação não representáveis com `400`.
- Rejeitar dados de cotação externos não representáveis como resposta inválida do provider (`502`) antes de iniciar a persistência.
- Acrescentar à cotação atual a defesa de banco de valor estritamente positivo e validar a V6 contra os dois bancos relevantes.

**Non-Goals:**

- Não mudar V1--V5, endpoints, arredondamento de preço médio, regras de carteira/posição, transações de cotação, providers ou concorrência de timestamps.
- Não criar mercados ou moedas, não tornar ticker globalmente único e não impor nova sintaxe de ticker por `CHECK`.
- Não adicionar `CHECK` relacional de mercado/moeda nesta change, nem revisar genericamente `DataIntegrityViolationException` como erro HTTP de cliente.

## Decisions

### Precisão canônica para valores financeiros persistidos

Preço de cotação atual, cotação histórica, preço unitário e quantidade de operação usarão `BigDecimal` mapeado para `NUMERIC(19,8)`: 11 dígitos inteiros e 8 fracionários. Essa escolha já é a capacidade de V4/V5 e comporta preços de ações BR e US recebidos por providers sem reduzir a precisão histórica. A V6 ampliará somente `acoes.cotacao_atual`; `Operacao` e `CotacaoHistorica` já estão alinhadas.

Alternativas descartadas: reduzir tudo para quatro casas perderia precisão já suportada pelo histórico; manter escalas distintas conserva risco de valores diferentes; usar precisão maior sem evidência alteraria mais o contrato e a capacidade atual das operações.

### Regras de entrada e origem externa

`OperacaoRequest` receberá validações declarativas de positividade e `@Digits(integer = 11, fraction = 8)` para quantidade e preço unitário. Isso mantém quantidade fracionária, que é a intenção inferida do `BigDecimal`, da escala oito e da ausência de restrição a inteiros em V4 e nas specs. Não será criada regra de lote inteiro.

Como a cotação não é body do cliente, `@Digits` não resolve a resposta de provider. `AcaoService` validará positividade e capacidade decimal antes de delegar a persistência, tanto no cadastro quanto na atualização. Valor externo fora da capacidade seguirá o contrato já existente de resposta externa inválida (`502`). Não haverá conversão, truncamento ou arredondamento silencioso.

### Fronteira de persistência e constraints

Uma V6 nova, sugerida como `V6__align_financial_numeric_precision.sql`, alterará `acoes.cotacao_atual` para `NUMERIC(19,8)` e adicionará `CHECK (cotacao_atual > 0)`. As constraints positivas de operações e histórico já existem em V4/V5; duplicá-las não adiciona proteção. A aplicação continua prevenindo erros de usuário com Bean Validation em vez de mapear genericamente qualquer `DataIntegrityViolationException` para `400` ou `422`, pois isso poderia esconder defeitos internos ou falhas de concorrência.

Mercado e moeda já possuem checks individuais em V2 e a entidade deriva moeda de `Mercado`. Um `CHECK` de pares aumentaria o acoplamento de schema aos enums e exigiria atualização coordenada a cada mercado futuro; por não ser necessário para corrigir precisão ou entrada monetária, fica como dívida técnica futura. A normalização de ticker continua no serviço e a unicidade composta `(ticker, mercado)` em V2; não será adicionada restrição de uppercase/trim nesta change pelo mesmo motivo de compatibilidade e escopo.

### Compatibilidade e testes de banco

O `ALTER` de `NUMERIC(19,4)` para `NUMERIC(19,8)` amplia a escala e não perde precisão dos dados existentes. Antes de aplicar a nova constraint, a execução deve auditar se há `cotacao_atual <= 0`; se existir, a migration falhará de forma segura e exigirá correção deliberada, sem DML oculto. O rollback conceitual é remover a constraint e reverter a coluna para escala quatro, mas esse retorno pode arredondar valores novos com cinco a oito casas e por isso não é uma reversão automática segura.

Os testes H2 verificarão que Flyway V6 e Hibernate `validate` iniciam com o mapeamento alinhado. Como `ALTER TABLE`, `NUMERIC(p,s)` e `CHECK` podem diferir entre H2 e PostgreSQL, haverá validação real opt-in com Docker/PostgreSQL: aplicar V1--V6, confirmar schema/constraint e persistir valores-limite e inválidos. Não serão criadas migrations específicas por banco salvo incompatibilidade comprovada; nesse caso a decisão será registrada antes de ampliar o escopo.

## Risks / Trade-offs

- [Dados existentes com cotação atual não positiva] → auditar antes da V6; a constraint deve bloquear a evolução em vez de alterar dados silenciosamente.
- [Provider retornar precisão acima do contrato] → responder `502` sem persistir e cobrir cadastro e atualização com testes.
- [Diferença H2/PostgreSQL] → manter H2 na suíte rápida e executar validação real PostgreSQL/Docker antes de concluir APPLY.
- [Cálculos de posição produzirem mais de oito casas internamente] → a change não persiste esses resultados; preservar comportamento atual de preço médio e tratar uma política de arredondamento de relatórios como tema separado caso necessário.

## Migration Plan

1. Auditar os dados existentes de `acoes` para confirmar que `cotacao_atual` é positiva.
2. Adicionar V6 sem alterar V1--V5, ampliando a escala da cotação atual e incluindo a constraint positiva nomeada.
3. Alinhar o mapeamento JPA e aplicar as validações nas fronteiras descritas nas delta specs.
4. Executar Flyway/Hibernate em H2, depois validar a mesma cadeia em PostgreSQL Docker e por fim `mvn verify`.
5. Se a validação real falhar, não tratar a mudança como concluída; corrigir somente a migration/mapeamento aprovado ou reportar incompatibilidade.
