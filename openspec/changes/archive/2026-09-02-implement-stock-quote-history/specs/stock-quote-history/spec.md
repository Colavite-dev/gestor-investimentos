## Purpose

Registrar e consultar observações históricas de cotações reais utilizadas pelo sistema, preservando uma visão auditável da evolução dos ativos sem depender de chamadas externas durante a consulta.

## ADDED Requirements

### Requirement: Registrar observações de cotação

O sistema SHALL persistir uma observação imutável contendo ação, cotação positiva e timestamp da fonte sempre que um cadastro ou atualização de cotação for concluído com dados externos válidos. A observação MUST usar os mesmos dados aceitos para atualizar a cotação atual e MUST NOT ser criada para respostas inválidas ou falhas externas.

#### Scenario: Cadastro cria histórico inicial

- **WHEN** uma ação é cadastrada com cotação externa válida
- **THEN** o sistema persiste a ação e uma observação histórica correspondente na mesma operação, sem dados fictícios

#### Scenario: Atualização cria nova observação

- **WHEN** a cotação de uma ação existente é atualizada com sucesso
- **THEN** o sistema atualiza a cotação atual e acrescenta uma nova observação sem alterar observações anteriores

#### Scenario: Falha não cria histórico

- **WHEN** o provider retorna erro, preço inválido ou timestamp inválido
- **THEN** o sistema não persiste nem altera a observação histórica da ação

### Requirement: Consultar histórico por ação

O sistema SHALL disponibilizar `GET /acoes/{id}/historico-cotacoes`, retornando somente DTOs com identificador, cotação, timestamp da cotação e data de registro. O resultado MUST ser ordenado por timestamp ascendente e, em empate, por identificador ascendente.

#### Scenario: Histórico existente

- **WHEN** o cliente consulta o histórico de uma ação cadastrada
- **THEN** o sistema responde `200 OK` com as observações em ordem determinística

#### Scenario: Histórico vazio

- **WHEN** o cliente consulta uma ação cadastrada sem observações
- **THEN** o sistema responde `200 OK` com uma coleção vazia

#### Scenario: Ação inexistente

- **WHEN** o cliente consulta o histórico de um ID não cadastrado
- **THEN** o sistema responde `404 Not Found` sem consultar providers externos

### Requirement: Filtrar período sem alterar dados

O endpoint de histórico SHALL aceitar opcionalmente os parâmetros `de` e `ate` em formato ISO-8601. O filtro SHALL incluir os limites informados, rejeitar intervalo em que `de` seja posterior a `ate` com `400 Bad Request` e MUST NOT alterar qualquer registro.

#### Scenario: Período válido

- **WHEN** o cliente informa limites de período válidos
- **THEN** o sistema retorna somente observações dentro dos limites, preservando a ordenação

#### Scenario: Intervalo inválido

- **WHEN** `de` é posterior a `ate` ou possui formato inválido
- **THEN** o sistema responde `400 Bad Request` sem consultar providers ou persistir alterações

### Requirement: Integridade e escopo do histórico

O sistema MUST vincular cada observação a uma ação existente por chave estrangeira, preservar a moeda da ação por meio da referência ao ativo e não permitir edição ou exclusão pública do histórico. A consulta SHALL ser somente leitura e não converter moedas.

#### Scenario: Moedas distintas

- **WHEN** o cliente consulta históricos de ações BRL e USD separadamente
- **THEN** cada observação permanece associada à sua ação e nenhuma conversão cambial é aplicada
