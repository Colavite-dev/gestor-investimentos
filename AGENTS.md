# AGENTS.md

## Project

This is an academic investment management system built with Java and Spring Boot.

Before making meaningful changes, understand the project context.

## Sources of Truth

Read the relevant documentation before implementing changes.

Product requirements:

docs/product/PRD.md

Original academic requirements:

docs/requirements/PROFESSOR_REQUIREMENTS.md

Architecture:

docs/architecture/ARCHITECTURE.md

Project decisions:

docs/decisions/DECISIONS.md

OpenSpec changes:

openspec/changes/

Stable system specifications:

openspec/specs/

## Development Method

This project follows Spec-Driven Development.

Do not implement a meaningful feature without checking whether an OpenSpec change exists for it.

When an OpenSpec change exists:

1. read proposal.md;
2. read the relevant specs;
3. read design.md;
4. read tasks.md;
5. implement only the approved scope;
6. update tasks as work is completed;
7. run relevant tests;
8. report deviations or blockers.

Do not silently expand scope.

## Academic Requirements

The requirements from the professor must not be ignored.

If a requested change conflicts with:

docs/requirements/PROFESSOR_REQUIREMENTS.md

or:

docs/product/PRD.md

do not silently choose one.

Report the conflict.

## Architecture

Follow the architecture documented in:

docs/architecture/ARCHITECTURE.md

Controllers must not contain business logic.

JPA entities must not be used directly as API request/response contracts.

External APIs must be isolated behind internal abstractions.

Business services must not depend directly on external API DTOs.

## External APIs

Never expose API keys or tokens in source code.

Use configuration/environment variables.

External provider failures must be handled deliberately.

Do not assume an external API is always available.

Do not create fake external data as a substitute for required integrations.

## Database

PostgreSQL is the primary database.

Flyway owns schema evolution.

Do not use Hibernate automatic schema creation as the official database migration mechanism.

Never modify an already-applied migration to represent a new schema change.

Create a new migration instead.

## Secrets

Never place secrets in:

- source code;
- committed configuration;
- documentation;
- examples.

Use environment variables.

`.env.example` must contain variable names only and no real credentials.

## Testing

New business rules should have relevant automated tests.

External APIs should normally be mocked or replaced by test doubles in automated tests.

After implementation:

- run relevant unit tests;
- run relevant integration tests;
- run the project build when practical.

Do not state that a task is complete when tests are failing unless the failure is clearly reported.

## Code Quality

Prefer simple and explicit solutions.

Avoid unnecessary abstractions.

Do not introduce a library unless it solves a concrete project need.

Follow existing naming and package conventions.

Keep classes focused on a single responsibility.

Avoid duplicating business rules.

## Change Discipline

Before editing code:

1. inspect the existing implementation;
2. inspect the relevant OpenSpec change;
3. identify affected components.

During implementation:

1. make small coherent changes;
2. preserve unrelated behavior;
3. avoid speculative features.

After implementation:

1. run validation;
2. compare the result with the spec;
3. report changed files;
4. report tests executed;
5. report unresolved issues.