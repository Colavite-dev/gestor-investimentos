## 1. Pré-checagem de dados legados

- [x] 1.1 Antes de criar ou aplicar V9, executar somente uma consulta de inventário em `corretoras` que registre quantidade, IDs, CNPJs, razão social e data de cadastro; verificar que nenhum comando de escrita, Docker ou limpeza foi executado.
- [x] 1.2 **GATE — se o inventário encontrar qualquer corretora legada, PARAR antes de criar ou aplicar V9, apresentar o inventário ao usuário e aguardar autorização explícita para qualquer remoção manual fora da migration; verificar que não houve DELETE automático, atribuição de owner, backfill, reset de banco ou remoção de volume.**
- [x] 1.3 Se o usuário autorizar explicitamente a remoção de dados locais descartáveis, remover manualmente somente os IDs de corretora previamente inventariados e confirmar em leitura posterior que `corretoras` está vazia, preservando usuários, carteiras, operações, ações, cotações, histórico, banco e volume.

## 2. Schema e migration V9

- [x] 2.1 Criar `V9__add_corretora_ownership.sql`, sem modificar V1–V8, adicionando `corretoras.usuario_id BIGINT NOT NULL` sem default, owner fixo ou backfill; verificar o diff da migration para confirmar a ausência de `UPDATE` e `DELETE`.
- [x] 2.2 Na V9, criar `fk_corretoras_usuario` para `usuarios(id)`, remover `uk_corretoras_cnpj`, criar `uk_corretoras_usuario_cnpj UNIQUE (usuario_id, cnpj)` e `idx_corretoras_usuario_id (usuario_id, id)` nessa ordem; verificar os nomes e a ordem no SQL antes de executar Flyway.
- [x] 2.3 Criar ou adaptar teste de migration para comprovar V9 em banco vazio e falha transacional com corretora legada sem owner, verificando que a linha legada e o schema anterior permanecem intactos após a falha.

## 3. Modelo e persistência owner-scoped

- [x] 3.1 Atualizar `Corretora` para ter relacionamento `ManyToOne(fetch = LAZY, optional = false)` com `Usuario` por `usuario_id`, exigindo owner na criação; verificar que nenhum DTO público expõe ou aceita `usuarioId`.
- [x] 3.2 Substituir no `CorretoraRepository` os acessos globais usados pelo fluxo HTTP por existência, listagem, busca por ID e busca por CNPJ delimitadas por `usuarioId`; verificar em teste de repositório que registros de outro owner nunca são retornados.
- [x] 3.3 Adaptar o mapper ou a construção da entidade para associar o `Usuario` resolvido sem modificar `CorretoraRequest` ou `CorretoraResponse`; verificar que os campos cadastrais e `validadaNaCvm` continuam mapeados como antes.

## 4. Serviço e garantia de unicidade

- [x] 4.1 Alterar `CorretoraService` para receber `usuarioId` explicitamente, verificar duplicidade somente por `(usuario_id, cnpj)` e resolver o `Usuario` autenticado; verificar que o mesmo CNPJ é permitido para owners diferentes e rejeitado para o mesmo owner.
- [x] 4.2 Preservar BrasilAPI, ViaCEP, reconciliação de endereço e CVM fora da transação curta de escrita, associando o `Usuario` somente à corretora validada antes de persistir; verificar que status `EM FUNCIONAMENTO NORMAL` com categoria compatível continua elegível e falhas externas continuam sem persistência.
- [x] 4.3 Atualizar `CorretoraPersistenceService` para reconhecer `uk_corretoras_usuario_cnpj` e traduzir somente sua colisão de chave duplicada para `CnpjDuplicadoException`; verificar em teste que outra violação de integridade continua sendo propagada.

## 5. Controller e autorização por recurso

- [x] 5.1 Adaptar `POST /corretoras`, `GET /corretoras`, `GET /corretoras/{id}` e `GET /corretoras/cnpj/{cnpj}` para extrair `AuthenticatedUserId.from(authentication)` e passá-lo ao serviço; verificar que URLs, DTOs, status de sucesso e payloads permanecem compatíveis.
- [x] 5.2 Garantir que busca por ID ou CNPJ de outro usuário use a mesma resposta `404 Not Found` de recurso inexistente, sem busca global seguida de filtro em memória; verificar a ausência de dados e de indicação de ownership na resposta.

## 6. Testes de isolamento, concorrência e regressão

- [x] 6.1 Criar teste de controller autenticado com USER_A criando X e vendo X na listagem, enquanto USER_B recebe lista vazia, `404` por ID e `404` por CNPJ; verificar também que o payload com identidade forjada permanece rejeitado pelo contrato estrito.
- [x] 6.2 Criar teste de controller/service em que USER_B cadastra o mesmo CNPJ X com sucesso e USER_A recebe `409` ao repetir X; verificar que cada conta lê somente seu próprio registro.
- [x] 6.3 Adaptar o teste de concorrência para o mesmo usuário e mesmo CNPJ, verificando exatamente uma criação e uma `CnpjDuplicadoException`/`409`.
- [x] 6.4 Adicionar cenário concorrente de dois usuários para o mesmo CNPJ, verificando duas criações independentes e nenhuma colisão de unicidade entre owners.
- [x] 6.5 Atualizar os testes unitários de serviço, mapper e persistência para owners explícitos, preservando cobertura de BrasilAPI, ViaCEP, CVM, indisponibilidade externa e `EM FUNCIONAMENTO NORMAL` elegível com categoria compatível.
- [x] 6.6 Atualizar o teste opt-in PostgreSQL/Flyway para schema V9, FK, constraint composta, rollback de persistência e isolamento por owner; verificar a execução somente quando `RUN_POSTGRES_IT=true`, sem apagar dados ou volumes.
- [x] 6.7 Executar primeiro os testes focados de Corretora, migration e autenticação, depois `mvnw verify`; registrar claramente qualquer limitação conhecida do Maven Wrapper/Codex sem alterar o projeto para contorná-la, e confirmar que os testes de Carteira e Operação permanecem verdes.

## 7. Validação manual e OpenSpec

- [x] 7.1 Executar validação manual com Conta A cadastrando e visualizando uma corretora, Conta B não vendo o registro de A, Conta B cadastrando o mesmo CNPJ e vendo somente o seu registro, e Conta A continuando a ver somente o seu; verificar os quatro resultados com Bearer tokens distintos.
- [x] 7.2 Executar `openspec validate enforce-broker-user-isolation --strict`, conferir o diff e marcar cada tarefa concluída somente após sua execução real; verificar que a change não seja arquivada antes dos testes finais aprovados.
