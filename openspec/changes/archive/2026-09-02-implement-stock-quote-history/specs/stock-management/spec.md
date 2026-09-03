## ADDED Requirements

### Requirement: Registrar histórico nas operações de cotação

Após um cadastro ou atualização de ação concluído com dados externos válidos, o sistema SHALL registrar uma observação histórica contendo a cotação e o timestamp aceitos. A observação MUST ser persistida na mesma transação da ação correspondente; falhas de validação, duplicidade ou indisponibilidade MUST NOT criar observação.

#### Scenario: Cadastro registra observação inicial

- **WHEN** o cliente cadastra uma ação válida
- **THEN** o sistema persiste o ativo e sua primeira observação histórica correspondente

#### Scenario: Atualização registra observação

- **WHEN** o cliente atualiza a cotação de uma ação existente com sucesso
- **THEN** o sistema altera apenas os campos de cotação da ação e acrescenta uma observação histórica, preservando as anteriores

#### Scenario: Erro não registra observação

- **WHEN** a resolução ou atualização falha por resposta externa inválida, ticker não consultável ou indisponibilidade
- **THEN** o sistema não altera a ação nem persiste uma nova observação
