## Purpose

Registrar compras e vendas de ativos em carteiras, preservando os fatos transacionais necessários para futuras posições e cálculos de desempenho.

## ADDED Requirements

### Requirement: Registrar operação
O sistema MUST permitir criar uma operação informando carteira, ação, tipo `COMPRA` ou `VENDA`, quantidade positiva, preço unitário positivo e data da operação. Carteira e ação devem existir previamente.

#### Scenario: Compra válida
- **WHEN** o cliente envia `POST /operacoes` com referências existentes e dados válidos
- **THEN** o sistema persiste a operação e retorna `201 Created` com DTO e localização do recurso

#### Scenario: Venda válida
- **WHEN** o cliente envia operação do tipo `VENDA` com quantidade e preço positivos
- **THEN** o sistema persiste a venda como fato transacional, sem calcular posição neste momento

#### Scenario: Dados inválidos
- **WHEN** tipo é ausente/incompatível, quantidade ou preço não são positivos, ou data é ausente/inválida
- **THEN** o sistema retorna `400 Bad Request` sem persistir

### Requirement: Validar referências
O sistema MUST rejeitar operação que aponte para carteira ou ação inexistente e MUST preservar as referências originais, sem criar entidades relacionadas automaticamente.

#### Scenario: Carteira inexistente
- **WHEN** o cliente informa carteira não cadastrada
- **THEN** o sistema retorna `404 Not Found` sem persistir a operação

#### Scenario: Ação inexistente
- **WHEN** o cliente informa ação não cadastrada
- **THEN** o sistema retorna `404 Not Found` sem persistir a operação

### Requirement: Consultar operações
O sistema MUST permitir consultar uma operação por ID e listar operações de uma carteira em ordem crescente de data e ID, retornando DTOs.

#### Scenario: Consulta existente
- **WHEN** o cliente consulta `GET /operacoes/{id}` para operação existente
- **THEN** o sistema retorna `200 OK` com seus dados

#### Scenario: Listagem da carteira
- **WHEN** o cliente consulta `GET /carteiras/{carteiraId}/operacoes`
- **THEN** o sistema retorna `200 OK` com as operações da carteira ordenadas

#### Scenario: Operação inexistente
- **WHEN** o cliente consulta ID de operação não cadastrado
- **THEN** o sistema retorna `404 Not Found`

### Requirement: Integridade transacional
O sistema MUST persistir tipo, quantidade, preço unitário, data, carteira e ação com chaves estrangeiras e não deve apagar ou alterar operações nesta capacidade.

#### Scenario: Referências preservadas
- **WHEN** uma operação válida é persistida
- **THEN** carteira, ação e valores informados permanecem associados ao registro retornado

### Requirement: Adiar cálculos
Esta capacidade MUST registrar fatos de compra e venda, mas não deve calcular posição, preço médio, patrimônio, rentabilidade ou impedir venda por saldo insuficiente.

#### Scenario: Venda sem cálculo
- **WHEN** uma venda é registrada
- **THEN** o sistema apenas armazena a operação e não cria saldo ou cálculo derivado
