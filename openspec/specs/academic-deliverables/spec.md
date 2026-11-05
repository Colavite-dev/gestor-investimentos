# academic-deliverables Specification

## Purpose

Disponibilizar artefatos reproduzíveis para demonstrar, testar e entregar academicamente a API REST e o modelo persistente do sistema de investimentos.

## Requirements

### Requirement: Coleção executável da API

O repositório SHALL conter uma coleção Postman ou Insomnia importável, sem credenciais reais, que documente os endpoints de corretoras e ações atualmente disponíveis. A coleção MUST incluir exemplos de cadastro, consultas, atualização de cotação, validação de entrada, recurso inexistente e duplicidade, além de indicar variáveis de ambiente necessárias sem valores secretos. Cada exemplo de erro MUST ser executável contra o contrato atual da API e MUST verificar o status HTTP esperado. Quando um exemplo depender de cadastro anterior ou de um recurso criado, a coleção MUST indicar essa pré-condição.

#### Scenario: Importação sem segredo

- **WHEN** uma pessoa importa a coleção em uma ferramenta compatível
- **THEN** consegue visualizar e executar os requests documentados após configurar localmente a URL base, sem que qualquer token, senha ou chave real esteja no arquivo

#### Scenario: Cobertura dos endpoints atuais

- **WHEN** a pessoa percorre a coleção
- **THEN** encontra requests para os endpoints de corretoras e ações, incluindo `PUT /acoes/{id}/atualizar-cotacao`, com exemplos coerentes com os contratos atuais

#### Scenario: Validação de entrada inválida

- **WHEN** uma pessoa executa um request com entrada inválida documentada
- **THEN** a coleção verifica que a API responde `400 Bad Request`

#### Scenario: Recurso inexistente

- **WHEN** uma pessoa executa um request de consulta para um identificador positivo inexistente
- **THEN** a coleção verifica que a API responde `404 Not Found`

#### Scenario: Recurso duplicado

- **WHEN** uma pessoa executa novamente um cadastro usando o mesmo identificador lógico de um recurso criado anteriormente para o mesmo usuário
- **THEN** a coleção documenta essa pré-condição e verifica que a API responde `409 Conflict`

### Requirement: Diagrama simplificado de entidades

O repositório SHALL conter um diagrama versionável e visualizável das entidades persistidas `Corretora` e `Acao`, seus principais campos, chaves e a identidade lógica de `Acao` como `(ticker, mercado)`. O diagrama MUST representar que não existe relacionamento obrigatório entre essas entidades no modelo atual.

#### Scenario: Visualização do modelo

- **WHEN** uma pessoa abre o arquivo do diagrama em ferramenta compatível ou visualizador Markdown
- **THEN** identifica as entidades, seus campos essenciais, a distinção de mercados e a ausência de vínculo obrigatório entre corretora e ação

### Requirement: Instruções de entrega

O README SHALL indicar a localização, o formato e o modo de uso da coleção e do diagrama, sem duplicar contratos já documentados nem incluir informações secretas.

#### Scenario: Orientação reproduzível

- **WHEN** uma pessoa segue as instruções do README em ambiente local configurado
- **THEN** consegue localizar os dois artefatos e entender como importá-los ou visualizá-los

### Requirement: Reproducible test coverage report and quality gate
The project SHALL generate a reproducible Java code coverage report during the standard Maven verification lifecycle. The verification SHALL fail when aggregate line coverage is below 80% or aggregate branch coverage is below 55%. The report SHALL be available as HTML and machine-readable XML, and local build artifacts SHALL remain untracked.

#### Scenario: Verification meets the coverage thresholds
- **WHEN** a developer runs the Maven wrapper verification with the automated test suite passing and both aggregate coverage thresholds met
- **THEN** the build succeeds and produces HTML and XML coverage reports under the Maven build output directory

#### Scenario: Verification fails below either threshold
- **WHEN** the automated tests pass but aggregate line coverage is below 80% or aggregate branch coverage is below 55%
- **THEN** the Maven verification fails and reports which coverage threshold was missed

#### Scenario: Coverage results can be inspected locally
- **WHEN** a developer completes the Maven verification
- **THEN** the developer can open the HTML report and consume the XML report without exposing secrets or adding generated reports to version control
