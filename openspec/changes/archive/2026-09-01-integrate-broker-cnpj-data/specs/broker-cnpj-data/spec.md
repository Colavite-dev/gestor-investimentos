## Purpose

Definir a consulta confiável de dados cadastrais por CNPJ através de uma abstração interna, usando a BrasilAPI como provider inicial sem expor seu contrato ao restante da aplicação.

## ADDED Requirements

### Requirement: Consulta cadastral externa por CNPJ
Após a validação local e a verificação de duplicidade, o sistema SHALL consultar a fonte cadastral configurada usando o CNPJ normalizado e SHALL usar o resultado para compor o cadastro da corretora.

#### Scenario: Consulta cadastral bem-sucedida
- **WHEN** um CNPJ localmente válido e ainda não cadastrado possui dados cadastrais disponíveis
- **THEN** o sistema consulta a fonte uma vez e devolve os dados em um modelo interno independente do formato externo

#### Scenario: CNPJ duplicado não consulta a fonte
- **WHEN** o CNPJ normalizado já pertence a uma corretora persistida
- **THEN** o sistema responde `409 Conflict` sem realizar consulta externa

### Requirement: Dados cadastrais oficiais
O sistema SHALL obter da fonte de CNPJ os campos CNPJ, razão social, nome fantasia, e-mail, primeiro telefone, CEP, logradouro, número, complemento, bairro, município, UF e descrição da situação cadastral. Nome fantasia, e-mail, telefone e complemento SHALL ser representados como `null` quando ausentes; os demais campos SHALL ser válidos e compatíveis com o modelo persistente.

#### Scenario: Resposta cadastral completa
- **WHEN** a fonte retorna os campos obrigatórios e opcionais para o CNPJ consultado
- **THEN** o sistema normaliza os valores e usa esses dados, em vez de valores fornecidos pelo cliente, no cadastro da corretora

#### Scenario: Campo opcional ausente
- **WHEN** nome fantasia, e-mail, telefone ou complemento está ausente ou vazio na resposta externa
- **THEN** o sistema representa o campo correspondente como `null` sem invalidar os demais dados

#### Scenario: Resposta externa inválida
- **WHEN** a fonte retorna corpo ilegível, CNPJ divergente, campo obrigatório ausente ou valor incompatível com o modelo persistente
- **THEN** o sistema não persiste a corretora e responde `502 Bad Gateway` com erro estável, sem incluir o corpo ou detalhes internos do provider

### Requirement: CNPJ não encontrado
O sistema SHALL distinguir um CNPJ localmente válido que não foi localizado pela fonte cadastral de falhas técnicas do serviço externo.

#### Scenario: Fonte não encontra o CNPJ
- **WHEN** a fonte cadastral informa que o CNPJ consultado não existe em sua base
- **THEN** o sistema não persiste a corretora e responde `422 Unprocessable Entity` sem repassar a mensagem interna da fonte

### Requirement: Falhas transitórias da fonte cadastral
O sistema SHALL tratar timeout, indisponibilidade, rate limit e erros técnicos externos sem persistir dados parciais ou expor detalhes do provider. Esses casos SHALL produzir uma resposta `503 Service Unavailable` estável e não SHALL realizar retry automático nesta etapa.

#### Scenario: Timeout na consulta
- **WHEN** a fonte não conclui a resposta dentro do timeout configurado
- **THEN** o sistema responde `503 Service Unavailable` e não persiste a corretora

#### Scenario: Rate limit externo
- **WHEN** a fonte rejeita a consulta por excesso de requisições
- **THEN** o sistema responde `503 Service Unavailable` e não expõe quota, headers ou mensagem interna da fonte

#### Scenario: Serviço externo indisponível
- **WHEN** ocorre falha de conexão ou resposta HTTP externa não classificada como CNPJ inexistente ou rate limit
- **THEN** o sistema responde `503 Service Unavailable` e não persiste a corretora
