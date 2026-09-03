# Delta Specification: operation-management

## MODIFIED Requirements

### Requirement: Registrar operação

O sistema MUST permitir criar uma operação informando carteira, ação, tipo `COMPRA` ou `VENDA`, quantidade positiva, preço unitário positivo e data da operação. Carteira e ação devem existir previamente. Para cada combinação de carteira e ação, antes de persistir uma `VENDA`, o sistema MUST avaliar a sequência completa das operações existentes mais a candidata em ordem `dataOperacao ASC, id ASC`, posicionando a candidata após operações já persistidas com o mesmo timestamp. Em todos os pontos, `compras - vendas` MUST ser maior ou igual a zero. A venda que violar essa regra MUST ser rejeitada integralmente.

#### Scenario: Compra válida

- **WHEN** o cliente envia `POST /operacoes` com referências existentes e dados válidos para uma compra
- **THEN** o sistema persiste a operação e retorna `201 Created` com DTO e localização do recurso

#### Scenario: Venda com saldo parcial disponível

- **WHEN** a carteira possui compra de 10 unidades e recebe venda de 5 unidades do mesmo ativo
- **THEN** o sistema persiste a venda e o saldo resultante é 5

#### Scenario: Venda que zera saldo

- **WHEN** a carteira possui compra de 10 unidades e recebe venda de 10 unidades do mesmo ativo
- **THEN** o sistema persiste a venda e o saldo resultante é 0

#### Scenario: Venda válida

- **WHEN** o cliente envia operação do tipo `VENDA` com quantidade e preço positivos e a sequência completa preserva saldo não negativo
- **THEN** o sistema persiste a venda como fato transacional

#### Scenario: Venda superior ao saldo

- **WHEN** a carteira possui compra de 10 unidades e recebe venda de 11 unidades do mesmo ativo
- **THEN** o sistema retorna `422 Unprocessable Entity` e não persiste a venda

#### Scenario: Venda sem compra anterior

- **WHEN** a carteira não possui quantidade disponível para o ativo e recebe uma venda
- **THEN** o sistema retorna `422 Unprocessable Entity` e não persiste a venda

#### Scenario: Venda retroativa que invalida operação posterior

- **WHEN** existem compra de 10 em 01/09 e venda de 8 em 03/09, e é solicitada venda de 5 em 02/09
- **THEN** o sistema rejeita a nova venda com `422 Unprocessable Entity` porque a sequência passaria a ter saldo negativo em 03/09, sem persistir qualquer parte da solicitação

#### Scenario: Operação retroativa válida

- **WHEN** uma operação com data anterior é inserida e toda a sequência resultante mantém saldo não negativo
- **THEN** o sistema a persiste e as operações permanecem ordenáveis de forma determinística

#### Scenario: Operações com mesmo timestamp

- **WHEN** a operação candidata possui o mesmo timestamp de operações já persistidas para a carteira e ação
- **THEN** as operações existentes são consideradas por ID crescente antes da candidata, que recebe novo ID após a persistência

#### Scenario: Rejeição não causa persistência parcial

- **WHEN** uma venda é rejeitada por saldo insuficiente
- **THEN** nenhuma operação nova é gravada e os registros existentes permanecem inalterados

#### Scenario: Dados inválidos

- **WHEN** tipo é ausente ou incompatível, quantidade ou preço não são positivos, ou data é ausente ou inválida
- **THEN** o sistema retorna `400 Bad Request` sem persistir

## REMOVED Requirements

### Requirement: Adiar cálculos

**Reason**: A integridade histórica de saldo é regra obrigatória de registro de venda, e não uma projeção opcional de posição.

**Migration**: Clientes que antes enviavam vendas sem saldo suficiente devem registrar compras que cubram a venda ou corrigir a sequência antes de reenviar a operação.
