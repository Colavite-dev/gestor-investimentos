## Why

The current Postman collection documents the main API flows but is missing the invalid-input, not-found, and duplicate examples required by the accepted academic-deliverables spec. Completing those examples will make the collection a more reliable, reviewable demonstration of the current API contracts.

## What Changes

- Add runnable Postman requests demonstrating a validation error, a missing resource, and a duplicate resource.
- Add response assertions for the expected HTTP status codes and document any request ordering or variables needed to run the examples.
- Keep the collection free of real credentials and align all examples with existing API behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `academic-deliverables`: Clarify that error examples in the importable API collection are runnable, assert their documented status codes, and state any prerequisites.

## Impact

- `docs/api/gestor-investimento.postman_collection.json`
- `openspec/specs/academic-deliverables/spec.md` (delta requirement only; stable spec is changed during the approved sync phase)
- No production API, persistence, or dependency changes.
