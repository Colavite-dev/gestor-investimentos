## 1. Postman examples

- [x] 1.1 Add an authenticated invalid-CNPJ request to the `Corretoras` folder and assert HTTP 400; verify the request uses a value rejected by local validation before any provider call.
- [x] 1.2 Add an authenticated lookup for the documented positive nonexistent broker ID and assert HTTP 404; verify the request uses the collection base URL and token variables.
- [x] 1.3 Add a duplicate-CNPJ request after the successful broker registration example, document that prerequisite, and assert HTTP 409; verify it reuses the same CNPJ and authenticated user.
- [x] 1.4 Review the full collection for importable JSON, expected folder order, and absence of real credentials; verify by parsing the JSON and checking that each new request has its assertion script and prerequisite description where applicable.

## 2. OpenSpec validation

- [x] 2.1 Run strict OpenSpec validation for this change and resolve any errors; verify the change validates successfully before implementation is considered complete.
