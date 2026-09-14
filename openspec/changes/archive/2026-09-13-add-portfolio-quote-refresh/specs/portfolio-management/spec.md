## MODIFIED Requirements

### Requirement: Manter escopo de carteira inicial

Esta capacidade MUST limitar seus próprios endpoints ao cadastro e consulta básica, mas a carteira MAY ser referenciada por operações registradas pela capacidade `operation-management`, MAY expor consultas derivadas pela capacidade `portfolio-position-calculation` e MUST expor uma ação explícita de atualização de cotações das posições abertas. A carteira não deve executar cálculos dentro de sua entidade nem alterar dados cadastrais durante essas consultas ou durante o refresh.

#### Scenario: Cadastro sem operações

- **WHEN** uma carteira é criada
- **THEN** ela não cria operações, posições ou relacionamentos obrigatórios com ações

#### Scenario: Carteira referenciada por operação

- **WHEN** uma operação válida é cadastrada para uma carteira existente
- **THEN** a operação mantém referência à carteira sem alterar os dados cadastrais dela

#### Scenario: Consulta derivada

- **WHEN** o cliente consulta posições ou resumo de uma carteira existente
- **THEN** o sistema calcula a resposta a partir das operações e ações persistidas sem alterar a carteira

#### Scenario: Consulta derivada não atualiza cotações

- **WHEN** o cliente consulta posições ou resumo de uma carteira existente
- **THEN** o sistema não chama provider, atualiza cotações nem cria histórico

## ADDED Requirements

### Requirement: Atualizar cotações abertas explicitamente por carteira

O sistema MUST disponibilizar `PUT /carteiras/{id}/atualizar-cotacoes`, sem corpo, somente para o usuário autenticado proprietário da carteira. Antes de qualquer consulta externa, o sistema MUST validar ownership; carteira inexistente ou pertencente a outro usuário MUST retornar `404 Not Found`. A ação MUST considerar somente ativos com posição líquida aberta e quantidade positiva, atualizar cada ativo elegível no máximo uma vez e retornar `200 OK` com `quantidadeAtualizada`, `quantidadeComFalha` e `tickersComFalha`.

O refresh MUST ser acionado somente por esse comando explícito; `GET /carteiras/{id}`, `GET /carteiras/{id}/posicoes` e `GET /carteiras/{id}/resumo` MUST NOT atualizar cotação, chamar provider ou criar histórico. A ação MUST NOT alterar carteira, operações, quantidade, custo, preço médio ou resultado realizado.

#### Scenario: Owner atualiza posições abertas

- **WHEN** o owner chama o endpoint para uma carteira com posições abertas BRL e USD
- **THEN** o sistema processa os ativos elegíveis uma vez cada e retorna os contadores do resultado sem converter moedas

#### Scenario: Posição encerrada é ignorada

- **WHEN** as operações de um ativo resultam em quantidade líquida zero
- **THEN** o ativo não é atualizado nem incluído nos contadores

#### Scenario: Carteira sem posição aberta

- **WHEN** o owner chama o endpoint para uma carteira sem posições líquidas positivas
- **THEN** o sistema retorna `200 OK` com `quantidadeAtualizada` e `quantidadeComFalha` iguais a zero e não consulta provider

#### Scenario: Carteira de outro usuário

- **WHEN** um usuário chama o endpoint para uma carteira pertencente a outro usuário
- **THEN** o sistema retorna `404 Not Found` antes de consultar provider ou revelar dados

#### Scenario: Falha parcial

- **WHEN** a atualização de um ativo falha e outra atualização do mesmo lote é bem-sucedida
- **THEN** o sistema preserva o sucesso, continua o processamento, retorna `200 OK` com os dois contadores e inclui somente os tickers que falharam
