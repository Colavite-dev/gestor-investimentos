## MODIFIED Requirements

### Requirement: Cadastro básico de corretora
O sistema SHALL disponibilizar `POST /corretoras` para cadastrar uma corretora recebendo somente o CNPJ. O sistema SHALL validar e normalizar o CNPJ, verificar duplicidade, obter da fonte cadastral configurada razão social, nome fantasia, e-mail, telefone, CEP, logradouro, número, complemento, bairro, município, UF e situação cadastral, validar o CEP cadastral por fonte externa e enriquecer apenas lacunas de endereço permitidas, validar a instituição perante o cadastro oficial de Participantes Intermediários da CVM, gerar `id` e `dataCadastro` e responder `201 Created` com o recurso persistido e sua localização. O sistema MUST NOT persistir a corretora se a validação da CVM não a aceitar.

#### Scenario: Cadastro válido
- **WHEN** o cliente envia um CNPJ válido, ainda não cadastrado e encontrado pela fonte cadastral com dados válidos, CEP confirmado pela fonte de CEP e participante aceito pela CVM
- **THEN** o sistema persiste a corretora, responde `201 Created`, devolve os dados cadastrais normalizados e informa o URI `/corretoras/{id}` no header `Location`

#### Scenario: Cadastro sem campos opcionais
- **WHEN** a fonte cadastral não fornece nome fantasia, e-mail, telefone ou complemento, mas fornece todos os campos obrigatórios, o CEP é confirmado e o participante é aceito pela CVM
- **THEN** o sistema cadastra a corretora e representa os campos opcionais ausentes como `null`

### Requirement: Estado de validação externa explícito
Todo cadastro concluído nesta capability SHALL representar consulta cadastral de CNPJ bem-sucedida, CEP cadastral confirmado e validação positiva no cadastro oficial de Participantes Intermediários da CVM. O sistema SHALL persistir essa corretora com `validadaNaCvm=true`, e o contrato de criação MUST NOT permitir que o cliente defina esse valor. O sistema MUST NOT apresentar a consulta de CNPJ ou de CEP como autorização para atuação no mercado financeiro.

#### Scenario: Cadastro validado pela CVM
- **WHEN** uma corretora é cadastrada com dados obtidos pela fonte de CNPJ, CEP confirmado e participante aceito pela CVM
- **THEN** a resposta e as consultas posteriores apresentam os dados cadastrais externos e `validadaNaCvm=true`

#### Scenario: Cadastro básico ainda não validado externamente
- **WHEN** os dados de CNPJ e CEP foram obtidos, mas o participante não é aceito pela validação da CVM
- **THEN** o sistema não conclui o cadastro, responde o erro de validação aplicável e não persiste `validadaNaCvm=false`
