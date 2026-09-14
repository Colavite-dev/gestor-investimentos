## Context

`Usuario` e autenticação JWT já existem, e o `sub` do token é validado como `Long` e colocado em `Authentication.getName()`. Entretanto, `Carteira` não referencia `Usuario`; V3 criou `carteiras` sem `usuario_id`, e a constraint `uk_carteiras_nome_normalizado` é global. `Operacao` referencia `Carteira` e `Acao`, portanto a cadeia de ownership pode ser obtida sem duplicar usuário em `operacoes`.

Os caminhos vulneráveis estão concentrados nos serviços atuais:

- `CarteiraService.listar` usa listagem global; `buscarPorId` usa ID global; cadastro não recebe owner.
- `CarteiraPosicaoService` valida somente `existsById` e consulta operações por `carteiraId`.
- `OperacaoService` trava carteira por ID global, consulta operação por ID global e lista por `carteiraId` global.
- Todos os endpoints são autenticados pela `SecurityFilterChain`, mas autenticação de rota não constitui autorização por objeto.
- O frontend mantém somente o token no `sessionStorage`; dados financeiros ficam em estado React e normalmente desmontam no logout. Não foi encontrado cache persistente financeiro em `localStorage` ou `sessionStorage`, mas promises em andamento não têm vínculo explícito com a identidade que as iniciou.

## Goals / Non-Goals

**Goals:**

- Aplicar ownership no banco e nas queries que alcançam dados financeiros privados.
- Usar o ID autenticado do JWT como única fonte de identidade para autorização.
- Fazer inexistência e ownership alheio resultarem no mesmo `404`.
- Preservar constraints financeiras, locks e transações atuais.
- Recusar legado sem owner e aplicar a decisão explícita já tomada para descartar somente os registros financeiros locais de teste antes da migration.

**Non-Goals:**

- Tornar `Acao`, catálogo, cotação ou histórico de cotação privados.
- Dar a ADMIN uma visão financeira transversal ou criar novos endpoints administrativos.
- Adicionar `usuario_id` redundante em `operacoes`.
- Alterar claims, assinatura ou armazenamento do JWT.
- Alterar payloads de carteira/operação ou regras de preço médio, saldo e valuation.

## Decisions

### 1. Carteira será a raiz de ownership

`Carteira` terá associação obrigatória `ManyToOne` para `Usuario` por `usuario_id NOT NULL`. `Operacao` continuará ligada somente a `Carteira` e `Acao`; seu owner será determinado pela carteira.

Isso evita duplicidade e risco de divergência entre `operacoes.usuario_id` e `carteiras.usuario_id`. A alternativa de adicionar owner também em cada operação foi rejeitada por não melhorar a autorização e criar duas fontes de verdade.

### 2. O ID autenticado será extraído do principal e passado explicitamente ao domínio de aplicação

Controllers autenticados converterão `Authentication.getName()` no ID estável já validado pelo filtro JWT e o passarão aos services. Os DTOs não receberão `usuarioId`.

Passar o ID explicitamente mantém services e testes legíveis e evita dependência oculta do `SecurityContext` dentro de repositories ou entidades. O backend nunca usará username do request ou role enviada pelo navegador para ownership.

### 3. Queries serão scoped pelo owner desde a leitura

`CarteiraRepository` deverá oferecer equivalentes a:

- `findAllByUsuarioIdOrderByIdAsc(usuarioId)`;
- `findByIdAndUsuarioId(id, usuarioId)`;
- `existsByUsuarioIdAndNomeNormalizado(usuarioId, nome)`;
- busca pessimista por `id` e `usuario.id` para o cadastro de operação.

`OperacaoRepository` deverá consultar operação por `id` e `carteira.usuario.id`. Listagens e sequências financeiras continuarão usando o ID da carteira somente depois de a carteira ter sido localizada/locked pelo owner na mesma operação de serviço. Onde uma query composta puder eliminar a leitura alheia diretamente, ela será preferida.

A alternativa de buscar globalmente e comparar owner depois foi rejeitada porque amplia exposição acidental, torna filtros fáceis de esquecer e não atende defesa em profundidade.

### 4. Acesso alheio será indistinguível de inexistência

Os mesmos `CarteiraNotFoundException` e `OperacaoNotFoundException` serão usados quando o ID não existir ou não pertencer ao principal. Não será criado erro “pertence a outro usuário”. Isso preserva o padrão `404` existente e reduz enumeração.

`ADMIN` usa os endpoints comuns sob as mesmas regras de owner. `/admin/users` e `/admin/metrics` permanecem conforme a spec atual; métricas agregadas não concedem acesso aos registros financeiros individuais.

### 5. Unicidade do nome passa de global para por usuário

