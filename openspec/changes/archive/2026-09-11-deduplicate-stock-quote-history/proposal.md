# Proposal: Deduplicate Stock Quote History

## Why

The quote-history persistence flow currently records an observation every time a valid quote is processed, even when the action, quote value, and quote timestamp are identical to an observation already stored. This creates misleading duplicate history rows without adding new market information.

## What Changes

- Prevent new exact duplicate quote-history observations using the identity tuple `(acao, cotacao, dataHoraCotacao)` while preserving all existing rows.
- Keep current quote update and asset-resolution behavior, including the first observation for a newly resolved asset.
- Improve historical-chart time labels so records in the same minute remain distinguishable, while retaining every record returned by the API.
- Keep REST contracts, authentication, ownership rules, financial calculations, providers, and database migrations unchanged.

## Capabilities

### Modified Capabilities

- `stock-quote-history`: New quote-history observations are idempotent for an identical action, quote, and quote timestamp; existing history remains immutable and complete.
- `frontend-application`: Historical chart labels and tooltips expose sufficient timestamp precision without client-side deduplication.

## Impact

- Backend repository and service logic will gain a pre-persistence existence check.
- Backend tests will cover exact duplicates, timestamp/value changes, quote updates, and asset resolution.
- Frontend history presentation will derive axis precision and tooltip timestamps from the real records already returned by the existing endpoint.
- No migration or cleanup is included. A database-level uniqueness constraint remains a future, separately planned evolution because legacy duplicate rows already exist.
