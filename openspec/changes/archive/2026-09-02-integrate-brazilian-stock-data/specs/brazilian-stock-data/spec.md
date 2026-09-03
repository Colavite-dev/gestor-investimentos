## Purpose

Resolver dados reais e atuais de ações brasileiras antes de seu cadastro, mantendo a dependência da brapi.dev isolada do domínio de ações.

## ADDED Requirements

### Requirement: Resolução de ação brasileira por fonte externa
O sistema SHALL consultar uma fonte externa de dados de ações brasileiras para resolver um ticker normalizado antes de cadastrar uma ação brasileira. A resolução SHALL produzir apenas ticker canônico, nome da empresa, moeda, cotação atual e data/hora da cotação necessários ao domínio, sem expor o contrato externo à API do sistema. Somente resultados válidos do recurso externo destinado a ações poderão ser aceitos nesta capability; FIIs, fundos, ETFs, opções, futuros, criptomoedas e ativos de mercados não brasileiros MUST NOT ser cadastrados por esse fluxo.

#### Scenario: Resolução de ação brasileira válida
- **WHEN** o cliente solicita o cadastro de um ticker brasileiro existente e a fonte retorna dados completos e válidos de ação
- **THEN** o sistema resolve o ativo com nome da empresa, moeda BRL, cotação atual e data/hora informados pela fonte

#### Scenario: Ticker inexistente ou não reconhecido
- **WHEN** a fonte não encontra um resultado de ação para o ticker solicitado
- **THEN** o sistema responde `422 Unprocessable Content` e não persiste uma ação

### Requirement: Ticker canônico e prevenção de duplicidade após resolução
O sistema SHALL comparar e persistir o ticker canônico retornado pela fonte para o mercado Brasil. Quando o ticker solicitado divergir do ticker canônico por renome conhecido, o sistema MUST NOT manter o código antigo como identidade persistida. A verificação de duplicidade SHALL usar o ticker canônico e o mercado Brasil antes da persistência.

#### Scenario: Ticker renomeado
- **WHEN** a fonte informa que o ticker solicitado foi resolvido para outro ticker canônico
- **THEN** o sistema persiste ou consulta a identidade usando o ticker canônico retornado

#### Scenario: Duplicidade por ticker renomeado
- **WHEN** o ticker canônico resolvido já está cadastrado no mercado Brasil
- **THEN** o sistema responde `409 Conflict` e não persiste um segundo ativo

### Requirement: Integridade dos dados externos de mercado
O sistema SHALL aceitar a resolução somente quando a fonte retornar um ticker canônico, nome da empresa não vazio, moeda BRL, cotação atual positiva e data/hora de cotação válidas. Dados ausentes, incompatíveis ou impossíveis de converter MUST NOT ser persistidos.

#### Scenario: Resposta externa incompatível
- **WHEN** a fonte retorna resposta sem um dos dados obrigatórios ou com moeda, preço ou data/hora incompatíveis
- **THEN** o sistema responde `502 Bad Gateway` pelo contrato centralizado e não persiste uma ação

### Requirement: Falhas controladas da fonte de ações brasileiras
O sistema SHALL tratar timeout, falha de conexão, indisponibilidade, falha de autenticação/configuração do provider e limitação de requisições como indisponibilidade externa, sem expor corpo, token ou detalhes internos da fonte.

#### Scenario: Rate limit ou indisponibilidade externa
- **WHEN** a fonte responde rate limit, falha técnica ou não responde dentro do timeout configurado
- **THEN** o sistema responde `503 Service Unavailable` pelo contrato centralizado e não persiste uma ação

### Requirement: Autenticação externa segura e configurável
O sistema SHALL suportar token opcional de autenticação da fonte externa por configuração de ambiente e SHALL enviá-lo em header de autorização somente quando estiver presente. O token MUST NOT ser aceito no request público, persistido, retornado, registrado em logs ou incluído em arquivos de exemplo com valor.

#### Scenario: Consulta sem token de ambiente
- **WHEN** não há token configurado e o ticker é acessível sem autenticação pela fonte
- **THEN** o sistema realiza a resolução sem enviar credencial fictícia

#### Scenario: Consulta com token configurado
- **WHEN** há token configurado no ambiente
- **THEN** o sistema envia a credencial exclusivamente no header de autorização da chamada externa
