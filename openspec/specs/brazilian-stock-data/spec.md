# brazilian-stock-data Specification

## Purpose

Resolver dados reais e atuais de ações brasileiras antes de seu cadastro, mantendo a dependência da brapi.dev isolada do domínio de ações.

## Requirements

### Requirement: Resolução de ação brasileira por fonte externa
O sistema SHALL consultar uma fonte externa de dados de ações brasileiras para resolver um ticker normalizado antes de cadastrar uma ação brasileira. A resolução SHALL produzir apenas ticker canônico, nome da empresa, moeda, cotação atual e data/hora da cotação necessários ao domínio, sem expor o contrato externo à API do sistema. Somente resultados válidos do recurso externo destinado a ações poderão ser aceitos nesta capability; FIIs, fundos, ETFs, opções, futuros, criptomoedas e ativos de mercados não brasileiros MUST NOT ser cadastrados por esse fluxo.

#### Scenario: Resolução de ação brasileira válida
- **WHEN** o cliente solicita o cadastro de um ticker brasileiro existente e a fonte retorna dados completos e válidos de ação
- **THEN** o sistema resolve o ativo com nome da empresa, moeda BRL, cotação atual e data/hora informados pela fonte

#### Scenario: Ticker inexistente ou não reconhecido
- **WHEN** a fonte não encontra um resultado de ação para o ticker solicitado
- **THEN** o sistema responde `422 Unprocessable Content` e não persiste uma ação

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

### Requirement: Integridade dos dados externos de mercado
O sistema SHALL aceitar a resolução somente quando a fonte retornar um ticker canônico, nome da empresa não vazio, moeda BRL, cotação atual positiva e data/hora de cotação válidas. Dados ausentes, incompatíveis ou impossíveis de converter MUST NOT ser persistidos.

#### Scenario: Resposta externa incompatível
- **WHEN** a fonte retorna resposta sem um dos dados obrigatórios ou com moeda, preço ou data/hora incompatíveis
- **THEN** o sistema responde `502 Bad Gateway` pelo contrato centralizado e não persiste uma ação

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
- **THEN** o sistema responde `503 Service Unavailable` pelo contrato centralizado e não persiste uma ação

### Requirement: Autenticação externa segura e configurável
O sistema SHALL suportar token opcional de autenticação da fonte externa por configuração de ambiente e SHALL enviá-lo em header de autorização somente quando estiver presente. O token MUST NOT ser aceito no request público, persistido, retornado, registrado em logs ou incluído em arquivos de exemplo com valor.

#### Scenario: Consulta sem token de ambiente
- **WHEN** não há token configurado e o ticker é acessível sem autenticação pela fonte
- **THEN** o sistema realiza a resolução sem enviar credencial fictícia

#### Scenario: Consulta com token configurado
- **WHEN** há token configurado no ambiente
- **THEN** o sistema envia a credencial exclusivamente no header de autorização da chamada externa


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
### Requirement: Catálogo brasileiro usa a listagem paginada da brapi

O sistema SHALL consultar a listagem oficial da brapi com `type=stock`, encaminhar busca e paginação suportadas e aceitar somente ações elegíveis do mercado brasileiro em BRL. SHALL mapear ticker, nome, cotação e logo quando os valores válidos estiverem presentes.

#### Scenario: Página BR com enriquecimento nativo

- **WHEN** a brapi retorna uma página válida contendo cotação e logo
- **THEN** o sistema retorna esses dados na mesma página sem chamadas adicionais por símbolo

#### Scenario: Paginação BR

- **WHEN** a brapi informa metadados de página e de continuidade
- **THEN** o sistema preserva metadados equivalentes para a navegação do cliente

#### Scenario: Item não elegível

- **WHEN** a resposta contém fundo, BDR ou item incompatível com ação brasileira em BRL
- **THEN** o item não integra o catálogo de ações BR
