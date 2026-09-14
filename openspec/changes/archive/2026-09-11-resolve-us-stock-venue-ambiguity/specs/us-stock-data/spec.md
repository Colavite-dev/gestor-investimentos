## MODIFIED Requirements

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

#### Scenario: Símbolo ambíguo
- **WHEN** a descoberta retorna dois ou mais matches elegíveis com a mesma maior prioridade ou sem uma prioridade reconhecida única
- **THEN** o sistema responde `422 Unprocessable Content`, informa que o ticker possui venues ambíguos e não consulta cotação nem persiste uma ação

#### Scenario: Instrumento incompatível
- **WHEN** a descoberta retorna um ticker existente cuja categoria, país ou moeda não atende aos critérios de ação comum dos Estados Unidos
- **THEN** o sistema responde `422 Unprocessable Content` e não consulta ou persiste uma cotação para o instrumento incompatível

## ADDED Requirements

### Requirement: Ambiguidade de venue é distinguida de ticker inexistente
O sistema SHALL manter `422 Unprocessable Content` para ticker inexistente, instrumento incompatível ou ambiguidade de venue, mas MUST usar mensagem controlada que não declare ticker inexistente quando há mais de um venue elegível sem prioridade determinística. A resposta MUST NOT expor corpo externo, URL, credencial ou detalhes internos da Twelve Data.

#### Scenario: Ticker inexistente
- **WHEN** a fonte não retorna um instrumento elegível correspondente ao ticker solicitado
- **THEN** o sistema responde `422 Unprocessable Content` com a mensagem controlada de ticker não encontrado ou incompatível

#### Scenario: Venue não resolvível
- **WHEN** a fonte retorna múltiplos venues elegíveis e a prioridade não produz uma única escolha
- **THEN** o sistema responde `422 Unprocessable Content` com mensagem controlada de ambiguidade de venue
