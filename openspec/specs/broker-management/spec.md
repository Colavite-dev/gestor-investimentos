# Broker Management Specification

## Purpose

Definir o gerenciamento básico e persistente de corretoras, com contratos REST próprios, validações locais, unicidade de CNPJ e indicação explícita de que a validação externa ainda não ocorreu.

## Requirements

### Requirement: Cadastro básico de corretora
O sistema SHALL disponibilizar `POST /corretoras` para cadastrar uma corretora recebendo somente o CNPJ. O sistema SHALL validar e normalizar o CNPJ, verificar duplicidade, obter da fonte cadastral configurada razão social, nome fantasia, e-mail, telefone, CEP, logradouro, número, complemento, bairro, município, UF e situação cadastral, gerar `id` e `dataCadastro` e responder `201 Created` com o recurso persistido e sua localização.

#### Scenario: Cadastro válido
- **WHEN** o cliente envia um CNPJ válido, ainda não cadastrado e encontrado pela fonte cadastral com dados válidos
- **THEN** o sistema persiste a corretora, responde `201 Created`, devolve os dados cadastrais normalizados e informa o URI `/corretoras/{id}` no header `Location`

#### Scenario: Cadastro sem campos opcionais
- **WHEN** a fonte cadastral não fornece nome fantasia, e-mail, telefone ou complemento, mas fornece todos os campos obrigatórios
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
O sistema SHALL exigir no cadastro somente um CNPJ não vazio, no formato numérico brasileiro já suportado, e SHALL rejeitar formato, quantidade de dígitos, dígitos verificadores ou propriedades adicionais desconhecidas no corpo. Os dados cadastrais retornados pela fonte externa SHALL ser validados separadamente antes da persistência.

#### Scenario: Requisição com campos inválidos
- **WHEN** o cliente omite o CNPJ ou envia formato ou dígitos verificadores inválidos
- **THEN** o sistema responde `400 Bad Request` com a identificação do campo inválido, sem consultar a fonte e sem persistir a corretora

#### Scenario: Requisição tenta fornecer dados cadastrais
- **WHEN** o cliente inclui no cadastro campos além do CNPJ
- **THEN** o sistema responde `400 Bad Request` e não permite sobrescrever dados cuja origem é cadastral

### Requirement: Estado de validação externa explícito
Todo cadastro concluído nesta capability SHALL representar uma consulta cadastral de CNPJ bem-sucedida, mas SHALL continuar persistido com `validadaNaCvm=false`; o contrato de criação MUST NOT permitir que o cliente defina esse valor. O sistema MUST NOT apresentar a consulta de CNPJ como validação de CEP por provider próprio ou como autorização para atuação no mercado financeiro.

#### Scenario: Cadastro básico ainda não validado externamente
- **WHEN** uma corretora é cadastrada com dados obtidos pela fonte de CNPJ
- **THEN** a resposta e as consultas posteriores apresentam os dados cadastrais externos e `validadaNaCvm=false`

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
