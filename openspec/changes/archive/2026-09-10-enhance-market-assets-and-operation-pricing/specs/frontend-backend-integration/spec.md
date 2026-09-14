## ADDED Requirements

### Requirement: Cliente integra catálogo e cotação sem quebrar fluxos existentes
O cliente HTTP autenticado SHALL consumir `GET /acoes/catalogo` com parâmetros codificados e SHALL continuar usando pesquisa, resolução e atualização de cotação existentes. Ao selecionar um ativo já persistido, SHALL solicitar sua atualização de cotação; ao resolver um novo ativo, SHALL reutilizar a cotação devolvida pela resolução, evitando uma segunda chamada imediata.

#### Scenario: Seleção de ativo persistido
- **WHEN** o usuário seleciona no combobox uma identidade já presente na lista local
- **THEN** o frontend mantém a seleção, solicita atualização e usa a resposta como sugestão de preço

#### Scenario: Seleção de ativo novo
- **WHEN** o usuário seleciona uma identidade ainda não persistida
- **THEN** o frontend resolve o ativo uma vez e usa a cotação da resposta como sugestão

#### Scenario: Resposta obsoleta de seleção
- **WHEN** uma resposta de cotação chega depois que o usuário trocou de ativo ou editou manualmente o preço
- **THEN** ela não sobrescreve a seleção ou o preço atuais
