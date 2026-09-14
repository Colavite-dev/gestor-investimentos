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
O sistema SHALL aceitar o instrumento americano somente quando a descoberta externa retornar ticker exato normalizado, `country` igual a `United States`, `instrument_type` igual a `Common Stock` e moeda USD. Quando existir mais de uma correspondência elegível para o mesmo ticker, o sistema SHALL selecionar um venue somente por prioridade explícita do par `exchange` e `mic_code`, sem depender da ordem do payload externo. Os venues principais reconhecidos SHALL ser `NASDAQ`/`XNGS`, `NYSE`/`XNYS` e `NYSE American`/`XASE`; um único match em um desses venues SHALL prevalecer sobre IEX. `IEX`/`IEXG` SHALL ser aceito somente quando não houver venue principal e for o único match elegível remanescente. Criptomoedas, forex, índices, ETFs, fundos, instrumentos de outros países, outros tipos incompatíveis e ambiguidades que permaneçam após a prioridade MUST NOT ser cadastrados por esse fluxo.

#### Scenario: Venue principal prevalece sobre IEX
- **WHEN** a descoberta retorna AAPL elegível em `NASDAQ`/`XNGS` e em `IEX`/`IEXG`
- **THEN** o sistema seleciona o instrumento `NASDAQ`/`XNGS`, consulta sua cotação e pode resolver a ação em USD

#### Scenario: Ordem externa não muda a seleção
- **WHEN** os mesmos matches elegíveis de venue principal e IEX chegam em ordem inversa
- **THEN** o sistema seleciona o mesmo venue principal e produz o mesmo resultado de resolução

#### Scenario: IEX é o único venue alternativo elegível
- **WHEN** não há venue principal e existe exatamente um match elegível em `IEX`/`IEXG`
- **THEN** o sistema aceita esse instrumento e continua a validação da cotação normalmente

#### Scenario: Instrumento incompatível
- **WHEN** a descoberta retorna um ticker existente cuja categoria ou país não atende aos critérios de ação comum dos Estados Unidos
- **THEN** o sistema responde `422 Unprocessable Content` e não consulta ou persiste uma cotação para o instrumento incompatível

#### Scenario: Símbolo ambíguo
- **WHEN** a descoberta retorna dois ou mais matches elegíveis com a mesma maior prioridade ou sem uma prioridade reconhecida única
- **THEN** o sistema responde `422 Unprocessable Content`, informa que o ticker possui venues ambíguos e não consulta cotação nem persiste uma ação

### Requirement: Ambiguidade de venue é distinguida de ticker inexistente
O sistema SHALL manter `422 Unprocessable Content` para ticker inexistente, instrumento incompatível ou ambiguidade de venue, mas MUST usar mensagem controlada que não declare ticker inexistente quando há mais de um venue elegível sem prioridade determinística. A resposta MUST NOT expor corpo externo, URL, credencial ou detalhes internos da Twelve Data.

#### Scenario: Ticker inexistente
- **WHEN** a fonte não retorna um instrumento elegível correspondente ao ticker solicitado
- **THEN** o sistema responde `422 Unprocessable Content` com a mensagem controlada de ticker não encontrado ou incompatível

#### Scenario: Venue não resolvível
- **WHEN** a fonte retorna múltiplos venues elegíveis e a prioridade não produz uma única escolha
- **THEN** o sistema responde `422 Unprocessable Content` com mensagem controlada de ambiguidade de venue

### Requirement: Integridade da cotação americana
O sistema SHALL aceitar a cotação somente quando a fonte retornar o ticker, nome não vazio, moeda USD, preço de fechamento positivo e timestamp válido. A data/hora persistida SHALL derivar do timestamp da fonte; dados ausentes, incompatíveis ou impossíveis de converter MUST NOT ser persistidos.

#### Scenario: Resposta de cotação incompatível
- **WHEN** a fonte retorna cotação sem um dado obrigatório, com moeda diferente de USD, preço inválido ou timestamp ausente ou inválido
- **THEN** o sistema responde `502 Bad Gateway` pelo contrato centralizado e não persiste uma ação

