# us-stock-data Specification

## Purpose

Permitir o cadastro confiável de ações comuns dos Estados Unidos com dados de mercado obtidos da Twelve Data, mantendo contrato externo, credencial e falhas técnicas isolados do domínio.

## Requirements

### Requirement: Resolução de ação americana por fonte externa
O sistema SHALL resolver uma ação do mercado Estados Unidos antes de seu cadastro por uma fonte externa de dados de mercado. A resolução SHALL produzir somente ticker, nome da empresa, moeda, cotação atual e data/hora da cotação necessários ao domínio, sem expor o contrato externo à API do sistema. A fonte SHALL ser consultada para descoberta do instrumento e para sua cotação mais recente.

#### Scenario: Resolução de ação americana válida
- **WHEN** o cliente solicita o cadastro de uma ação americana existente e a fonte retorna dados completos e válidos
- **THEN** o sistema resolve o ativo com ticker normalizado, nome da empresa, moeda USD, cotação atual e data/hora da fonte

#### Scenario: Ticker inexistente
- **WHEN** a fonte não encontra instrumento correspondente ao ticker solicitado
- **THEN** o sistema responde `422 Unprocessable Content` e não persiste uma ação

### Requirement: Elegibilidade de ação comum dos Estados Unidos
O sistema SHALL aceitar o instrumento americano somente quando a descoberta externa retornar correspondência exata para o ticker normalizado, `country` igual a `United States`, `instrument_type` igual a `Common Stock` e moeda USD. Criptomoedas, forex, índices, ETFs, fundos, instrumentos de outros países e outros tipos incompatíveis MUST NOT ser cadastrados por esse fluxo.

#### Scenario: Instrumento incompatível
- **WHEN** a descoberta retorna um ticker existente cuja categoria ou país não atende aos critérios de ação comum dos Estados Unidos
- **THEN** o sistema responde `422 Unprocessable Content` e não consulta ou persiste uma cotação para o instrumento incompatível

### Requirement: Integridade da cotação americana
O sistema SHALL aceitar a cotação somente quando a fonte retornar o ticker, nome não vazio, moeda USD, preço de fechamento positivo e timestamp válido. A data/hora persistida SHALL derivar do timestamp da fonte; dados ausentes, incompatíveis ou impossíveis de converter MUST NOT ser persistidos.

#### Scenario: Resposta de cotação incompatível
- **WHEN** a fonte retorna cotação sem um dado obrigatório, com moeda diferente de USD, preço inválido ou timestamp ausente ou inválido
- **THEN** o sistema responde `502 Bad Gateway` pelo contrato centralizado e não persiste uma ação

### Requirement: Falhas controladas da fonte de ações americanas
O sistema SHALL converter timeout, falha de conexão, HTTP 429, HTTP 5xx, indisponibilidade e falha de autenticação ou configuração da fonte em indisponibilidade externa. Corpo de erro, URL, credencial e detalhes do provider MUST NOT ser expostos pela API do sistema.

#### Scenario: Rate limit ou indisponibilidade externa
- **WHEN** a fonte responde HTTP 429, HTTP 5xx, falha técnica ou não responde dentro do timeout configurado
- **THEN** o sistema responde `503 Service Unavailable` pelo contrato centralizado e não persiste uma ação

### Requirement: Credencial da Twelve Data segura e configurável
O sistema SHALL obter a chave da Twelve Data exclusivamente de `TWELVE_DATA_API_KEY` e enviá-la somente na chamada externa por mecanismo de autenticação suportado pela fonte. A chave MUST NOT ser aceita no request público, persistida, retornada, registrada em logs ou incluída em arquivos de exemplo com valor.

#### Scenario: Chave configurada no ambiente
- **WHEN** a aplicação consulta a Twelve Data com `TWELVE_DATA_API_KEY` configurada
- **THEN** a credencial é enviada apenas à fonte externa e permanece ausente da resposta e dos logs da aplicação
