## 1. Persistência de Corretora

- [x] 1.1 Criar `V1__create_corretoras.sql` com todas as colunas, constraints e índice único de CNPJ definidos no design, e verificar que Flyway aplica a migration no perfil H2 sem Hibernate criar schema.
- [x] 1.2 Implementar a entidade `Corretora` com mapeamento equivalente à migration, geração de ID, `dataCadastro` e `validadaNaCvm=false`, e verificar pelo startup de teste que `ddl-auto=validate` aceita o mapeamento.
- [x] 1.3 Implementar `CorretoraRepository` com busca/existência por CNPJ e listagem ordenável, e verificar por teste de repository a persistência, unicidade e consultas.
  - Evidência: Flyway aplicou `V1` no H2, Hibernate concluiu `ddl-auto=validate` e os 3 testes de repository passaram, incluindo a constraint única.

## 2. Contratos e validações

- [x] 2.1 Implementar normalização e Bean Validation de CNPJ, CEP, UF, e-mail, campos obrigatórios e tamanhos, e verificar por testes casos válidos, mascarados, inválidos e sequências repetidas.
- [x] 2.2 Criar `CorretoraRequest`, `CorretoraResponse` e `CorretoraMapper`, sem expor a entidade JPA nem aceitar `id`, `dataCadastro` ou `validadaNaCvm` no request, e verificar por testes que os dados normalizados e opcionais são mapeados corretamente.
  - Evidência: os testes do validator, mapper e API cobriram máscara, dígitos verificadores, normalização, opcionais e erros por campo.

## 3. Regras de negócio e API REST

- [x] 3.1 Implementar `CorretoraService` para cadastrar, listar por ID crescente e consultar por ID/CNPJ, incluindo prevenção de duplicidade e estado CVM falso, e verificar por testes unitários os fluxos de sucesso e erro.
- [x] 3.2 Implementar exceções de recurso inexistente e CNPJ duplicado, `ApiErrorResponse` e `GlobalExceptionHandler`, e verificar que validação retorna 400, ausência retorna 404 e duplicidade retorna 409 sem detalhes internos.
- [x] 3.3 Implementar `CorretoraController` com `POST /corretoras`, `GET /corretoras`, `GET /corretoras/{id}` e `GET /corretoras/cnpj/{cnpj}`, e verificar por testes HTTP status, `Location`, JSON, ordenação e validação de parâmetros.
  - Evidência: 6 testes unitários de service e 6 testes HTTP passaram, cobrindo sucesso, ordenação, `Location`, 400, 404 e 409.

## 4. Testes e documentação

- [x] 4.1 Habilitar Flyway no perfil H2 de teste mantendo Hibernate em `validate`, atualizar `contextLoads` se necessário e verificar que a suíte não depende de PostgreSQL ou Docker.
- [x] 4.2 Adicionar testes automatizados de validator, service, repository e controller cobrindo os cenários da spec, e verificar que `mvnw.cmd test` passa sem falhas.
- [x] 4.3 Documentar no `README.md` os endpoints, payloads, respostas, erros e a limitação das integrações de CNPJ/CEP/CVM, deixando explícito que `validadaNaCvm=false` não conclui a validação acadêmica externa.
  - Evidência: `mvn test` passou com 20 testes, 0 falhas e 0 erros usando H2/Flyway, e o README documenta contratos e limitações.

## 5. Validação integrada

- [x] 5.1 Executar `mvnw.cmd verify` e registrar evidência de build, testes e geração do JAR sem falhas.
  - Evidência: Maven 3.9.16 concluiu `verify` com `BUILD SUCCESS`, 20 testes sem falhas/erros e geração de `target/gestor-investimento-0.0.1-SNAPSHOT.jar`.
- [x] 5.2 Validar a aplicação e a migration `V1` contra PostgreSQL 17 local sem alterar ou apagar dados existentes, confirmando Flyway, Hibernate e os quatro endpoints; se Docker não estiver disponível no ambiente, manter esta tarefa pendente e registrar os comandos/evidências necessários.
  - Evidência: PostgreSQL 17.11 permaneceu saudável em `localhost:5433`; a aplicação iniciou, Flyway aplicou/validou `V1`, Hibernate validou o schema e `CorretoraPostgresIT` passou com cadastro, listagem, consultas por ID/CNPJ e duplicidade, usando rollback transacional.
- [x] 5.3 Executar `openspec validate implement-broker-management --strict`, corrigir divergências entre proposal/spec/design/tasks e implementação, e manter a change ativa para revisão sem arquivá-la.
  - Evidência: `openspec validate implement-broker-management --strict` concluiu com `Change 'implement-broker-management' is valid`; a change permanece ativa e não foi arquivada.
