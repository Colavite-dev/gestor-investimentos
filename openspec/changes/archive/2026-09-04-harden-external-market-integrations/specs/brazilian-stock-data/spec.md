## MODIFIED Requirements

### Requirement: Ticker canônico e prevenção de duplicidade após resolução
O sistema SHALL aceitar uma resolução brasileira somente quando `requestedSymbol` e `symbol` retornados pela fonte, após normalização, forem ambos iguais ao ticker normalizado solicitado e `changed` for explicitamente `false`. O sistema MUST NOT seguir automaticamente alias, renomeação ou outro ticker retornado pela fonte; resposta ausente, inconsistente ou que indique mudança de símbolo SHALL ser tratada como conteúdo externo incompatível. A verificação de duplicidade SHALL usar o ticker confirmado e o mercado Brasil antes da persistência.

#### Scenario: Ticker confirmado pela fonte
- **WHEN** a fonte retorna `requestedSymbol` e `symbol` iguais ao ticker normalizado solicitado e `changed=false`
- **THEN** o sistema usa esse ticker confirmado para verificar duplicidade e persistir a ação

#### Scenario: Ticker renomeado
- **WHEN** a fonte informa `changed=true` ou retorna `requestedSymbol` ou `symbol` diferente do ticker solicitado
- **THEN** o sistema responde `502 Bad Gateway` e não persiste nem altera uma ação

#### Scenario: Duplicidade por ticker renomeado
- **WHEN** o ticker solicitado é resolvido pela fonte para outro ticker que já está cadastrado no mercado Brasil
- **THEN** o sistema responde `502 Bad Gateway` e não usa a renomeação implícita para consultar nem persistir uma ação

#### Scenario: Duplicidade por ticker confirmado
- **WHEN** o ticker confirmado já está cadastrado no mercado Brasil
- **THEN** o sistema responde `409 Conflict` e não persiste um segundo ativo

### Requirement: Falhas controladas da fonte de ações brasileiras
O sistema SHALL classificar pela semântica da resposta da fonte, sem expor corpo, token, URL ou detalhes internos. Resultado vazio ou HTTP `404` que indiquem ticker não encontrado SHALL resultar em `422 Unprocessable Content`. Conteúdo JSON ausente, malformado ou incompatível — inclusive resposta `400` ou outro `4xx` que não identifique de forma confiável ticker inexistente — SHALL resultar em `502 Bad Gateway`. Timeout, falha de conexão, HTTP `401`, `403`, `429`, `5xx`, indisponibilidade, autenticação/configuração externa necessária SHALL resultar em `503 Service Unavailable`.

#### Scenario: Ticker inexistente
- **WHEN** a fonte informa ticker inexistente por resultado vazio ou HTTP `404`
- **THEN** o sistema responde `422 Unprocessable Content` e não persiste nem altera uma ação

#### Scenario: Conteúdo ou resposta incompatível
- **WHEN** a fonte retorna payload inválido, resposta inconsistente ou `4xx` cuja semântica não confirme ticker inexistente
- **THEN** o sistema responde `502 Bad Gateway` e não persiste nem altera uma ação

#### Scenario: Rate limit ou indisponibilidade externa
- **WHEN** a fonte responde HTTP `401`, `403`, `429` ou `5xx`, sofre timeout, falha de conexão ou não responde dentro do timeout configurado
- **THEN** o sistema responde `503 Service Unavailable` pelo contrato centralizado e não persiste nem altera uma ação
