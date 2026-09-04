## Context

See `proposal.md` for motivation and the `broker-management` delta for the concurrent uniqueness contract. `CorretoraService.cadastrar` is currently a public `@Transactional` method. The Spring proxy opens its transaction before `existsByCnpj`, and the method then performs BrasilAPI, ViaCEP and CVM I/O before calling `saveAndFlush`. Every external exception leaves no broker row because no save was attempted, but the database transaction remains open for the entire remote latency.

`corretoras.cnpj` already has the named unique constraint `uk_corretoras_cnpj` in V1 and the entity declares the same uniqueness. The service catches every `DataIntegrityViolationException` from persistence as `CnpjDuplicadoException`, which can incorrectly turn an unexpected integrity defect into `409`. `CorretoraPostgresIT` is excluded from the normal Surefire naming convention and is stale: it mocks only CNPJ, expects `validadaNaCvm=false`, and has a test-level transaction that hides the commit boundary it purports to test.

## Goals / Non-Goals

**Goals:**

- Keep all external broker enrichment and CVM eligibility evaluation outside a write transaction.
- Make one short, proxy-backed transaction responsible only for saving the already validated `Corretora` and making database errors observable at flush time.
- Preserve early duplicate rejection and give a concurrent CNPJ uniqueness collision the existing controlled `409` semantics.
- Establish opt-in real PostgreSQL evidence for Flyway, Hibernate validation, commit/rollback and unique-CNPJ behavior without adding network dependencies to normal tests.

**Non-Goals:**

- No migration V7, schema change, retry, cache, new external provider, endpoint/payload change, or global repository exception translation.
- No changes to BrasilAPI, ViaCEP or CVM adapters, their timeout policy, or their error taxonomy.
- No broad transaction annotation cleanup outside the broker flow; existing read-only annotations remain unless an implementation finding makes one directly relevant.

## Decisions

### 1. Orchestrate externally in `CorretoraService`; persist through a separate transactional bean

`CorretoraService.cadastrar` will remain the coordination boundary but will no longer be transactional. It will normalize and pre-check the CNPJ, call the existing CNPJ, CEP and CVM ports sequentially, reconcile the address, create a validated entity and delegate that entity to a dedicated broker persistence service.

The dedicated bean will expose a public `@Transactional` persistence method that calls `saveAndFlush`. Being a separate Spring bean ensures the transactional proxy is crossed; annotating a private helper or calling a transactional method on `this` was rejected because self-invocation would not create the intended boundary. The persistence component receives no ports and makes no HTTP call. Reusing the conceptual external-I/O-then-short-write pattern already used for stock quote persistence is appropriate, but broker-specific naming and responsibilities will be used.

### 2. Flush inside the short transaction and preserve rollback

`saveAndFlush` is necessary here to surface a unique or other database constraint violation before the transactional method returns. A successful flush commits at method completion. A persistence failure escapes as a runtime exception, marking the small transaction for rollback; no partially saved broker remains. External failures occur before this method is invoked, so they neither open nor require rollback of a write transaction and do not trigger an automatic re-query or retry.

### 3. Translate only a CNPJ unique-key collision

The pre-check is an optimization, not a concurrency guarantee. The V1 unique constraint remains the authority when two requests pass it concurrently. The persistence layer will translate only a duplicate-key violation (`SQLState 23505`) arising from the broker save path into `CnpjDuplicadoException`; other integrity violations will propagate unchanged and still roll back.

This uses the portable duplicate-key SQL state supported by PostgreSQL and H2 instead of matching PostgreSQL exception message text. It is intentionally scoped to the sole current unique business key on `corretoras` (`cnpj`); if another unique business constraint is added to that table in a future change, this classifier must be revisited. Catching all `DataIntegrityViolationException` was rejected because it can hide null, check, foreign-key or unexpected schema defects as a `409`.

### 4. Keep read transactions and public HTTP mappings stable

`listar`, `buscarPorId` and `buscarPorCnpj` retain their existing read-only transactions because they perform repository reads and do not participate in the long-write problem. `CnpjDuplicadoException` continues through the current centralized handler as `409 Conflict`; integration exceptions preserve their existing `422`, `502` and `503` mappings. No generic `DataIntegrityViolationException` handler will be introduced.

### 5. Replace the stale PostgreSQL IT with an opt-in, commit-visible integration test

`CorretoraPostgresIT` will be updated rather than removed. It will use the configured Docker PostgreSQL datasource, Flyway and Hibernate `validate`, mock all three external ports, remove the class-level `@Transactional`, and clean up only records created by that test. It will verify a successful committed broker has `validadaNaCvm=true`, that a concurrent same-CNPJ registration leaves one row and maps the loser to the duplicate business exception, and that a failed write leaves no partial row.

The test remains opt-in because it requires a locally running PostgreSQL configured through the documented environment. Normal `mvn verify` stays H2/offline. The APPLY phase will run PostgreSQL validation only when Docker and the configured local datasource are available; it will report a genuine environmental limitation rather than claiming it passed.

## Risks / Trade-offs

- [A database connection can still be acquired for the initial duplicate read] → no service write transaction spans remote I/O; the only explicit write transaction begins in the persistence bean after enrichment.
- [H2 and PostgreSQL expose constraint causes differently] → classify using SQLState `23505`, assert behavior in H2, and prove the actual PostgreSQL path in the opt-in IT without parsing vendor messages.
- [A real PostgreSQL IT writes into a developer database] → use dedicated valid test CNPJs, delete only those records in test cleanup, and never remove Docker volumes or reset unrelated data.
- [A losing concurrent request may already have used providers] → preserve the initial pre-check for the ordinary duplicate case; no retry or compensation is introduced for the unavoidable race window.

## Migration Plan

1. Deploy the source-only transaction-boundary refactor; Flyway V1–V6 and the existing CNPJ unique constraint remain unchanged.
2. Existing rows and API payloads require no data conversion.
3. If rollback is necessary, revert the service/persistence component source together; no schema or persisted-data rollback is required.
