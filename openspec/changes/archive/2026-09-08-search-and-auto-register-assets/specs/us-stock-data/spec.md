## ADDED Requirements

### Requirement: Descoberta de sugestões americanas sem escrita
Para uma pesquisa de ações, o sistema SHALL usar a descoberta de instrumentos da fonte americana para retornar sugestões dos Estados Unidos compatíveis com o termo. Uma sugestão americana MUST representar ação comum dos Estados Unidos em USD; instrumentos de outros países, moeda, tipo ou mercado incompatíveis MUST NOT ser apresentados. A descoberta SHALL retornar somente dados de sugestão e MUST NOT persistir, atualizar ou registrar histórico de ação.

#### Scenario: Busca parcial americana
- **WHEN** o cliente pesquisa um prefixo ou nome compatível com ação comum elegível dos Estados Unidos
- **THEN** o sistema retorna sugestão com ticker, nome quando disponível, mercado Estados Unidos e moeda USD, sem escrita

#### Scenario: Resultado americano incompatível
- **WHEN** a fonte retorna resultado com país, tipo de instrumento ou moeda incompatíveis
- **THEN** o sistema exclui o resultado das sugestões e não persiste ação

### Requirement: Seleção americana confirma dados antes de cadastrar
Uma sugestão americana selecionada SHALL passar pela mesma validação completa de instrumento e cotação usada no cadastro explícito. O sistema MUST confirmar correspondência exata de ticker, ação comum dos Estados Unidos, moeda USD, preço positivo representável e timestamp válido antes de persistir.

#### Scenario: Sugestão americana não confirmada
- **WHEN** a descoberta retorna sugestão mas a resolução não confirma dados americanos elegíveis e completos
- **THEN** o sistema responde com a classificação externa aplicável e não persiste ação
