## Context

See `proposal.md` for the motivation and the delta specs for the behavioral contract. The application already isolates providers behind `StockDataProvider` and `CvmParticipantProvider`, uses `RestClient` with explicit connect/read timeouts, and maps the three stock integration exception types centrally to `422`, `502`, and `503`.

The brapi adapter currently ignores `requestedSymbol` and `changed`, returning `symbol` as the persisted identity; its existing test accepts `VVAR3 -> BHIA3`. The Twelve Data adapter maps any structured error with code `400` or `404` to ticker-not-found, while an HTTP `400` is handled before its body and becomes provider-unavailable. The CVM parser stores one `CvmParticipantData` per CNPJ with `Map.put`, so the final CSV row wins.

## Goals / Non-Goals

**Goals:**

- Preserve the requested Brazilian asset identity unless the provider confirms the same normalized symbol without a change indication.
- Map external failures by their meaning, consistently across HTTP and structured error envelopes, while maintaining the existing public error contract and secret-safe messages.
- Decide CVM eligibility from all records for a CNPJ, without dependence on CSV order.
- Keep ordinary automated tests offline and deterministic.

**Non-Goals:**

- No automatic ticker-renaming workflow, persisted alias table, redirect, or migration of existing assets.
- No new provider, market, endpoint, HTTP client framework, database change, cache redesign, global retry, or modification to BrasilAPI/ViaCEP.
- No propagation of provider bodies, retry-after headers, API keys, tokens, or provider URLs to API clients.
- No real-provider dependency in `mvn verify`.

## Decisions

### 1. Treat brapi renames and identity mismatches as incompatible external content

The brapi response contains a requested symbol, returned symbol, and change flag. During registration and direct quote refresh, the adapter will normalize all relevant values and accept only `requestedSymbol == requested ticker`, `symbol == requested ticker`, and `changed == false`. A missing field, inconsistent value, or `changed == true` will raise the existing invalid-external-stock-data exception (`502`), not silently register or update another ticker.

This intentionally changes the current canonical-renaming behavior. Automatic adoption of the provider's current symbol was considered, but it would change the logical identity `(ticker, mercado)` without an explicit user decision and can make an update of an existing asset target another asset. A future rename workflow can explicitly model user consent and duplicate handling.

### 2. Use a small provider-error classifier for Twelve Data, with body parsing on non-2xx responses

The Twelve Data adapter will use one internal error-envelope representation (`status`, numeric `code`, and message) for successful HTTP bodies and error HTTP bodies. It will classify the envelope plus the transport status, rather than letting `RestClient.onStatus` discard the body before semantic analysis.

- A clearly symbol-scoped error (invalid, missing, unsupported, or unavailable requested symbol), whether delivered as HTTP `200`, `400`, or `404`, maps to `StockTickerNotFoundException` (`422`).
- A generic/unknown client-side provider error, malformed error envelope, `414`, or an error that cannot safely be attributed to the requested ticker maps to `InvalidStockDataResponseException` (`502`).
- `401`, `403`, `429`, `5xx`, missing API key, network failure, and timeout map to `StockProviderUnavailableException` (`503`).

The classifier will inspect only bounded, provider-supplied fields needed for classification and will never place their text in public exceptions or logs. Mapping every `400` to `422` was rejected because Twelve Data documents `400` as a generic invalid-parameter response; mapping all `400` to `503` was rejected because it contradicts known symbol errors and the existing structured-error behavior.

The adapter will also collect exact eligible `symbol_search` matches before choosing one. Zero results and more than one eligible exact result are both `422`; the latter prevents an arbitrary `findFirst()` decision. Quote ticker/currency/price/timestamp inconsistencies remain external-content failures (`502`).

### 3. Aggregate CVM records by CNPJ and select a deterministic representative

The CSV parser will first retain all valid rows grouped by normalized CNPJ. For each group it will deterministically select an accepted representative when at least one normalized row is active and has a Corretora or Distribuidora category. If no row is accepted, it will deterministically select a non-accepted representative so that the existing service still rejects the CNPJ with `422`.

The priority is: eligible active category first; then a stable ordering over normalized status and category for non-eligible ties. The shared eligibility predicate will have one authoritative implementation reusable by the CVM aggregation and the service decision, avoiding divergent interpretations. Keeping only the last row was rejected because it changes acceptance when CSV order changes; exposing `List<CvmParticipantData>` through the provider port was rejected because consumers require a validation decision, not raw dataset rows.

The official dataset describes a current-day registry spanning several participant categories, so same-CNPJ rows must be handled as potentially meaningful categories rather than presumed corrupt duplicates.

### 4. Preserve existing timeout and credential boundaries; defer retry

brapi, Twelve Data, and CVM configurations already define connect and read timeouts (`2s` connection; reads `5s`, `5s`, and `10s` respectively) and adapters convert `ResourceAccessException` to availability errors. These timeouts are sufficient for this focused change and will be regression-tested through the existing adapter test style.

Automatic retry is deferred. Retrying `429` can consume more quota, and retrying a quote or CVM snapshot needs an explicit policy for backoff, provider limits, observability, and idempotency. It does not correct the identified classification or deterministic-selection failures.

### 5. Retain central error handling and safe logging posture

Adapters emit existing domain integration exceptions; controllers and `GlobalExceptionHandler` remain the public HTTP boundary. There is no adapter logging path presently and exception messages are generic, so no production logging cleanup is required. Tests will assert that mapped public messages do not contain remote body content, credentials, token text, or URLs when new classification paths are added.

## Risks / Trade-offs

- [Provider changes wording or error envelope] → classify only a conservative, documented set of symbol-specific signals; otherwise return `502` instead of incorrectly blaming the client.
- [Legacy ticker can no longer be registered through automatic rename] → reject with `502` until a dedicated rename/alias change defines explicit domain behavior.
- [CVM categories arrive with spelling/accent variants] → reuse the current accent-insensitive normalization and verify representative selection with permutations.
- [Reading error bodies adds adapter complexity] → keep the DTO internal to the Twelve Data package and cover equivalent HTTP/body forms with mock-server tests.
- [Real services vary or rate-limit] → retain opt-in real integration tests and avoid internet in the normal Maven lifecycle.

## Migration Plan

1. Deploy adapter and test changes without schema or data migration.
2. New lookups will reject renamed/inconsistent brapi responses; existing persisted assets are not rewritten.
3. CVM snapshots are rebuilt on their normal refresh cycle using deterministic aggregation.
4. Rollback is source-only: restore the prior adapter behavior; no persisted data conversion is required.
