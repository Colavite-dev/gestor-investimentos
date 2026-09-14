## MODIFIED Requirements

### Requirement: Browser API client

The frontend SHALL centralize requests to the Spring API behind a typed client configured by `VITE_API_BASE_URL`, use the existing endpoint paths and DTO-compatible payloads, and SHALL NOT call external providers directly. Any HTTP response received from the Spring API, including controlled 4xx and 5xx responses, SHALL mark the backend as reachable; only a network failure without an HTTP response SHALL mark the backend as unavailable. Provider failures returned by the backend SHALL remain distinguishable from browser-to-backend connectivity failures.

#### Scenario: Frontend loads a portfolio summary

- **WHEN** a portfolio detail page is opened
- **THEN** the client requests the existing `/carteiras/{id}/resumo`, `/posicoes` and `/operacoes` resources through the configured Spring base URL

#### Scenario: Backend returns provider failure

- **WHEN** the catalog request receives a controlled HTTP error caused by an external provider
- **THEN** the frontend keeps the backend marked reachable and presents the provider error without calling the backend offline

#### Scenario: Browser cannot reach backend

- **WHEN** a request fails before receiving an HTTP response from the configured Spring API
- **THEN** the frontend marks the backend unavailable and presents the connectivity error
