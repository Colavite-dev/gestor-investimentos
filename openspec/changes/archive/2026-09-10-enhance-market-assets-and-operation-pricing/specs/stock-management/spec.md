## ADDED Requirements

### Requirement: Catálogo e persistência sob demanda permanecem distintos
O sistema SHALL manter `GET /acoes` como listagem de ações persistidas e SHALL oferecer `GET /acoes/catalogo` como consulta externa separada. A identidade de qualquer item em ambos os fluxos SHALL continuar sendo `(ticker normalizado, mercado)`.

#### Scenario: Ativo externo ainda não utilizado
- **WHEN** um ativo aparece no catálogo, mas não foi cadastrado, resolvido ou utilizado
- **THEN** ele não aparece em `GET /acoes` e não existe registro local criado por essa visualização

#### Scenario: Mesmo ticker em mercados diferentes
- **WHEN** providers retornam o mesmo ticker para Brasil e Estados Unidos
- **THEN** os itens permanecem distintos pelo campo `mercado`
