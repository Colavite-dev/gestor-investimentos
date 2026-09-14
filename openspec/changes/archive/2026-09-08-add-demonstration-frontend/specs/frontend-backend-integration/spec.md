## Purpose

Definir um consumo seguro e previsível da API Spring pelo navegador, preservando os contratos existentes e mantendo credenciais de providers exclusivamente no backend.

## ADDED Requirements

### Requirement: Browser API client

The frontend SHALL centralize requests to the Spring API behind a typed client configured by `VITE_API_BASE_URL`, use the existing endpoint paths and DTO-compatible payloads, and SHALL NOT call external providers directly.

#### Scenario: Frontend loads a portfolio summary

- **WHEN** a portfolio detail page is opened
- **THEN** the client requests the existing `/carteiras/{id}/resumo`, `/posicoes` and `/operacoes` resources through the configured Spring base URL

### Requirement: Backend error presentation

The frontend SHALL map the backend's controlled HTTP statuses 400, 404, 409, 422, 502 and 503 to user-readable messages without exposing raw credentials, provider bodies or sensitive URLs.

#### Scenario: Backend rejects a sale

- **WHEN** an operation request returns 422
- **THEN** the UI identifies it as a business-rule rejection and does not claim that the operation was created

### Requirement: Local browser access

The browser demonstration SHALL work from the configured local Vite origin to the local Spring API. If same-origin serving is not used, the backend SHALL allow only the documented local development origin(s) required by the frontend rather than an unrestricted wildcard.

#### Scenario: Browser calls the local API

- **WHEN** the frontend runs on its local development origin and requests a backend endpoint
- **THEN** the request succeeds or returns the API's controlled response without requiring provider credentials in the browser

### Requirement: Environment and secret boundary

The frontend SHALL expose only a non-secret API base URL through Vite environment configuration and SHALL NOT contain BRAPI_TOKEN, TWELVE_DATA_API_KEY, DB_PASSWORD or other provider/database secrets.

#### Scenario: Frontend build configuration is inspected

- **WHEN** the demonstrable frontend is built or its environment example is reviewed
- **THEN** only browser-safe configuration is present and all external provider authentication remains server-side

## REMOVED Requirements

None.
