## ADDED Requirements

### Requirement: Aceitar somente cotações externas representáveis

Antes de persistir uma cotação recebida de provider durante cadastro ou atualização, o sistema MUST aceitar somente valor positivo que caiba em `NUMERIC(19,8)`, isto é, no máximo 11 algarismos inteiros e 8 casas decimais. Uma resposta externa fora desse limite MUST ser tratada como dado externo inválido com `502 Bad Gateway`, sem criar ou alterar a ação.

#### Scenario: Cotação externa com oito casas decimais

- **WHEN** o provider retorna uma cotação positiva com até oito casas decimais e até onze algarismos inteiros
- **THEN** o sistema aceita a resposta e preserva o valor na ação cadastrada ou atualizada

#### Scenario: Cotação externa fora da precisão contratada

- **WHEN** o provider retorna cotação positiva com mais de oito casas decimais ou mais de onze algarismos inteiros
- **THEN** o sistema responde `502 Bad Gateway` e não persiste nem altera a ação
