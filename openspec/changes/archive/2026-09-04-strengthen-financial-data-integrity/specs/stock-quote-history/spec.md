## ADDED Requirements

### Requirement: Preservar precisão única entre cotação atual e histórico

O sistema MUST representar a cotação atual da ação e cada observação histórica como decimal positivo `NUMERIC(19,8)`. Quando uma cotação externa válida for persistida, a ação e a observação histórica correspondente MUST conservar o mesmo valor decimal, sem arredondamento ou truncamento divergente entre os dois registros.

#### Scenario: Cotação com quatro casas decimais

- **WHEN** uma cotação externa positiva com quatro casas decimais é persistida
- **THEN** a cotação atual e a observação histórica correspondente armazenam o mesmo valor

#### Scenario: Cotação com oito casas decimais

- **WHEN** uma cotação externa positiva com oito casas decimais é persistida
- **THEN** a cotação atual e a observação histórica correspondente armazenam o mesmo valor de oito casas sem perda de precisão