### Requirement: Falhas controladas da fonte de ações americanas
O sistema SHALL classificar erros HTTP e erros estruturados retornados no corpo com a mesma semântica, sem expor corpo de erro, URL, credencial ou detalhes do provider. Erro que identifique de forma confiável o ticker como inexistente, não suportado, incompatível ou ambíguo SHALL resultar em `422 Unprocessable Content`, independentemente de ter chegado em HTTP `200`, HTTP `400` ou HTTP `404`. Resposta `400`, `414` ou outro conteúdo de erro genérico, malformado ou sem semântica confiável de ticker SHALL resultar em `502 Bad Gateway`. HTTP `401`, `403`, `429`, `5xx`, timeout, falha de conexão, indisponibilidade e autenticação/configuração externa necessária SHALL resultar em `503 Service Unavailable`.

#### Scenario: Erro de ticker equivalente em formatos distintos
- **WHEN** a fonte informa de forma estruturada ou em resposta HTTP que o ticker solicitado é inválido, inexistente ou não suportado
- **THEN** o sistema responde `422 Unprocessable Content` nos dois formatos e não persiste nem altera uma ação

#### Scenario: Resposta externa de erro genérica ou inválida
- **WHEN** a fonte retorna erro HTTP ou estruturado cujo conteúdo é malformado, genérico ou incompatível e não permite concluir que o ticker é inválido
- **THEN** o sistema responde `502 Bad Gateway` e não persiste nem altera uma ação

#### Scenario: Rate limit ou indisponibilidade externa
- **WHEN** a fonte responde HTTP `401`, `403`, `429` ou `5xx`, sofre timeout, falha de conexão ou não responde dentro do timeout configurado
- **THEN** o sistema responde `503 Service Unavailable` pelo contrato centralizado e não persiste uma ação

### Requirement: Credencial da Twelve Data segura e configurável
O sistema SHALL obter a chave da Twelve Data exclusivamente de `TWELVE_DATA_API_KEY` e enviá-la somente na chamada externa por mecanismo de autenticação suportado pela fonte. A chave MUST NOT ser aceita no request público, persistida, retornada, registrada em logs ou incluída em arquivos de exemplo com valor.

#### Scenario: Chave configurada no ambiente
- **WHEN** a aplicação consulta a Twelve Data com `TWELVE_DATA_API_KEY` configurada
- **THEN** a credencial é enviada apenas à fonte externa e permanece ausente da resposta e dos logs da aplicação


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
### Requirement: Catálogo americano usa dados de referência da Twelve Data

O sistema SHALL usar `/stocks` como fonte do inventário, filtrando `country=United States`, `type=Common Stock` e moeda USD. SHALL mapear símbolo, nome, moeda, exchange e MIC quando disponíveis, sem tratar o banco local como catálogo.

#### Scenario: Catálogo US válido
- **WHEN** a Twelve Data retorna seu inventário de ações
- **THEN** somente Common Stocks dos Estados Unidos em USD são disponibilizadas e paginadas pela aplicação

#### Scenario: Mesmo ticker em exchanges incompatíveis
- **WHEN** metadados não permitem identificar de forma segura uma ação US elegível
- **THEN** o item ambíguo ou incompatível é descartado em vez de receber identidade inventada

### Requirement: Ações US não usam serviço externo de logo

O sistema MUST NOT chamar `/logo` da Twelve Data nem outro serviço externo de logo para ações US em qualquer fluxo, incluindo catálogo, pesquisa, seleção, resolução, uso e atualização. O catálogo MUST omitir `logoUrl`, e o frontend SHALL usar somente seu ícone padrão. O sistema também MUST NOT chamar `/quote` individualmente para cada item do catálogo; o inventário de referência MAY ser mantido em cache volátil, e cotações SHALL continuar sendo consultadas pontualmente nos fluxos de uso ou atualização do ativo.

#### Scenario: Página US exibida
- **WHEN** uma página contém vários ativos americanos
- **THEN** ela é produzida a partir do inventário de referência sem chamada de cotação por item e sem qualquer chamada de logo

#### Scenario: Ação US selecionada ou utilizada
- **WHEN** uma ação US é pesquisada, selecionada, resolvida, utilizada ou atualizada
- **THEN** a integração pode usar os endpoints de dados e cotação previstos, mas nunca consulta `/logo`

#### Scenario: Cache expira
- **WHEN** o cache volátil do inventário expira
- **THEN** uma consulta posterior pode recarregar `/stocks`, sem persistir o catálogo em tabelas da aplicação
