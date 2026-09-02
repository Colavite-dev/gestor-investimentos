## MODIFIED Requirements

### Requirement: Cadastro básico de corretora
O sistema SHALL disponibilizar `POST /corretoras` para cadastrar uma corretora recebendo somente o CNPJ. O sistema SHALL validar e normalizar o CNPJ, verificar duplicidade, obter da fonte cadastral configurada razão social, nome fantasia, e-mail, telefone, CEP, logradouro, número, complemento, bairro, município, UF e situação cadastral, gerar `id` e `dataCadastro` e responder `201 Created` com o recurso persistido e sua localização.

#### Scenario: Cadastro válido
- **WHEN** o cliente envia um CNPJ válido, ainda não cadastrado e encontrado pela fonte cadastral com dados válidos
- **THEN** o sistema persiste a corretora, responde `201 Created`, devolve os dados cadastrais normalizados e informa o URI `/corretoras/{id}` no header `Location`

#### Scenario: Cadastro sem campos opcionais
- **WHEN** a fonte cadastral não fornece nome fantasia, e-mail, telefone ou complemento, mas fornece todos os campos obrigatórios
- **THEN** o sistema cadastra a corretora e representa os campos opcionais ausentes como `null`

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
