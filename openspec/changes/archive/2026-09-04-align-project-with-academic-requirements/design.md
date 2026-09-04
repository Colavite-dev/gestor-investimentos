## Context

The audit verified the mandatory broker and stock flows in controllers, services, repositories, provider adapters, migrations V1–V6, H2/PostgreSQL validation, and the normal test suite. No mandatory functional behavior is currently unsupported. The remaining conflicts are interpretations of the academic wording rather than implementation defects.

## Goals / Non-Goals

**Goals:**

- Preserve the verified implementation while a human resolves academic ambiguities.
- Define a narrow decision protocol that prevents an unnecessary destructive change to asset identity or database support.
- Keep documentation-refresh findings assigned to the later delivery-documentation phase.

**Non-Goals:**

- Changing `(ticker, mercado)` to globally unique ticker.
- Adding MySQL, a new datasource, dependencies, profiles, Docker services, or migrations.
- Changing endpoints, provider selection, external integration behavior, tests, or production code.
- Updating README, entity model, Postman, or general external-API documentation in this change.

## Decisions

### Decision 1: Ticker identity requires professor confirmation

**Professor:** RN07 literally prohibits registering two shares with the same ticker.

**Project:** `(ticker, mercado)` remains the effective identity because it distinguishes instruments from the two supported markets and is enforced by `uk_acoes_ticker_mercado`.

**Status:** justified divergence pending human confirmation.

**Recommendation:** do not alter the implementation before professor confirmation. A global ticker constraint is not introduced from a literal reading alone: it could reject distinct market identities and break the documented ambiguous-ticker query behavior.

Alternative considered: immediately enforce global ticker uniqueness. Rejected because it changes established behavior, schema, stable specifications, and the ability to represent a valid same-symbol cross-market case without evidence that the professor intended that restriction.

### Decision 2: Database wording requires professor confirmation

**Professor:** the non-functional wording mentions H2, MySQL and PostgreSQL.

**Project:** PostgreSQL remains the runtime database and H2 remains test-only.

**Status:** ambiguity pending human confirmation.

**Recommendation:** do not add MySQL before professor confirmation. The current approach satisfies the PRD's technical decision, Flyway governance, and real PostgreSQL validation; the professor wording does not say whether the three names are simultaneous requirements or examples.

Alternative considered: add a MySQL profile. Rejected because it adds infrastructure, compatibility scope, and database behavior without a confirmed academic requirement.

### Keep documentation work separate

The audit found README mojibake and stale descriptions, an entity diagram that omits `Carteira`, `Operacao`, and `CotacaoHistorica`, and a Postman collection that covers the academic minimum but not the complete current API. These are delivery-documentation items for Phase 7, not evidence of a missing core backend behavior.

## Risks / Trade-offs

- [Professor interprets ticker as globally unique] → Obtain a written clarification before changing the unique constraint; if global uniqueness is required, create a dedicated behavior-and-migration change.
- [Professor requires all three database products] → Obtain clarification before adding MySQL; assess whether a profile or portability demonstration is actually required.
- [Documentation is presented before Phase 7] → Prioritize the documented P1/P2 refresh items in the delivery phase; no backend change mitigates stale presentation material.

## Migration Plan

No deployment, rollback, or database migration is planned. If a later human decision requires behavior changes, it must be proposed in a dedicated change with its own stable-spec delta and, if needed, a new migration.
