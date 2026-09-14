## 1. Backend deduplication

- [x] 1.1 Confirm the existing history persistence paths and legacy duplicate behavior without modifying data.
- [x] 1.2 Add the repository exact-event existence query keyed by action, quote, and quote timestamp.
- [x] 1.3 Update the history service to skip only exact duplicates before persistence.
- [x] 1.4 Preserve current transaction, current-quote update, and response behavior for `PUT /acoes/{id}/atualizar-cotacao`.
- [x] 1.5 Preserve first-observation behavior for new assets resolved through `POST /acoes/resolver`.

## 2. Backend verification

- [x] 2.1 Add tests proving the first observation is saved.
- [x] 2.2 Add tests proving an exact duplicate is not saved.
- [x] 2.3 Add tests proving the same quote at a different timestamp is saved.
- [x] 2.4 Add tests proving a changed quote is saved and existing update behavior remains valid.
- [x] 2.5 Verify no test or implementation deletes, merges, or rewrites existing history rows.

## 3. Frontend temporal presentation

- [x] 3.1 Update history-axis formatting to select `DD/MM`, `HH:mm`, or `HH:mm:ss` from real timestamp collisions.
- [x] 3.2 Update the history tooltip to display the full timestamp through seconds and the real asset currency.
- [x] 3.3 Add regressions for minute collisions, tooltip precision, equal values, single points, and preservation of all table records.
- [x] 3.4 Verify no random data, interval generation, or frontend deduplication is introduced.

## 4. Validation

- [x] 4.1 Run backend tests and `mvn verify`.
- [x] 4.2 Run frontend tests, build, and lint.
- [x] 4.3 Validate the change with `openspec validate deduplicate-stock-quote-history --strict`.
- [x] 4.4 Run `git diff --check` and review that only this new planning change is affected.
