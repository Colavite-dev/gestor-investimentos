## Purpose

Definir o gerenciamento básico e persistente de corretoras, com contratos REST próprios, validações locais, unicidade de CNPJ e indicação explícita de que a validação externa ainda não ocorreu.

## ADDED Requirements

### Requirement: Cadastro básico de corretora
O sistema SHALL disponibilizar `POST /corretoras` para cadastrar uma corretora com CNPJ, razão social, nome fantasia opcional, e-mail opcional, telefone opcional, CEP, logradouro, número, complemento opcional, bairro, cidade, UF e situação cadastral. O sistema SHALL gerar `id` e `dataCadastro`, normalizar CNPJ e CEP para dígitos, normalizar UF para letras maiúsculas e responder `201 Created` com o recurso persistido e sua localização.

#### Scenario: Cadastro válido
- **WHEN** o cliente envia todos os campos obrigatórios válidos e o CNPJ não está cadastrado
- **THEN** o sistema persiste a corretora, responde `201 Created`, devolve os dados normalizados e informa o URI `/corretoras/{id}` no header `Location`

#### Scenario: Cadastro sem campos opcionais
- **WHEN** o cliente omite nome fantasia, e-mail, telefone e complemento, mas envia os campos obrigatórios válidos
- **THEN** o sistema cadastra a corretora e representa os campos opcionais ausentes como `null`

### Requirement: CNPJ localmente válido e único
O sistema SHALL aceitar CNPJ com 14 dígitos ou com a máscara brasileira usual, SHALL validar seus dígitos verificadores e SHALL armazená-lo somente com dígitos. O sistema MUST NOT persistir mais de uma corretora com o mesmo CNPJ normalizado.

#### Scenario: CNPJ inválido
- **WHEN** o cliente informa um CNPJ com formato, quantidade de dígitos ou dígitos verificadores inválidos
- **THEN** o sistema responde `400 Bad Request` e não persiste a corretora

#### Scenario: CNPJ duplicado
- **WHEN** já existe uma corretora com o mesmo CNPJ normalizado
- **THEN** o sistema responde `409 Conflict` e preserva apenas o cadastro existente

### Requirement: Validação dos dados de entrada
O sistema SHALL rejeitar campos obrigatórios ausentes ou em branco, e-mail malformado, CEP diferente de oito dígitos após normalização, UF diferente de duas letras e valores que excedam os limites documentados do modelo persistente.

#### Scenario: Requisição com campos inválidos
- **WHEN** o cliente envia um ou mais campos que violam as restrições de entrada
- **THEN** o sistema responde `400 Bad Request` com a identificação dos campos inválidos e não persiste a corretora

### Requirement: Estado de validação externa explícito
Enquanto as integrações externas não estiverem implementadas, todo cadastro básico SHALL ser persistido com `validadaNaCvm=false`, e o contrato de criação MUST NOT permitir que o cliente defina esse valor. O sistema MUST NOT apresentar esse registro como validação concluída de CNPJ, CEP ou autorização no mercado financeiro.

#### Scenario: Cadastro básico ainda não validado externamente
- **WHEN** uma corretora é cadastrada nesta capability
- **THEN** a resposta e as consultas posteriores apresentam `validadaNaCvm=false`

### Requirement: Listagem de corretoras
O sistema SHALL disponibilizar `GET /corretoras` e responder uma coleção JSON de todas as corretoras cadastradas em ordem crescente de `id`.

#### Scenario: Existem corretoras cadastradas
- **WHEN** o cliente solicita a listagem e existem corretoras persistidas
- **THEN** o sistema responde `200 OK` com todas as corretoras em ordem crescente de `id`

#### Scenario: Não existem corretoras cadastradas
- **WHEN** o cliente solicita a listagem e não existe corretora persistida
- **THEN** o sistema responde `200 OK` com uma coleção vazia

### Requirement: Consulta de corretora por ID
O sistema SHALL disponibilizar `GET /corretoras/{id}` para consultar uma corretora pelo identificador persistente.

#### Scenario: ID existente
- **WHEN** o cliente consulta um ID existente
- **THEN** o sistema responde `200 OK` com a corretora correspondente

#### Scenario: ID inexistente
- **WHEN** o cliente consulta um ID positivo inexistente
- **THEN** o sistema responde `404 Not Found`

#### Scenario: ID inválido
- **WHEN** o cliente informa um ID menor que 1
- **THEN** o sistema responde `400 Bad Request`

### Requirement: Consulta de corretora por CNPJ
O sistema SHALL disponibilizar `GET /corretoras/cnpj/{cnpj}` e SHALL exigir no path o CNPJ com 14 dígitos, aplicando os mesmos dígitos verificadores usados no cadastro. A máscara brasileira continuará aceita no corpo do cadastro, mas não no path porque contém o separador `/`.

#### Scenario: CNPJ existente
- **WHEN** o cliente consulta um CNPJ válido cadastrado usando 14 dígitos
- **THEN** o sistema responde `200 OK` com a corretora correspondente

#### Scenario: CNPJ válido inexistente
- **WHEN** o cliente consulta um CNPJ localmente válido que não está cadastrado
- **THEN** o sistema responde `404 Not Found`

#### Scenario: CNPJ de consulta inválido
- **WHEN** o cliente consulta um CNPJ localmente inválido
- **THEN** o sistema responde `400 Bad Request`

### Requirement: Erros HTTP centralizados
O sistema SHALL representar erros de validação, duplicidade e recurso inexistente por um contrato JSON centralizado contendo timestamp, status HTTP, título, mensagem, caminho da requisição e, quando aplicável, erros por campo.

#### Scenario: Erro tratado
- **WHEN** uma requisição produz erro de validação, duplicidade ou recurso inexistente
- **THEN** o sistema responde com o status especificado e um corpo compatível com o contrato centralizado, sem expor stack trace ou detalhes internos
