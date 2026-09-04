## MODIFIED Requirements

### Requirement: Elegibilidade de ação comum dos Estados Unidos
O sistema SHALL aceitar o instrumento americano somente quando a descoberta externa retornar uma única correspondência exata para o ticker normalizado, `country` igual a `United States`, `instrument_type` igual a `Common Stock` e moeda USD. Criptomoedas, forex, índices, ETFs, fundos, instrumentos de outros países, outros tipos incompatíveis e múltiplas correspondências elegíveis para o mesmo ticker MUST NOT ser cadastrados por esse fluxo.

#### Scenario: Instrumento incompatível
- **WHEN** a descoberta retorna um ticker existente cuja categoria ou país não atende aos critérios de ação comum dos Estados Unidos
- **THEN** o sistema responde `422 Unprocessable Content` e não consulta ou persiste uma cotação para o instrumento incompatível

#### Scenario: Símbolo ambíguo
- **WHEN** a descoberta retorna mais de uma correspondência elegível e exata para o ticker normalizado
- **THEN** o sistema responde `422 Unprocessable Content` e não escolhe arbitrariamente nem consulta uma cotação

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
- **THEN** o sistema responde `503 Service Unavailable` pelo contrato centralizado e não persiste nem altera uma ação
