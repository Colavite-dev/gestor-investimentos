## Context

See `proposal.md` for motivation and the authentication delta spec for behavior. The existing conditional bootstrap creates a local administrator with versioned username/password values. Spring configuration already maps environment variables into `application.properties`, and the local launcher restricts the variables it imports from `.env`.

## Goals / Non-Goals

**Goals:**

- Keep the demonstration bootstrap opt-in, disabled by default, idempotent and BCrypt-backed.
- Move its username and password to externally supplied environment configuration with no secret fallback.
- Make an enabled but incomplete configuration fail closed by skipping administrator creation with a safe message.
- Remove only the two confirmed unreferenced frontend assets.

**Non-Goals:**

- Changing authentication endpoints, JWT semantics, user schema, roles, migrations, frontend behavior, or historical OpenSpec archives.
- Adding secrets, secret-management dependencies, new provider integrations, or a production bootstrap pathway.

## Decisions

### Bind demo bootstrap inputs through Spring configuration

`APP_DEMO_ADMIN_ENABLED`, `APP_DEMO_ADMIN_USERNAME`, and `APP_DEMO_ADMIN_PASSWORD` will map through `application.properties`. The bootstrap will receive configuration rather than embed a username or password.

Using environment-backed Spring properties reuses the existing deployment convention and keeps credentials out of versioned source. A separate credential store is unnecessary for local academic demonstration and would expand scope.

### Fail closed for incomplete enabled configuration

When enabled but the configured username or password is blank/invalid, the runner will skip bootstrap and emit only a safe diagnostic that contains no credential value. It will not substitute a default account or password.

Skipping preserves local developer compatibility while preventing an insecure account. Failing the complete application startup was considered, but is not necessary to guarantee the security property and would make a local optional demo feature disruptive.

### Preserve idempotency and BCrypt

For valid configuration, lookup remains keyed by configured username. An existing `ADMIN` is reused without password overwrite; an existing non-admin remains an error. New records use the existing BCrypt `PasswordEncoder`. Any non-secret display metadata required to create the local demo user will be deterministic and must not introduce a usable credential.

### Keep historical records immutable

The current normative specification and executable documentation will stop containing the fixed credential. Existing archived OpenSpec artifacts will remain unchanged because, after runtime removal, they are historical documentation only and no longer provide a functional credential. Verification will record the remaining count and classification without revealing the value.

### Delete assets only after reference confirmation

`frontend/public/icons.svg` and `frontend/src/assets/hero.png` will be removed only after a repository reference scan confirms no imports, CSS URLs, or HTML references. Referenced logo, symbol, and favicon assets remain untouched.

## Risks / Trade-offs

- [Enabled local demo lacks configuration] → Skip bootstrap safely and make the configuration requirement clear without printing values.
- [Configurable username collides with an existing user/email] → Preserve existing role integrity checks and surface a non-secret startup error rather than changing data or roles.
- [Historical literal is mistaken for active secret] → Verify all remaining occurrences are limited to immutable archives/tests and confirm none is used by runtime configuration.
- [Asset reference missed by static scan] → Scan source, public HTML, CSS and tests before deletion; retain only confirmed referenced brand assets.

## Migration Plan

1. Developers who need the local demo set the three documented environment variables in their untracked `.env`.
2. Deployments without the opt-in flag see no bootstrap behavior change.
3. A rollback consists of reverting this change before staging; no migration or persistent schema change is involved.
