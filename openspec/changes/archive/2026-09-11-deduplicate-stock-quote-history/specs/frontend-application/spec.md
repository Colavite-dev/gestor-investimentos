## ADDED Requirements

### Requirement: Historical quote views present persisted data faithfully
The frontend SHALL present historical quote records returned by the backend in chronological order, preserving every returned record in the textual table and chart data. Historical labels SHALL use `DD/MM` for differing dates, `HH:mm` for same-day records without a shared minute, and `HH:mm:ss` when multiple same-day records share a minute. Tooltips SHALL include `DD/MM/YYYY, HH:mm:ss` (or an equivalent locale-consistent full timestamp) and the actual quote in the asset currency. The frontend SHALL NOT deduplicate, synthesize, or continuously generate history points.

#### Scenario: History table preserves all returned records
- **WHEN** the backend returns multiple historical observations, including equal values
- **THEN** the frontend displays every observation in the history table and chart input

#### Scenario: Axis precision follows real timestamps
- **WHEN** timestamps have different dates, non-colliding same-day minutes, or colliding same-minute values
- **THEN** the frontend selects `DD/MM`, `HH:mm`, or `HH:mm:ss` respectively

#### Scenario: Tooltip is precise and currency-aware
- **WHEN** a historical point is hovered or focused
- **THEN** the tooltip shows its full timestamp through seconds and formats the quote with the asset's actual currency

#### Scenario: No synthetic real-time behavior is introduced
- **WHEN** the history page is rendered
- **THEN** no random values, interval-generated points, or client-side deduplication are used
