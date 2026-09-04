## 1. Schema e mapeamento financeiro

- [x] 1.1 Auditar `acoes.cotacao_atual` existente para valores não positivos e registrar resultado antes de qualquer DDL, verificando que nenhum dado será corrigido silenciosamente.
- [x] 1.2 Criar `V6__align_financial_numeric_precision.sql` para ampliar `acoes.cotacao_atual` para `NUMERIC(19,8)` e adicionar a constraint positiva nomeada, verificando que V1--V5 permanecem inalteradas.
- [x] 1.3 Alinhar o mapeamento JPA de `Acao` a `precision = 19, scale = 8` e confirmar que `CotacaoHistorica` e `Operacao` já usam a mesma capacidade.

## 2. Validação nas fronteiras

- [x] 2.1 Adicionar ao DTO de operação validação de positividade e `@Digits(integer = 11, fraction = 8)` para quantidade e preço unitário, verificando resposta `400 Bad Request` antes de qualquer save para valores inválidos.
- [x] 2.2 Estender a validação de dados externos de cotação no cadastro e na atualização para recusar valores não positivos ou fora de `NUMERIC(19,8)`, verificando `502 Bad Gateway` sem alteração persistida.
- [x] 2.3 Confirmar que o tratamento centralizado existente já converte falhas de Bean Validation em `400` e que nenhuma tradução genérica de `DataIntegrityViolationException` é introduzida; adicionar ajuste somente se um teste de contrato demonstrar necessidade.

## 3. Cobertura focada

- [x] 3.1 Adicionar testes de service e integração para cotações com quatro e oito casas, comprovando valor idêntico entre `Acao` e `CotacaoHistorica` no cadastro e na atualização.
- [x] 3.2 Adicionar testes para cotação externa com escala ou parte inteira excessiva, comprovando `502` e ausência de cadastro ou atualização parcial.
- [x] 3.3 Adicionar testes de controller para preço unitário e quantidade válidos, fracionários válidos, não positivos e fora de `NUMERIC(19,8)`, comprovando `400` e ausência de operação persistida nas rejeições.
- [x] 3.4 Adicionar testes de repository/migration para a constraint positiva da cotação atual e para valores-limite de precisão, verificando que a capacidade persistida corresponde ao contrato.

## 4. Validação de bancos e regressão

- [x] 4.1 Executar os testes focados no perfil H2 e confirmar Flyway V6 mais Hibernate `ddl-auto=validate` com o mapeamento alinhado.
- [x] 4.2 Validar V1--V6 em PostgreSQL real via Docker, verificando `ALTER TABLE`, `NUMERIC(19,8)`, constraint positiva e persistência de valores de fronteira sem depender somente de H2.
- [x] 4.3 Executar `mvn verify` e confirmar que não há regressão na suíte existente nem nos testes adicionados.
- [x] 4.4 Executar `openspec validate strengthen-financial-data-integrity --strict` e comparar a implementação final com as três delta specs antes de solicitar SYNC/ARCHIVE.
