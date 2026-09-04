## 1. Fronteira de escrita da corretora

- [x] 1.1 Criar o componente de persistência de corretora com método público `@Transactional` e verificar que ele depende somente do repositório e recebe uma entidade já validada.
- [x] 1.2 Remover a transação de escrita de `CorretoraService.cadastrar`, preservar sua orquestração de CNPJ, CEP e CVM e verificar que nenhuma chamada externa é feita pelo componente transacional.
- [x] 1.3 Delegar o salvamento ao componente transacional com `saveAndFlush` e verificar que o cadastro válido ainda responde `201 Created` com `validadaNaCvm=true`.

## 2. Unicidade e rollback controlados

- [x] 2.1 Implementar a tradução estrita de violação de chave duplicada SQLState `23505` no caminho de persistência de corretora e verificar que ela produz `CnpjDuplicadoException` somente para colisão de unicidade.
- [x] 2.2 Preservar a propagação de outras falhas de integridade e verificar que não há handler genérico `DataIntegrityViolationException -> 409/422`.
- [x] 2.3 Verificar por teste de integração H2 que uma falha durante `saveAndFlush` faz rollback da transação curta sem deixar corretora parcial persistida.

## 3. Testes da orquestração e contrato HTTP

- [x] 3.1 Atualizar os testes de `CorretoraService` para verificar que falhas de BrasilAPI, ViaCEP, CVM e inelegibilidade não delegam persistência.
- [x] 3.2 Adicionar teste Spring focado que observe que cada port externo é chamado sem transação de escrita ativa e que a persistência é chamada somente depois de todos os dados serem validados.
- [x] 3.3 Adicionar cobertura de colisão concorrente de CNPJ em H2 e verificar que há um único registro persistido e uma `CnpjDuplicadoException`.
- [x] 3.4 Confirmar no controller/handler que duplicidade antecipada e duplicidade por corrida retornam o contrato existente `409 Conflict`, enquanto os erros externos continuam em `422`, `502` ou `503`.

## 4. PostgreSQL opt-in

- [x] 4.1 Modernizar `CorretoraPostgresIT` para mockar CNPJ, CEP e CVM, remover a transação de teste e limpar exclusivamente seus CNPJs de teste; verificar que ele não depende de internet.
- [x] 4.2 Cobrir no PostgreSQL real o commit de cadastro validado, Flyway V1–V6, Hibernate `ddl-auto=validate`, colisão concorrente de CNPJ e rollback de escrita falha.
- [x] 4.3 Manter a execução PostgreSQL explicitamente opt-in e verificar que `mvn verify` normal não exige Docker, banco local ou credenciais externas.

## 5. Validação final

- [x] 5.1 Executar os testes focados de corretora, exceção e persistência H2 e verificar ausência de falhas e erros.
- [x] 5.2 Executar a validação PostgreSQL opt-in quando Docker e datasource local estiverem disponíveis; caso contrário, registrar a limitação ambiental sem alegar sucesso.
- [x] 5.3 Executar `mvn verify`, validação OpenSpec strict e `git diff --check`; verificar que não há migration nova nem mudança fora do escopo.
