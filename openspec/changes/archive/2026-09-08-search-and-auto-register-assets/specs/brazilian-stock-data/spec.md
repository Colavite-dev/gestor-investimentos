## ADDED Requirements

### Requirement: Descoberta de sugestões brasileiras sem escrita
Para uma pesquisa de ações, o sistema SHALL consultar a capacidade de busca da fonte brasileira para obter sugestões da B3 compatíveis com o termo informado. O sistema MUST filtrar e retornar somente ações brasileiras elegíveis ao domínio; fundos, ETFs, FIIs, BDRs, opções, futuros, criptomoedas, índices e resultados sem ticker válido MUST NOT ser apresentados como sugestões. A descoberta SHALL retornar somente dados de sugestão e MUST NOT cadastrar, alterar cotação nem criar histórico.

#### Scenario: Busca parcial brasileira
- **WHEN** o cliente pesquisa um prefixo ou nome compatível com ações brasileiras elegíveis
- **THEN** o sistema retorna sugestões brasileiras com ticker, nome quando disponível, mercado Brasil e moeda BRL, sem escrita

#### Scenario: Busca brasileira sem correspondência elegível
- **WHEN** a fonte não retorna ação brasileira elegível para o termo
- **THEN** o sistema contribui com nenhuma sugestão brasileira e não persiste ação

### Requirement: Seleção brasileira mantém a identidade confirmada
Após uma sugestão brasileira ser selecionada para resolução, o sistema SHALL aplicar integralmente as regras de ticker canônico já vigentes: ticker solicitado, `requestedSymbol` e `symbol` normalizados MUST coincidir e `changed` MUST ser `false`. A sugestão descoberta MUST NOT autorizar alias, renomeação ou substituição automática de ticker.

#### Scenario: Sugestão brasileira divergente ao resolver
- **WHEN** a resolução de sugestão brasileira informa símbolo diferente, `requestedSymbol` diferente ou `changed=true`
- **THEN** o sistema responde `502 Bad Gateway` e não persiste ação
