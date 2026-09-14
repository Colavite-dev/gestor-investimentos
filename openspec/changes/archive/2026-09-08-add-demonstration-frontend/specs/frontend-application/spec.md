## Purpose

Oferecer uma interface web própria, legível e demonstrável para acompanhar visualmente os fluxos reais de investimentos do backend.

## ADDED Requirements

### Requirement: Demonstration shell and navigation

The frontend SHALL provide a dark-only responsive application shell with a blue primary identity, compact sidebar, contextual topbar and navigation for Dashboard, Ações, Carteiras, Operações, Corretoras and Histórico.

#### Scenario: User navigates the application

- **WHEN** the user selects an item in the sidebar
- **THEN** the corresponding page is rendered without a full browser reload and the active item is visually identified

### Requirement: Real investment views

The frontend SHALL present Corretoras, Ações, Histórico de Cotações, Carteiras, Operações, Posições and Resumo using data returned by the backend API, without replacing demonstrable production flows with fabricated dashboard data.

#### Scenario: User selects a portfolio

- **WHEN** the user changes the selected Carteira in the dashboard
- **THEN** the displayed resumo, posições and operations are loaded for that carteira

#### Scenario: User opens quote history

- **WHEN** the user requests history for an existing Acao
- **THEN** the UI displays the returned timestamp and quote values in a table and may render a line chart from those same values

### Requirement: Financial presentation

The frontend SHALL format monetary values and dates according to their returned currency and timestamp, distinguish positive results with success styling and negative results with danger styling, and SHALL NOT reimplement or override backend financial rules.

#### Scenario: User views a position

- **WHEN** a position contains values from the API
- **THEN** quantity, average price, invested value, current value and profit/loss are shown using the returned values and presentation-only calculations

### Requirement: Accessible feedback states

The frontend SHALL provide visible loading, empty, success and controlled error states for asynchronous pages and forms, with labels, keyboard-focus indication and semantic controls.

#### Scenario: Provider-backed form is submitted

- **WHEN** the user submits a Corretora or Acao form
- **THEN** the UI shows progress, prevents duplicate submission and presents the API result or a recoverable error state

## REMOVED Requirements

None.
