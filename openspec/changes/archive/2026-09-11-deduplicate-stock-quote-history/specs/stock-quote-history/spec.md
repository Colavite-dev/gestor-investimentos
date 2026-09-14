## ADDED Requirements

### Requirement: Historical observations are idempotent for an exact quote event
The system SHALL preserve every existing historical observation and SHALL NOT insert a new observation when the same action, quote value, and `dataHoraCotacao` already exist together. An observation with the same action and quote but a different quote timestamp SHALL remain eligible for insertion, and an observation with a different quote SHALL remain eligible for insertion. This rule SHALL NOT alter the current quote update or asset-resolution response contracts.

#### Scenario: First observation is stored
- **WHEN** a valid action quote is processed and no historical observation exists for the same action, quote, and quote timestamp
- **THEN** one historical observation is persisted

#### Scenario: Exact duplicate is ignored
- **WHEN** a valid action quote is processed and an observation with the same action, quote, and quote timestamp already exists
- **THEN** no additional historical observation is persisted and the main quote-processing operation remains successful

#### Scenario: Same quote at a different time is retained
- **WHEN** a valid action quote has the same value as an earlier observation but a different quote timestamp
- **THEN** a new historical observation is persisted

#### Scenario: A changed quote is retained
- **WHEN** a valid action quote has a different value from earlier observations
- **THEN** a new historical observation is persisted according to the existing quote-processing flow

#### Scenario: Existing history is never cleaned up by this behavior
- **WHEN** the deduplication behavior is deployed
- **THEN** existing historical rows remain unchanged and no delete, backfill, or automatic cleanup is performed

### Requirement: Quote update and asset resolution preserve historical semantics
The system SHALL continue to update the current action quote through `PUT /acoes/{id}/atualizar-cotacao` while suppressing only an exact duplicate historical observation. The system SHALL continue to allow `POST /acoes/resolver` to create the first observation for a newly persisted action and SHALL preserve the existing behavior for an already persisted action.

#### Scenario: Quote update changes the current quote without duplicating identical history
- **WHEN** `PUT /acoes/{id}/atualizar-cotacao` processes a valid quote event identical to an existing historical event
- **THEN** the current quote update succeeds and no duplicate historical row is added

#### Scenario: New asset resolution records its first observation
- **WHEN** `POST /acoes/resolver` persists a previously unknown valid action with a quote
- **THEN** the action and its first historical observation are created using the existing contract

#### Scenario: Existing asset resolution does not change unrelated history behavior
- **WHEN** `POST /acoes/resolver` resolves an action already present in the master catalog
- **THEN** the existing resolution response and historical behavior remain unchanged

### Requirement: Historical chart presentation preserves records and exposes time precision
The frontend SHALL render every historical record returned by `GET /acoes/{id}/historico-cotacoes` without filtering or deduplicating records. For records on different dates it SHALL use a compact day/month label; for records on the same date it SHALL use `HH:mm` when no records share a minute and `HH:mm:ss` when two or more records share a minute. Tooltips SHALL show a full date and time including seconds in the active locale format.

#### Scenario: Different dates use compact date labels
- **WHEN** the returned history contains records on more than one calendar date
- **THEN** the chart uses compact `DD/MM`-style labels while preserving all records

#### Scenario: Same-day records without minute collision use minutes
- **WHEN** all returned records are on the same date and no two records share the same hour and minute
- **THEN** chart labels use `HH:mm`

#### Scenario: Same-minute records use seconds
- **WHEN** two or more returned records share the same date, hour, and minute
- **THEN** chart labels use `HH:mm:ss` so the observations remain distinguishable

#### Scenario: Tooltip exposes full timestamp
- **WHEN** a user inspects a historical point
- **THEN** the tooltip shows the record's date and time through seconds and its actual quote formatted in the asset currency

#### Scenario: Equal historical values remain equal
- **WHEN** multiple records have the same quote value
- **THEN** the chart renders those real records without altering values or inventing variation
