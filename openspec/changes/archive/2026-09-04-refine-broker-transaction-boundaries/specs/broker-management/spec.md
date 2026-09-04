## MODIFIED Requirements

### Requirement: CNPJ localmente válido e único
O sistema SHALL aceitar CNPJ com 14 dígitos ou com a máscara brasileira usual, SHALL validar seus dígitos verificadores e SHALL armazená-lo somente com dígitos. O sistema MUST NOT persistir mais de uma corretora com o mesmo CNPJ normalizado. A verificação antecipada de duplicidade SHALL evitar consultas externas quando o cadastro já existir; a garantia final SHALL também abranger solicitações concorrentes que ultrapassem essa verificação antes da persistência.

#### Scenario: CNPJ inválido
- **WHEN** o cliente informa um CNPJ com formato, quantidade de dígitos ou dígitos verificadores inválidos
- **THEN** o sistema responde `400 Bad Request` e não persiste a corretora

#### Scenario: CNPJ duplicado
- **WHEN** já existe uma corretora com o mesmo CNPJ normalizado
- **THEN** o sistema responde `409 Conflict` e preserva apenas o cadastro existente

#### Scenario: Colisão concorrente de CNPJ
- **WHEN** duas solicitações válidas para o mesmo CNPJ normalizado passam pela verificação inicial antes que qualquer uma seja persistida
- **THEN** o sistema persiste exatamente uma corretora e responde `409 Conflict` para a solicitação que perder a colisão de unicidade
