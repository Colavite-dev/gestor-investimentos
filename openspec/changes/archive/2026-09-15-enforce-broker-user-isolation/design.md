## Context

See `proposal.md` for motivation and the delta specs for required behavior. `Corretora` is currently a global entity: its table has no `usuario_id`, its CNPJ is globally unique, and its controller, service, and repository do not consume the authenticated identity. Authentication is already available through the JWT principal, whose stable user ID is exposed through `Authentication.getName()` and converted by `AuthenticatedUserId`.

`Carteira` is the established ownership reference. It has a mandatory `ManyToOne` relationship to `Usuario`, receives the authenticated ID at its controller boundary, passes it explicitly to its service, and executes repository queries scoped by `usuarioId`. V8 added its ownership without backfill or implicit deletion. `CorretoraService` already keeps the BrasilAPI, ViaCEP, and CVM calls outside the short write transaction delegated to `CorretoraPersistenceService`.

## Goals / Non-Goals

**Goals:**

- Make `Corretora` a private resource owned by exactly one authenticated `Usuario`.
- Enforce ownership in database constraints and owner-scoped reads, including ID and CNPJ lookups.
- Preserve the existing HTTP endpoints and DTO contracts while limiting their result sets to the owner.
- Make `(usuario_id, cnpj)` the final database-enforced identity for a broker registration.
- Add V9 later without assigning, deleting, or silently transforming legacy broker rows.

**Non-Goals:**

- Change JWT claims, authentication, roles, or the `AuthenticatedUserId` mechanism.
- Change BrasilAPI, ViaCEP, CVM validation, retries, caching, or the CVM eligibility rule.
- Change Carteira, Operacao, market data, frontend behavior, visual UI, Swagger, or request/response DTO fields.
- Add a broker catalog or a separate shared-master-to-user-association model.

## Decisions

### 1. Corretora is the ownership root for broker registrations

`Corretora` will have a mandatory unidirectional `ManyToOne` relationship to `Usuario`, stored as `usuario_id NOT NULL`. The logical relationship is:

```text
Usuario (1) ─────< (N) Corretora
```

The owner is stored on `Corretora`; an inverse collection on `Usuario` is unnecessary because this change only needs owner-scoped broker reads. Requiring `Usuario` in the broker construction path preserves the entity invariant instead of allowing an ownerless transient broker.

This mirrors the existing `Carteira` ownership model and avoids a separate association table. A shared global broker catalog plus a user association was considered, but it would introduce a new domain model, extra lifecycle rules, and a larger migration without being required for private broker registrations.

### 2. Authenticated identity crosses controller to service explicitly

`CorretoraController` will accept `Authentication` on all four broker endpoints and call `AuthenticatedUserId.from(authentication)`. It will pass the resulting `Long usuarioId` to `CorretoraService` methods for create, list, lookup by ID, and lookup by CNPJ.

`CorretoraRequest` remains CNPJ-only and `CorretoraResponse` remains unchanged. Neither DTO gains `usuarioId`, owner, or username. Passing the owner explicitly follows `CarteiraController` and keeps authorization visible in service signatures; reading `SecurityContext` in repositories/services was rejected because it would hide an authorization dependency and complicate focused tests.

### 3. Repository queries are owner-scoped at the database boundary

The repository contract will replace global broker queries with owner-scoped equivalents:

- existence by `usuarioId` and normalized CNPJ;
- list by `usuarioId`, ordered by `id` ascending;
- lookup by `id` and `usuarioId`;
- lookup by normalized CNPJ and `usuarioId`.

The service will pass the authenticated ID to every query. It will not load a global broker and compare its owner in memory. Both a missing row and a row owned by a different user therefore produce the same empty repository result and the existing `CorretoraNotFoundException`, mapped to `404 Not Found`.

This prevents IDOR-style access by ensuring an arbitrary ID or CNPJ is never resolved outside the principal's ownership scope. Route authentication alone was rejected: it proves that a caller has a valid identity but does not prove the caller owns a requested resource.

### 4. Registration preserves the existing external-validation and transaction boundary

The registration flow will be:

1. controller derives `usuarioId` from the authenticated principal;
2. service normalizes the CNPJ and checks duplicate registration for that owner;
3. service performs BrasilAPI, ViaCEP, address reconciliation, and CVM eligibility validation outside a write transaction;
4. service locates the `Usuario` by the authenticated ID and builds a `Corretora` associated with it;
5. `CorretoraPersistenceService` persists and flushes the validated entity in its existing short transaction.

The user lookup follows the established `CarteiraService` approach. A stale authenticated ID that no longer resolves to a user is handled through the existing credential/error policy rather than accepting an owner supplied by the client.

External validation remains per registration. Two users registering the same institution can cause two valid provider flows; adding a global cache or changing provider behavior is out of scope. The status `EM FUNCIONAMENTO NORMAL` remains eligible when the participant category is compatible, as already defined by the current CVM rule.

### 5. Database uniqueness is composite and remains the race-proof guarantee

V9 will replace `uk_corretoras_cnpj` with:

- foreign key: `fk_corretoras_usuario` on `corretoras(usuario_id)` referencing `usuarios(id)`;
- unique constraint: `uk_corretoras_usuario_cnpj` on `(usuario_id, cnpj)`;
- owner-listing index: `idx_corretoras_usuario_id` on `(usuario_id, id)`.

