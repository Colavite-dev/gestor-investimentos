## MODIFIED Requirements

### Requirement: Cadastro básico de corretora
O sistema SHALL disponibilizar `POST /corretoras` para cadastrar uma corretora recebendo somente o CNPJ. O sistema SHALL validar e normalizar o CNPJ, verificar duplicidade, obter da fonte cadastral configurada razão social, nome fantasia, e-mail, telefone, CEP, logradouro, número, complemento, bairro, município, UF e situação cadastral, validar o CEP cadastral por fonte externa e enriquecer apenas lacunas de endereço permitidas, gerar `id` e `dataCadastro` e responder `201 Created` com o recurso persistido e sua localização.

#### Scenario: Cadastro válido
- **WHEN** o cliente envia um CNPJ válido, ainda não cadastrado e encontrado pela fonte cadastral com dados válidos e CEP confirmado pela fonte de CEP
- **THEN** o sistema persiste a corretora, responde `201 Created`, devolve os dados cadastrais normalizados e informa o URI `/corretoras/{id}` no header `Location`

#### Scenario: Cadastro sem campos opcionais
- **WHEN** a fonte cadastral não fornece nome fantasia, e-mail, telefone ou complemento, mas fornece todos os campos obrigatórios e o CEP é confirmado
- **THEN** o sistema cadastra a corretora e representa os campos opcionais ausentes como `null`
