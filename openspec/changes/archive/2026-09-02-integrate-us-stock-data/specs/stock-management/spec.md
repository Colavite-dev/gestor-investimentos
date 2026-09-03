## MODIFIED Requirements

### Requirement: Cadastro brasileiro resolvido por provider
O sistema SHALL disponibilizar `POST /acoes` para cadastro de uma ação recebendo exclusivamente `ticker` e `mercado`. O mercado SHALL ser obrigatório e selecionar deterministicamente a fonte externa: Brasil usa a fonte brasileira e Estados Unidos usa a fonte americana. O sistema SHALL normalizar o ticker e preencher exclusivamente a partir da resolução externa o ticker persistido, nome da empresa, moeda, cotação atual e data/hora da cotação; o mercado persistido SHALL corresponder ao mercado solicitado e validado pela fonte. O cliente MUST NOT informar nem sobrescrever moeda, nome da empresa, cotação ou data/hora, e o endpoint MUST NOT aceitar dados manuais novamente.

#### Scenario: Cadastro válido
- **WHEN** o cliente envia ticker válido, um mercado suportado e o ativo ainda não está cadastrado
- **THEN** o sistema seleciona a fonte correspondente, persiste o ativo resolvido, responde `201 Created`, devolve os dados obtidos e derivados e informa o URI `/acoes/{id}` no header `Location`

#### Scenario: Cadastro americano válido
- **WHEN** o cliente envia ticker americano válido, mercado `ESTADOS_UNIDOS` e o ativo ainda não está cadastrado
- **THEN** o sistema seleciona a fonte americana, persiste o ativo resolvido com moeda USD, responde `201 Created`, devolve os dados obtidos e derivados e informa o URI `/acoes/{id}` no header `Location`

#### Scenario: Dados de cadastro inválidos
- **WHEN** o body não contém ticker ou mercado válidos ou contém atributos adicionais de cadastro manual
- **THEN** o sistema responde `400 Bad Request`, não consulta uma fonte externa quando a falha é local e não persiste o ativo
