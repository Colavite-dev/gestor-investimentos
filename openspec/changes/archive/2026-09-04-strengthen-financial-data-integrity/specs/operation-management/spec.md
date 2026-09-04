## ADDED Requirements

### Requirement: Validar capacidade decimal dos valores de operação

O sistema MUST aceitar quantidade e preço unitário somente quando forem positivos e couberem em `NUMERIC(19,8)`, com no máximo 11 algarismos inteiros e 8 casas decimais. A quantidade fracionária de até oito casas permanece suportada. Dados ausentes, não positivos ou fora dessa capacidade MUST resultar em `400 Bad Request` antes da persistência.

#### Scenario: Operação com quantidade fracionária e preço representáveis

- **WHEN** o cliente envia quantidade positiva com até oito casas decimais e preço unitário positivo com até oito casas decimais
- **THEN** o sistema aceita os valores e persiste a operação se as demais regras forem satisfeitas

#### Scenario: Preço ou quantidade com escala excessiva

- **WHEN** o cliente envia quantidade ou preço unitário com mais de oito casas decimais ou mais de onze algarismos inteiros
- **THEN** o sistema responde `400 Bad Request` e não persiste a operação
