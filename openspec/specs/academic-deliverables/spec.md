# academic-deliverables Specification

## Purpose

Disponibilizar artefatos reproduzíveis para demonstrar, testar e entregar academicamente a API REST e o modelo persistente do sistema de investimentos.

## Requirements

### Requirement: Coleção executável da API

O repositório SHALL conter uma coleção Postman ou Insomnia importável, sem credenciais reais, que documente os endpoints de corretoras e ações atualmente disponíveis. A coleção MUST incluir exemplos de cadastro, consultas, atualização de cotação, validação de entrada, recurso inexistente e duplicidade, além de indicar variáveis de ambiente necessárias sem valores secretos.

#### Scenario: Importação sem segredo

- **WHEN** uma pessoa importa a coleção em uma ferramenta compatível
- **THEN** consegue visualizar e executar os requests documentados após configurar localmente a URL base, sem que qualquer token, senha ou chave real esteja no arquivo

#### Scenario: Cobertura dos endpoints atuais

- **WHEN** a pessoa percorre a coleção
- **THEN** encontra requests para os endpoints de corretoras e ações, incluindo `PUT /acoes/{id}/atualizar-cotacao`, com exemplos coerentes com os contratos atuais

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
