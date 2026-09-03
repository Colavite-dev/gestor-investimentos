# Delta Specification: stock-quote-history

## MODIFIED Requirements

### Requirement: Registrar observações de cotação

O sistema SHALL persistir uma observação imutável contendo ação, cotação positiva e timestamp da fonte sempre que um cadastro ou atualização de cotação for concluído com dados externos válidos. A observação MUST usar os mesmos dados aceitos para atualizar a cotação atual e MUST NOT ser criada para respostas inválidas ou falhas externas. O cadastro inicial e cada atualização MUST gravar a cotação atual e sua observação histórica como uma única unidade atômica: se qualquer uma das duas persistências falhar, a operação MUST falhar e nenhuma alteração parcial poderá permanecer persistida.

#### Scenario: Cadastro cria histórico inicial

- **WHEN** uma ação é cadastrada com cotação externa válida
- **THEN** o sistema persiste a ação e uma observação histórica correspondente na mesma operação, sem dados fictícios

#### Scenario: Falha no histórico durante cadastro desfaz ação

- **WHEN** a persistência da observação histórica inicial falha
- **THEN** o sistema falha o cadastro e não deixa a ação nem uma observação parcial persistidas

#### Scenario: Atualização cria nova observação

- **WHEN** a cotação de uma ação existente é atualizada com sucesso
- **THEN** o sistema atualiza a cotação atual e acrescenta uma nova observação com a mesma cotação e o mesmo timestamp da fonte, sem alterar observações anteriores

#### Scenario: Falha no histórico durante atualização desfaz cotação atual

- **WHEN** a persistência da nova observação histórica falha durante uma atualização
- **THEN** o sistema falha a atualização, preserva no banco a cotação atual e o timestamp anteriores e não acrescenta observação parcial

#### Scenario: Falha não cria histórico

- **WHEN** o provider retorna erro, preço inválido ou timestamp inválido
- **THEN** o sistema não persiste nem altera a ação ou a observação histórica correspondente

### Requirement: Consultar histórico por ação

O sistema SHALL disponibilizar `GET /acoes/{id}/historico-cotacoes`, retornando somente DTOs com identificador, cotação, timestamp da cotação e data de registro. O resultado MUST ser ordenado por `dataHoraCotacao` ascendente e, em empate, por identificador ascendente.

#### Scenario: Histórico existente

- **WHEN** o cliente consulta o histórico de uma ação cadastrada sem parâmetros de período
- **THEN** o sistema responde `200 OK` com todas as observações em ordem determinística

#### Scenario: Histórico vazio

- **WHEN** o cliente consulta uma ação cadastrada sem observações
- **THEN** o sistema responde `200 OK` com uma coleção vazia

#### Scenario: Somente limite inicial

- **WHEN** o cliente informa somente `de` em ISO-8601 válido
- **THEN** o sistema retorna observações com timestamp maior ou igual a `de`, preservando a ordenação

#### Scenario: Somente limite final

- **WHEN** o cliente informa somente `ate` em ISO-8601 válido
- **THEN** o sistema retorna observações com timestamp menor ou igual a `ate`, preservando a ordenação

#### Scenario: Período válido com dois limites

- **WHEN** o cliente informa `de` e `ate` válidos
- **THEN** o sistema retorna somente observações dentro dos dois limites inclusivos, preservando a ordenação

#### Scenario: Empate de timestamp

- **WHEN** duas ou mais observações da mesma ação têm o mesmo timestamp de cotação
- **THEN** o sistema as retorna por identificador crescente

#### Scenario: Intervalo ou timestamp inválido

- **WHEN** `de` é posterior a `ate` ou qualquer parâmetro de período não está em formato ISO-8601 válido
- **THEN** o sistema responde `400 Bad Request` sem consultar providers ou persistir alterações

#### Scenario: Ação inexistente

- **WHEN** o cliente consulta o histórico de um ID não cadastrado
- **THEN** o sistema responde `404 Not Found` sem consultar providers externos

### Requirement: Filtrar período sem alterar dados

O endpoint de histórico SHALL aceitar opcionalmente os parâmetros `de` e `ate` em formato ISO-8601. O filtro SHALL incluir os limites informados, rejeitar intervalo em que `de` seja posterior a `ate` com `400 Bad Request` e MUST NOT alterar qualquer registro.

#### Scenario: Período válido

- **WHEN** o cliente consulta o histórico com nenhum, um ou dois limites válidos
- **THEN** a consulta não cria, altera ou exclui ação nem observação histórica

#### Scenario: Intervalo inválido

- **WHEN** `de` é posterior a `ate`
- **THEN** o sistema responde `400 Bad Request` e não altera registros
