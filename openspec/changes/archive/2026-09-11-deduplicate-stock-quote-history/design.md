## Context

The existing history service persists a row for every valid quote event, while the repository has no exact-event existence query. The database already contains legacy duplicate observations, so this change must protect future writes without rewriting history or changing the REST contract. The existing history page already receives real records and sorts them for presentation, but its same-day axis labels currently omit seconds.

## Goals / Non-Goals

**Goals:**

- Make future history writes idempotent for the exact tuple action, quote, and quote timestamp.
- Preserve current quote-update, asset-resolution, ordering, currency, and transaction semantics.
- Make same-minute history points distinguishable and make tooltips precise.
- Cover the behavior with focused backend and frontend regression tests.

**Non-Goals:**

- Do not delete or merge existing rows.
- Do not add a migration, database constraint, cleanup job, or REST endpoint.
- Do not deduplicate records in the frontend.
- Do not change portfolio, operation, authentication, isolation, provider, or dashboard behavior.

## Decisions

### Service-level exact-event guard

Add a repository existence query keyed by the action identifier, quote value, and `dataHoraCotacao`. The history service checks this tuple immediately before constructing and flushing a new entity. An existing exact event returns without insertion; all other events follow the current persistence path. This reuses the current layered architecture and keeps the public API unchanged.

### Preserve event semantics in update and resolution flows

The existing quote update path continues to update the action's current quote. Its history call is guarded only when the exact event is already present. New-action resolution continues to record its first observation; resolving an existing action retains its current behavior.

### No database UNIQUE constraint in this change

An `exists`-then-save guard is sufficient for the current academic/local scope and avoids a migration that would fail against known duplicate legacy rows. It is not a complete concurrency guarantee: two concurrent transactions can both observe absence and insert. A future database uniqueness migration may be considered only after an explicit legacy-data policy and duplicate cleanup plan are approved. That future work is outside this change.

### Timestamp presentation is derived from real records

The history page keeps the existing chronological sort and all records. A presentation helper derives minute keys from the real timestamps. If any same-day records share a minute, labels include seconds; otherwise same-day labels remain concise. Tooltip formatting always includes the full date and seconds. No chart data is synthesized or filtered.

## Risks / Trade-offs

- **Race between existence check and insert** → Document the best-effort scope and cover sequential exact duplicates in tests; defer database-level enforcement until legacy duplicates have an approved resolution.
- **Legacy duplicate rows remain visible** → This is intentional; preserving audit/history data is safer than destructive cleanup.
- **More detailed labels can become dense** → Apply precision only when a real same-minute collision exists and retain responsive tick reduction already used by the chart.
- **Timezone formatting can vary by environment** → Use the existing frontend date/locale conventions and real ISO timestamps consistently in tests.

## Migration Plan

No migration is required. Deploying the service guard is additive and leaves existing rows untouched. Rollback consists of reverting the application change; no schema rollback or data operation is needed.

## Open Questions

None for this scope. A future database uniqueness decision requires a separate approved change after the legacy duplicate policy is defined.
