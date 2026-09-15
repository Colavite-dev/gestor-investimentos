## MODIFIED Requirements

### Requirement: Cadastro básico de corretora
O sistema SHALL disponibilizar `POST /corretoras` para cadastrar uma corretora recebendo somente o CNPJ. Para uma solicitação autenticada, o sistema SHALL associar a corretora exclusivamente ao ID estável da identidade autenticada, sem aceitar `usuarioId`, owner ou username como fonte de autorização. O sistema SHALL validar e normalizar o CNPJ, verificar duplicidade dentro do proprietário autenticado, obter da fonte cadastral configurada razão social, nome fantasia, e-mail, telefone, CEP, logradouro, número, complemento, bairro, município, UF e situação cadastral, validar o CEP cadastral por fonte externa e enriquecer apenas lacunas de endereço permitidas, validar a instituição perante o cadastro oficial de Participantes Intermediários da CVM, gerar `id` e `dataCadastro` e responder `201 Created` com o recurso persistido e sua localização. O sistema MUST NOT persistir a corretora se a validação da CVM não a aceitar.

#### Scenario: Cadastro válido
- **WHEN** o usuário autenticado envia um CNPJ válido, ainda não cadastrado em sua conta e encontrado pela fonte cadastral com dados válidos, CEP confirmado pela fonte de CEP e participante aceito pela CVM
- **THEN** o sistema persiste uma corretora pertencente a esse usuário, responde `201 Created`, devolve os dados cadastrais normalizados e informa o URI `/corretoras/{id}` no header `Location`

#### Scenario: Cadastro sem campos opcionais
- **WHEN** a fonte cadastral não fornece nome fantasia, e-mail, telefone ou complemento, mas fornece todos os campos obrigatórios, o CEP é confirmado e o participante é aceito pela CVM
- **THEN** o sistema cadastra a corretora para o usuário autenticado e representa os campos opcionais ausentes como `null`

#### Scenario: Cliente tenta fornecer identidade de proprietário
- **WHEN** o cliente inclui `usuarioId`, owner ou username no corpo de `POST /corretoras`
- **THEN** o sistema rejeita o corpo pelo contrato estrito de entrada e nunca atribui a corretora a outra identidade

### Requirement: CNPJ localmente válido e único
O sistema SHALL aceitar CNPJ com 14 dígitos ou com a máscara brasileira usual, SHALL validar seus dígitos verificadores e SHALL armazená-lo somente com dígitos. O sistema MUST NOT persistir mais de uma corretora com o mesmo CNPJ normalizado para o mesmo usuário autenticado, mas SHALL permitir o mesmo CNPJ normalizado para usuários diferentes. A verificação antecipada de duplicidade SHALL evitar consultas externas quando o cadastro já existir para aquele usuário; a garantia final SHALL também abranger solicitações concorrentes que ultrapassem essa verificação antes da persistência.

#### Scenario: CNPJ inválido
- **WHEN** o cliente informa um CNPJ com formato, quantidade de dígitos ou dígitos verificadores inválidos
- **THEN** o sistema responde `400 Bad Request` e não persiste a corretora

#### Scenario: CNPJ duplicado
- **WHEN** já existe uma corretora do usuário autenticado com o mesmo CNPJ normalizado
- **THEN** o sistema responde `409 Conflict` e preserva apenas o cadastro existente daquele usuário

#### Scenario: Mesmo CNPJ em contas diferentes
- **WHEN** USER_A e USER_B cadastram o mesmo CNPJ normalizado com solicitações válidas
- **THEN** o sistema persiste uma corretora independente para cada usuário e ambas as solicitações respondem `201 Created`

#### Scenario: Colisão concorrente de CNPJ
- **WHEN** duas solicitações válidas do mesmo usuário para o mesmo CNPJ normalizado passam pela verificação inicial antes que qualquer uma seja persistida
- **THEN** o sistema persiste exatamente uma corretora para esse usuário e responde `409 Conflict` para a solicitação que perder a colisão de unicidade

#### Scenario: Colisão concorrente de CNPJ entre usuários diferentes
- **WHEN** USER_A e USER_B enviam simultaneamente solicitações válidas para o mesmo CNPJ normalizado
- **THEN** o sistema persiste uma corretora para cada usuário e ambas as solicitações respondem `201 Created`

### Requirement: Listagem de corretoras
O sistema SHALL disponibilizar `GET /corretoras` e responder uma coleção JSON somente das corretoras pertencentes ao usuário autenticado, em ordem crescente de `id`.

#### Scenario: Existem corretoras cadastradas
- **WHEN** o usuário autenticado solicita a listagem e possui corretoras persistidas
- **THEN** o sistema responde `200 OK` com somente as corretoras desse usuário em ordem crescente de `id`

#### Scenario: Não existem corretoras cadastradas
- **WHEN** o usuário autenticado solicita a listagem e existem corretoras somente em outras contas
- **THEN** o sistema responde `200 OK` com uma coleção vazia

### Requirement: Consulta de corretora por ID
O sistema SHALL disponibilizar `GET /corretoras/{id}` para consultar uma corretora pelo identificador persistente somente quando ela pertencer ao usuário autenticado. Um ID inexistente e um ID pertencente a outro usuário SHALL produzir a mesma resposta `404 Not Found`, sem confirmar a existência ou o proprietário do recurso.

#### Scenario: ID existente
- **WHEN** o usuário autenticado consulta o ID de uma corretora que lhe pertence
- **THEN** o sistema responde `200 OK` com a corretora correspondente

#### Scenario: ID inexistente
- **WHEN** o usuário autenticado consulta um ID positivo inexistente ou pertencente a outro usuário
- **THEN** o sistema responde `404 Not Found` sem dados da corretora e sem informar qual das duas condições ocorreu

#### Scenario: ID inválido
- **WHEN** o cliente informa um ID menor que 1
- **THEN** o sistema responde `400 Bad Request`

### Requirement: Consulta de corretora por CNPJ
O sistema SHALL disponibilizar `GET /corretoras/cnpj/{cnpj}` e SHALL exigir no path o CNPJ com 14 dígitos, aplicando os mesmos dígitos verificadores usados no cadastro. A consulta SHALL ser limitada ao usuário autenticado. A máscara brasileira continuará aceita no corpo do cadastro, mas não no path porque contém o separador `/`.

#### Scenario: CNPJ existente
- **WHEN** o usuário autenticado consulta um CNPJ válido cadastrado em sua conta usando 14 dígitos
- **THEN** o sistema responde `200 OK` com a corretora correspondente

#### Scenario: CNPJ válido inexistente
- **WHEN** o usuário autenticado consulta um CNPJ localmente válido que não está cadastrado em sua conta, inclusive quando o CNPJ existe somente em outra conta
- **THEN** o sistema responde `404 Not Found` sem dados da corretora e sem informar qual das duas condições ocorreu

#### Scenario: CNPJ de consulta inválido
- **WHEN** o cliente consulta um CNPJ localmente inválido
- **THEN** o sistema responde `400 Bad Request`
