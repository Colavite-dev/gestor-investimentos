## Context

The provider contracts were checked with real calls: brapi returns HTTPS `logo` URLs and Twelve Data returns real eligible US instruments for the existing country/type/currency filters. The local `.env` contains a Twelve Data key, while the current shell process does not, and the existing local script already whitelists that variable for the child backend process.

## Goals / Non-Goals

**Goals:**

- Correct only an evidenced logo handoff or browser rendering failure.
- Preserve real US catalog behavior when the backend process inherits its local environment.
- Keep provider-originated HTTP errors distinct from backend network failure in the browser.

**Non-Goals:**

- Changing Twelve Data eligibility filters, adding a US fallback list, persisting logos, migrations, authentication, CORS, layout, or provider credentials.

## Decisions

### Verify the running contract before altering mappings

The adapter, service, DTO and TypeScript chain already declare `logoUrl`; implementation will use an authenticated endpoint response and browser image behavior to identify the first failing handoff. Only that handoff will change. This avoids speculative changes to a provider contract that already returns valid HTTPS logos.

### Treat missing process configuration as an operational cause, not a synthetic-data case

The Twelve Data adapter will continue to fail safely without a key. The existing script/property mechanism will be changed only if endpoint execution proves it fails to pass the present local key into the child process. No key, static US instruments, or relaxed provider filter will be added.

### Preserve HTTP reachability semantics

The API client will keep marking any received HTTP response as backend reachable. Catalog UI feedback will show provider failure as such and reserve backend-offline feedback for fetch failures without a response.

## Risks / Trade-offs

- [Provider response changes] → Keep strict validation and fixture coverage while testing the live contract separately.
- [Local backend is started outside the supported script] → Report the missing process variable rather than masking it in code.
- [Browser image failure is transient] → Retain per-image fallback and add a regression test for the valid URL path.

## Migration Plan

No schema or deployment migration is required. Restarting the backend through the supported local launcher is sufficient for a present `.env` key to be inherited.

## US catalog refinement

The `/stocks` directory response is mapped only to catalog discovery metadata; it does not contain a current price. The adapter SHALL not issue a `/quote` request per result because that would turn one cached directory request into a rate-limit-sensitive fan-out. The existing resolver/update quote flow remains the only source of a current price.

For directory eligibility, the adapter SHALL require the existing `country`, `type` and `currency` contract, an accepted structured exchange/MIC venue, and a compact normal ticker identifier. This generic rule rejects separators, whitespace and other composite-symbol punctuation without a ticker-specific exclusion list, while preserving ordinary listed symbols.
