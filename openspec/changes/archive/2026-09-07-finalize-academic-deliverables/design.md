## Context

See `proposal.md` for motivation. The project has 18 HTTP endpoint mappings: Corretoras (4), Acoes (5), Historico de Cotacoes (1), Carteiras (6), and Operacoes (2). The current Postman collection has 18 requests but only organizes Corretoras and Acoes, so several requests are error examples rather than coverage of the current Carteira and Operacao endpoints. `entity-model.md` still renders only Corretora and Acao. README has both stale phase statements and mojibake, most visibly in the historical quote, operations and positions sections.

## Goals / Non-Goals

**Goals:**

- Make each deliverable traceable to controllers, DTOs, migrations, configuration and automated tests already present in the repository.
- Keep README concise by using it as the entry point and placing provider-specific detail in one `docs/integrations/external-apis.md` document.
- Preserve the professor requirements as a source record: only correct its textual encoding, never reinterpret its wording in place.
- State the two academic ambiguities as pending confirmation rather than as professor-approved decisions.

**Non-Goals:**

- Adding an endpoint, API contract, runtime configuration key, provider, migration, test, Swagger/OpenAPI, CORS, frontend, authentication, retry or cache.
- Claiming external-provider quotas, credentials, or behavior that cannot be verified from the application configuration or provider documentation.
- Creating a diagram relation between Corretora and Acao, which is not persisted.

## Decisions

### README is a concise navigation document; integrations receive one detailed document

The README will contain a short integration overview and link to `docs/integrations/external-apis.md`. The detailed document will cover BrasilAPI, ViaCEP, CVM, brapi and Twelve Data: call purpose, configuration keys and defaults, authentication only where applicable, explicit timeouts, controlled error semantics, real-test opt-in mechanism, and provider-plan-dependent rate limits.

Alternative considered: document every provider in the README. Rejected because it would duplicate operational detail and make the primary entry point too long for an academic presentation.

### Documentation sources of truth are code and configuration

Endpoint coverage will be derived from controller mappings and request bodies from DTOs. Mermaid fields and relations will be derived from migrations and JPA entities. Configuration will be derived from `application.properties`, `compose.yaml`, `.env.example` and opt-in test annotations. The documentation must not infer future support from PRD language.

### Postman will cover each current endpoint once as its baseline

The collection will use folders Corretoras, Acoes, Historico de Cotacoes, Carteiras and Operacoes. It will define only non-secret reusable variables: `baseUrl`, `corretoraId`, `cnpj`, `acaoId`, `ticker`, `mercado`, `carteiraId` and `operacaoId`. Request examples will use valid DTO fields and ISO-8601 timestamps; no database password, provider key or bearer token will be stored.

Alternative considered: retain endpoint-error examples instead of missing resource folders. Rejected because full API coverage is the delivery goal; error behavior belongs in README and can remain as a small optional example only if it does not displace a real endpoint.

### Academic ambiguities remain explicit and non-operative

Documentation will describe `(ticker, mercado)` as the project’s current logical and database identity, and PostgreSQL runtime plus H2 test use as the current technical choice. Both statements must say they are pending academic confirmation because the professor text is literally different or ambiguous. Neither will be “fixed” by changing code or adding MySQL.

### UTF-8 fixes are reviewed document by document

Apply changes will save changed text files in UTF-8 and inspect every changed file for known mojibake forms. The Postman JSON remains structurally valid JSON; no binary artifact is touched. The professor requirements receive encoding-only corrections, while README, architecture, decisions and PRD receive factual revisions limited to delivery coherence.

## Risks / Trade-offs

- [Documentation drifts while being consolidated] → Cross-check every endpoint, DTO field, environment key, migration number and test command against its source before final validation.
- [Provider limits change outside the repository] → Document only configured timeout/error behavior and say quotas depend on provider and plan when no project-held evidence exists.
- [Mermaid syntax is invalid or cardinality is invented] → Validate syntax and compare relationships to V3–V5 and the JPA mappings.
- [A secret enters documentation or Postman] → Inspect changed files and collection variables for secret-shaped names/values; retain only placeholders and non-secret variables.
- [Academic ambiguity is presented as approval] → Use “pendente de confirmação acadêmica” consistently in README and DECISIONS.

## Migration Plan

Documentation-only change. No runtime migration or rollback is required. Reverting the commit restores prior documents without affecting schema or application behavior.
