# operation-management Specification

## Purpose

Registrar compras e vendas de ativos em carteiras, preservando os fatos transacionais necessários para futuras posições e cálculos de desempenho.

## Requirements

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

- **WHEN** o cliente consulta ID de operação não cadastrada
- **THEN** o sistema retorna `404 Not Found`

### Requirement: Integridade transacional

O sistema MUST persistir tipo, quantidade, preço unitário, data, carteira e ação com chaves estrangeiras e não deve apagar ou alterar operações nesta capacidade.

#### Scenario: Referências preservadas

- **WHEN** uma operação válida é persistida
- **THEN** carteira, ação e valores informados permanecem associados ao registro retornado

### Requirement: Validar capacidade decimal dos valores de operação

O sistema MUST aceitar quantidade e preço unitário somente quando forem positivos e couberem em `NUMERIC(19,8)`, com no máximo 11 algarismos inteiros e 8 casas decimais. A quantidade fracionária de até oito casas permanece suportada. Dados ausentes, não positivos ou fora dessa capacidade MUST resultar em `400 Bad Request` antes da persistência.

#### Scenario: Operação com quantidade fracionária e preço representáveis

- **WHEN** o cliente envia quantidade positiva com até oito casas decimais e preço unitário positivo com até oito casas decimais
- **THEN** o sistema aceita os valores e persiste a operação se as demais regras forem satisfeitas

#### Scenario: Preço ou quantidade com escala excessiva

- **WHEN** o cliente envia quantidade ou preço unitário com mais de oito casas decimais ou mais de onze algarismos inteiros
- **THEN** o sistema responde `400 Bad Request` e não persiste a operação
### Requirement: Cotação atual é apenas sugestão de preço da operação

Ao selecionar uma ação persistida ou resolvida no formulário, o sistema SHALL tentar obter sua cotação atual e apresentá-la separadamente como referência. O preço unitário SHALL ser preenchido com essa cotação somente como sugestão e SHALL permanecer editável; o valor enviado no `POST /operacoes` SHALL ser o preço efetivamente persistido.

#### Scenario: Usuário aceita a sugestão
- **WHEN** a cotação é obtida e o usuário envia o formulário sem alterar o preço sugerido
- **THEN** a operação é persistida com esse preço unitário

#### Scenario: Usuário substitui a sugestão
- **WHEN** a cotação é 32,47 e o usuário altera o preço unitário para 30,00
- **THEN** a operação é persistida com preço unitário 30,00

#### Scenario: Usuário edita enquanto a cotação carrega
- **WHEN** o usuário informa manualmente um preço antes da resposta assíncrona
- **THEN** a resposta tardia não sobrescreve o preço manual

### Requirement: Falha de cotação não bloqueia preço manual

Quando uma ação já puder ser identificada localmente e a cotação estiver indisponível, o formulário SHALL manter a ação selecionada, informar a falha e permitir o registro com um preço manual positivo. A falha MUST NOT substituir a cotação persistida por zero ou nulo.

#### Scenario: Provider falha para ação local
- **WHEN** a atualização de cotação falha após a seleção de uma ação persistida
- **THEN** o usuário pode preencher um preço positivo e registrar a operação normalmente

#### Scenario: Novo ativo não pode ser resolvido
- **WHEN** um ativo ainda não persistido não pode ser validado pelo provider
- **THEN** o sistema não inventa nem persiste sua identidade e informa que o ativo não pôde ser selecionado

### Requirement: Operações respeitam o owner da carteira

O sistema SHALL criar e consultar operações somente quando a carteira associada pertence ao usuário autenticado. A validação de saldo e a ordenação SHALL considerar exclusivamente as operações daquela carteira autorizada, preservando todas as regras financeiras existentes.

#### Scenario: Compra na própria carteira

- **WHEN** USER_A registra uma compra válida em sua própria carteira
- **THEN** a operação é persistida e pode ser consultada por USER_A

#### Scenario: Compra em carteira alheia

- **WHEN** USER_B tenta registrar uma compra ou venda na carteira de USER_A
- **THEN** a API responde `404 Not Found`, não executa a regra financeira sobre dados de USER_A e não persiste a operação

#### Scenario: Consulta isolada de operações

- **WHEN** USER_B consulta por ID uma operação ou lista operações da carteira de USER_A
- **THEN** a API responde `404 Not Found` e não retorna fatos transacionais de USER_A
