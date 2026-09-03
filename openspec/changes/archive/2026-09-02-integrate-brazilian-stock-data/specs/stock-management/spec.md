## RENAMED Requirements

- FROM: `### Requirement: Cadastro transitório sem provider de cotação`
- TO: `### Requirement: Cadastro brasileiro resolvido por provider`

## MODIFIED Requirements

### Requirement: Cadastro brasileiro resolvido por provider
O sistema SHALL disponibilizar `POST /acoes` para cadastro de uma ação brasileira recebendo somente `ticker`. O sistema SHALL normalizar o ticker e preencher exclusivamente a partir da resolução externa o ticker persistido, nome da empresa, mercado Brasil, moeda BRL, cotação atual e data/hora da cotação. O cliente MUST NOT informar nem sobrescrever mercado, moeda, nome da empresa, cotação ou data/hora neste fluxo. A evolução para ativos dos Estados Unidos será definida por capability posterior e MUST NOT fazer este endpoint aceitar dados manuais novamente.

#### Scenario: Cadastro válido
- **WHEN** o cliente envia um ticker brasileiro válido ainda não cadastrado
- **THEN** o sistema persiste o ativo resolvido, responde `201 Created`, devolve os dados obtidos e derivados e informa o URI `/acoes/{id}` no header `Location`

#### Scenario: Dados de cadastro inválidos
- **WHEN** o body não contém ticker válido ou contém atributos adicionais de cadastro manual
- **THEN** o sistema responde `400 Bad Request`, não consulta a fonte externa quando a falha é local e não persiste o ativo
