## 1. Maven Coverage Configuration

- [x] 1.1 Pin a stable JaCoCo Maven plugin version compatible with Java 17 and add it to `pom.xml`; verify Maven resolves the project with `./mvnw validate`.
- [x] 1.2 Bind coverage collection, HTML/XML report generation, and aggregate line/branch checks to the Maven test/verify lifecycle at 80% line and 55% branch coverage; verify the configured lifecycle with `./mvnw help:effective-pom`.

## 2. Test Suite and Thresholds

- [x] 2.1 Run the existing automated suite with `./mvnw verify` and inspect `target/site/jacoco/index.html` plus `target/site/jacoco/jacoco.xml` to identify uncovered behavior.
- [x] 2.2 Add focused, behavior-based unit or integration tests for meaningful uncovered paths if needed until the default verification meets both thresholds; verify with `./mvnw verify` and confirm the report shows at least 80% lines and 55% branches. The existing suite already meets both thresholds, so no additional tests were needed.
- [x] 2.3 Temporarily raise a threshold through a Maven property override and verify `./mvnw verify` fails with a useful coverage message, then verify the default command succeeds.

## 3. Documentation and Spec Validation

- [x] 3.1 Document the `./mvnw verify` command and HTML/XML report paths in the root README; verify the instructions match the generated report layout.
- [x] 3.2 Validate the completed change with `openspec validate add-jacoco-coverage-gate --strict` and confirm generated reports remain ignored by Git.
