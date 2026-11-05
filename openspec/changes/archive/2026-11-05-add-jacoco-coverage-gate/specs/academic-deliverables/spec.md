## ADDED Requirements

### Requirement: Reproducible test coverage report and quality gate
The project SHALL generate a reproducible Java code coverage report during the standard Maven verification lifecycle. The verification SHALL fail when aggregate line coverage is below 80% or aggregate branch coverage is below 55%. The report SHALL be available as HTML and machine-readable XML, and local build artifacts SHALL remain untracked.

#### Scenario: Verification meets the coverage thresholds
- **WHEN** a developer runs the Maven wrapper verification with the automated test suite passing and both aggregate coverage thresholds met
- **THEN** the build succeeds and produces HTML and XML coverage reports under the Maven build output directory

#### Scenario: Verification fails below either threshold
- **WHEN** the automated tests pass but aggregate line coverage is below 80% or aggregate branch coverage is below 55%
- **THEN** the Maven verification fails and reports which coverage threshold was missed

#### Scenario: Coverage results can be inspected locally
- **WHEN** a developer completes the Maven verification
- **THEN** the developer can open the HTML report and consume the XML report without exposing secrets or adding generated reports to version control
