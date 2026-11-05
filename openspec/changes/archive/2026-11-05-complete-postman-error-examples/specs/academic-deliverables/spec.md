## MODIFIED Requirements

### Requirement: Coleção executável da API

O repositório SHALL conter uma coleção Postman ou Insomnia importável, sem credenciais reais, que documente os endpoints de corretoras e ações atualmente disponíveis. A coleção MUST incluir exemplos de cadastro, consultas, atualização de cotação, validação de entrada, recurso inexistente e duplicidade, além de indicar variáveis de ambiente necessárias sem valores secretos. Cada exemplo de erro MUST ser executável contra o contrato atual da API e MUST verificar o status HTTP esperado. Quando um exemplo depender de cadastro anterior ou de um recurso criado, a coleção MUST indicar essa pré-condição.

#### Scenario: Importação sem segredo

- **WHEN** uma pessoa importa a coleção em uma ferramenta compatível
- **THEN** consegue visualizar e executar os requests documentados após configurar localmente a URL base, sem que qualquer token, senha ou chave real esteja no arquivo

#### Scenario: Cobertura dos endpoints atuais

- **WHEN** a pessoa percorre a coleção
- **THEN** encontra requests para os endpoints de corretoras e ações, incluindo `PUT /acoes/{id}/atualizar-cotacao`, com exemplos coerentes com os contratos atuais

#### Scenario: Validação de entrada inválida

- **WHEN** a pessoa executa um request com entrada inválida documentada
- **THEN** a coleção verifica que a API responde `400 Bad Request`

#### Scenario: Recurso inexistente

- **WHEN** a pessoa executa um request de consulta para um identificador positivo inexistente
- **THEN** a coleção verifica que a API responde `404 Not Found`

#### Scenario: Recurso duplicado

- **WHEN** a pessoa executa novamente um cadastro usando o mesmo identificador lógico de um recurso criado anteriormente para o mesmo usuário
- **THEN** a coleção documenta essa pré-condição e verifica que a API responde `409 Conflict`