A constraint global `uk_carteiras_nome_normalizado` será substituída por `UNIQUE (usuario_id, nome_normalizado)`. Assim, contas diferentes podem usar “Principal”, enquanto o mesmo owner continua recebendo `409` para duplicata normalizada. A garantia final continua no banco e o service trata corrida de unicidade.

### 6. V8 não fará backfill nem exclusão automática

Será criada uma nova `V8__add_carteira_ownership.sql`; V1–V7 não serão editadas. Como o banco local foi inventariado e o usuário declarou as duas carteiras e sete operações como dados de teste descartáveis, esses registros serão removidos por ação local explícita e auditável antes da execução da V8. Usuários, ações, cotações, histórico, banco e volume serão preservados.

A migration deverá:

1. adicionar `usuario_id BIGINT NOT NULL` sem default e sem backfill;
2. criar FK para `usuarios(id)`;
3. substituir a unique global pela unique `(usuario_id, nome_normalizado)`;
4. criar somente índice útil para listagem/autorização por `(usuario_id, id)`.

Em banco sem carteiras, a coluna obrigatória é adicionada diretamente. Em qualquer banco que ainda possua carteiras sem owner, `ADD COLUMN ... NOT NULL` sem default falha e a migration transacional reverte integralmente. A migration nunca escolhe owner e nunca apaga dados.

Antes da remoção local, uma segunda consulta read-only confirmará que os alvos continuam sendo exatamente as carteiras IDs 1 e 2 e suas operações. A limpeza será feita na ordem das FKs, exclusivamente em `operacoes` dessas carteiras e depois nessas duas `carteiras`, seguida de leitura que confirme os totais. Nenhum comando amplo contra o banco ou volume será usado.

Foram rejeitados: atribuir ao primeiro usuário, atribuir automaticamente ao `adm`, manter placeholder de owner sem necessidade, deixar owner nullable, apagar o banco/volume ou executar `repair` para mascarar falha.

### 7. Estado frontend será vinculado ao ciclo da identidade

Logout continuará removendo o token e navegará para login, desmontando páginas privadas. Além disso, carregamentos de Dashboard, detalhe e Operações deverão usar cancelamento/epoch de identidade para ignorar respostas concluídas após logout/troca de conta, e seus estados serão reinicializados quando a identidade mudar.

Não será criado cache persistente. Essa medida elimina vazamento visual transitório, mas a segurança continuará garantida pelo backend mesmo para chamadas manuais à API.

### 8. Testes usarão duas identidades reais no contexto de segurança

Testes de controller/integração criarão USER_A e USER_B, autenticarão requests com principais distintos e confirmarão listagem, acesso por ID, criação de operação, posições, resumo e operação direta. Ambos usarão a mesma `Acao` PETR3 para demonstrar que dado mestre é global e patrimônio é independente.

Testes unitários existentes serão adaptados para fornecer owner. Testes de migration validarão FK, `NOT NULL`, unique por owner e falha diante de linha legada sem owner em H2, além de validação PostgreSQL opt-in para semântica real.

## Risks / Trade-offs

- **[Remoção local é irreversível]** → Limitar aos IDs 1 e 2 já inventariados, reconfirmar antes da escrita e preservar todo o restante do banco e o volume; a autorização explícita do usuário foi registrada.
- **[Outro ambiente contém legado real]** → A V8 falha transacionalmente por não possuir default/backfill; revisar e mapear o legado antes de qualquer limpeza.
- **[Query antiga global esquecida]** → Auditar todos os usos de `CarteiraRepository`/`OperacaoRepository` e manter testes negativos por endpoint.
- **[Lock perde proteção de concorrência]** → Substituir a query pessimista por variante com owner na própria cláusula, mantendo a mesma transação.
- **[Resposta tardia aparece após troca de conta]** → Cancelamento/epoch no frontend e limpeza ao mudar identidade; backend permanece a barreira primária.
- **[Mudança de unique causa regressão]** → Testar nomes iguais entre owners, duplicidade no mesmo owner e corrida concorrente.

## Migration Plan

1. Reconfirmar em leitura que as únicas carteiras locais são IDs 1 e 2 e que possuem sete operações no total.
2. Remover somente as operações dessas carteiras e depois as carteiras 1 e 2, conforme autorização explícita; confirmar por leitura que usuários e dados mestres permaneceram.
3. Implementar e testar a V8 sem placeholder, default, backfill ou exclusão automática; legado remanescente deve causar falha/rollback.
4. Atualizar entidade e repositories; somente então adaptar services/controllers e frontend.
5. Executar `mvn verify`, testes frontend, strict OpenSpec e `git diff --check`.
6. Validar PostgreSQL real sem apagar banco ou volume.

Rollback antes da aplicação da V8 é simplesmente reverter código/migration ainda não aplicados. A limpeza local autorizada não possui rollback automático. Depois de aplicada, não editar V8 nem executar downgrade destrutivo; qualquer correção de schema deverá ser uma migration posterior.
