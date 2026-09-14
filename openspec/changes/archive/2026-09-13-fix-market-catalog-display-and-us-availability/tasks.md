## 1. Diagnose and correct catalog handoffs

- [x] 1.1 Verify authenticated BR and US catalog responses from the running backend without exposing tokens; identify the first failing BR logo handoff and confirm the US provider error or data count.
- [x] 1.2 Verify the BR logo mapping and rendering handoff; existing provider, DTO, endpoint and HTTPS rendering behavior already preserve valid logos, so no BR source change was warranted by the reproduced contract.
- [x] 1.3 Verify local environment propagation for Twelve Data; preserve the working script, property and provider filters, and document that the running process must receive the local variable.

## 2. Error presentation and tests

- [x] 2.1 Update only directly affected backend/frontend tests for logo delivery, US catalog behavior and backend-versus-network error classification; verify no static US data or provider key enters the frontend.
- [x] 2.2 Verify catalog feedback keeps a received provider HTTP error distinct from a backend network failure without redesigning the page.

## 3. Validation

## 4. Refine US catalog eligibility and quote feedback

- [x] 4.1 Inspect the Twelve Data `/stocks` contract and refine US catalog eligibility with structured venue metadata and a generic normal-symbol rule, without ticker blacklists or individual catalog quote calls.
- [x] 4.2 Add focused backend tests for a normal US ticker, rejected composite/ineligible instruments and a non-empty catalog page; preserve BR catalog behavior.
- [x] 4.3 Replace the empty catalog-price dash with concise tracking-time quote feedback and cover it with the existing frontend page test.
- [x] 4.4 Run the requested backend, frontend, OpenSpec and live authenticated BR/US catalog validations without exposing credentials or changing migrations/security work.

- [x] 4.5 Run Java 17 verification with `./mvnw.cmd verify` and report test totals, failures and errors.
- [x] 4.6 Run `npm test`, `npm run build` and `npm run lint` from `frontend/`.
- [x] 4.7 Validate the change and all OpenSpec changes strictly, run `git diff --check`, inspect secret exposure without values, and confirm migrations and `secure-demo-admin-before-git` remain unchanged.