The application pre-check remains an optimization that avoids external calls for an already-registered CNPJ within the same account. It is not the concurrency guarantee. The composite unique constraint is authoritative: concurrent same-user attempts may both finish validation, but exactly one `saveAndFlush` succeeds and the other is translated to `CnpjDuplicadoException` and HTTP `409`.

`CorretoraPersistenceService` will identify the explicit `uk_corretoras_usuario_cnpj` constraint name, while retaining duplicate-key SQL-state checking, and translate only that collision to `CnpjDuplicadoException`. Other integrity violations continue to propagate through existing error handling. This makes the intended conflict resilient to PostgreSQL's concurrent write race without incorrectly treating cross-user registrations as duplicates.

### 6. V9 adds mandatory ownership safely and in a transaction

The planned migration filename is `V9__add_corretora_ownership.sql`. V1 through V8 remain immutable. PostgreSQL transactional DDL and Flyway's transaction support will be relied upon so a failed migration rolls back its schema changes.

The V9 operation order will be:

1. `ALTER TABLE corretoras ADD COLUMN usuario_id BIGINT NOT NULL` with no default and no backfill;
2. add `fk_corretoras_usuario` referencing `usuarios(id)`;
3. drop `uk_corretoras_cnpj`;
4. add `uk_corretoras_usuario_cnpj UNIQUE (usuario_id, cnpj)`;
5. create `idx_corretoras_usuario_id (usuario_id, id)`.

On an empty `corretoras` table, the migration succeeds and establishes mandatory ownership. On any populated legacy table, step 1 fails because existing rows cannot satisfy `NOT NULL`; PostgreSQL rolls back the migration, leaving the original schema and data intact. The migration intentionally contains no `UPDATE`, `DELETE`, hardcoded owner, default owner, or placeholder user.

Adding a nullable column and later making it non-null was rejected because it would create an ambiguous period and invite silent ownership assignment. Automatic backfill to the first user, an administrator, or a fixed ID was rejected because it would fabricate authorization and could expose legacy data to the wrong account.

### 7. Legacy development data requires an explicit out-of-band decision

Before V9 is implemented or applied to a local development database, the user must perform a read-only inventory of legacy rows in `corretoras` and decide whether each row has a known, legitimate owner.

For the simple academic-development case where every legacy broker row is confirmed as disposable test data, the user must explicitly approve removal of the inventoried broker IDs outside Flyway, then remove only those exact rows manually and re-run the read-only inventory to confirm the table is empty. This process must not remove users, carteiras, operacoes, acoes, quotations, history, the database, or Docker volumes; it must never use `docker compose down -v`.

If a legacy broker must be preserved and no owner is known, V9 must not be applied. The only preservation alternative is a separately approved, auditable mapping of each legacy broker to a user selected with business authority. That is intentionally outside this change's minimal scope because it requires data ownership decisions that cannot be inferred safely from the current schema.

### 8. HTTP and frontend contracts remain compatible

`POST /corretoras`, `GET /corretoras`, `GET /corretoras/{id}`, and `GET /corretoras/cnpj/{cnpj}` retain their paths, methods, request DTO, response DTO, successful status codes, and provider error mappings. The only observable change is that successful reads and uniqueness are scoped to the authenticated owner. The frontend continues to send only `{ "cnpj": "..." }` and consumes the same response fields.

## Risks / Trade-offs

- [Legacy broker rows block V9] → Inventory and decide on data outside the migration; do not implement or apply V9 until the table is empty or every preserved row has an explicitly authorized ownership plan.
- [Two users can trigger duplicate external validations for the same institution] → Accept this as required by independent registrations; do not add cache or retry behavior in this change.
- [Concurrent same-user registrations both reach providers] → Preserve the current short transaction model and rely on `uk_corretoras_usuario_cnpj` for final `409` enforcement.
- [A global query is accidentally retained] → Cover each repository method and each endpoint with USER_A/USER_B tests; use owner-scoped query names and signatures.
- [Constraint translation breaks after V9] → Use the explicit composite constraint name in persistence tests and PostgreSQL integration tests.
- [Migration behavior differs between H2 and PostgreSQL] → Retain a focused H2/Flyway migration test for empty and legacy schemas, plus opt-in PostgreSQL integration validation when V9 is implemented.

## Migration Plan

1. During IMPLEMENT, add the entity and repository ownership changes, controller/service propagation, persistence constraint translation, V9, and the planned tests as one coherent change.
2. Before applying V9 to a populated local database, inventory `corretoras` and obtain explicit authorization for any removal or ownership mapping outside Flyway.
3. If all legacy broker rows are explicitly confirmed disposable, remove only the exact inventoried rows manually, confirm `corretoras` is empty, then apply V9 through the normal Flyway startup path.
4. Validate the resulting PostgreSQL schema, owner-scoped USER_A/USER_B behavior, same-owner collision handling, and cross-owner concurrent registration.
5. Rollback strategy: do not use Flyway repair, schema reset, volume deletion, or automatic down migration. If V9 fails on legacy data, the transaction preserves the original schema and rows; resolve the explicitly reported data decision before retrying.
