## Context

See `proposal.md` for motivation and `specs/academic-deliverables/spec.md` for the required coverage behavior. The project already has Maven tests and Spring Security integration tests, but no JaCoCo plugin or coverage gate in `pom.xml`. The normal build is run through the Maven Wrapper; generated files under `target/` are ignored.

## Goals / Non-Goals

**Goals:**
- Measure coverage from the existing automated Maven suite.
- Produce local HTML and XML reports and enforce the specified aggregate thresholds during `verify`.
- Keep measurement and thresholds in Maven so local verification is reproducible without adding another service.

**Non-Goals:**
- Change authentication, authorization, investment-data ownership, or API contracts, which are already defined by stable specs and implemented.
- Add a CI provider/workflow, change test frameworks, or instrument frontend code.
- Exclude application packages merely to make the aggregate threshold pass.

## Decisions

1. **Use the JaCoCo Maven plugin in `pom.xml`.** Bind `prepare-agent` before tests, then generate reports and run the `check` goal in `verify`. This uses the existing test lifecycle and avoids a separate coverage command that could be skipped. Use the current stable JaCoCo release compatible with the project's Java 17 build (0.8.16 at proposal time). Alternatives considered: IDE-only reports are not reproducible in CI or across developers; a separate scanner adds infrastructure beyond the lesson's scope.
2. **Gate the aggregate bundle at 80% line and 55% branch coverage.** These are the thresholds taught in lesson 15 and give a clear project-wide floor. Keep the rule at `BUNDLE` so packages cannot silently fall below the project target while an aggregate report appears healthy. If the existing suite misses a limit, add behavior-focused tests for meaningful uncovered paths instead of weakening the required thresholds or broadly excluding production code.
3. **Publish standard JaCoCo reports under `target/site/jacoco`.** The HTML report is for local inspection; the XML report is suitable for later tooling. These are generated artifacts and remain ignored by Git under `target/`.
4. **Document `mvnw verify` and the report location in the root README.** Do not add a workflow because the repository has no existing CI workflow and the lesson's core outcome is the reproducible Maven gate.

## Risks / Trade-offs

- [The current suite may not meet the thresholds.] → Measure it during implementation and add focused tests for uncovered application behavior before enabling the gate as a passing default build.
- [Coverage can reward execution without proving correctness.] → Keep tests behavior-based and treat coverage as a floor, not as evidence that the system is correct.
- [A JaCoCo upgrade may not support a future bytecode version.] → Pin a stable plugin release compatible with Java 17 and update it deliberately when the Java toolchain changes.

## Migration Plan

No data migration or runtime deployment change is required. Add the plugin and README guidance, execute `./mvnw verify`, inspect the HTML/XML reports and threshold result, then retain or add focused tests until the default verification passes. Rollback consists of reverting the JaCoCo plugin configuration and its documentation if the build cannot be made compatible; no generated `target/` files need to be migrated.
