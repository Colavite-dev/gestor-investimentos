# Arquitetura do Sistema

## Visão Geral

O sistema utilizará Java e Spring Boot seguindo arquitetura em camadas.

Fluxo principal:

Controller
→ Service
→ Repository
→ PostgreSQL

Integrações externas deverão utilizar abstrações adicionais:

Service
→ Port/Provider
→ Adapter
→ API externa

## Camadas

### Controller

Responsável por:

- receber requisições HTTP;
- validar DTOs;
- encaminhar chamadas para services;
- retornar respostas HTTP.

Não deve conter regra de negócio.

### Service

Responsável por:

- regras de negócio;
- orquestração de operações;
- comunicação com repositories;
- comunicação com abstrações de integrações externas.

### Repository

Responsável pela persistência através de Spring Data JPA.

### Entity

Representa os dados persistidos.

### DTO

Representa entrada e saída da API.

Entidades JPA não devem ser usadas diretamente como contrato HTTP.

## Integrações externas

APIs externas deverão ser isoladas.

Exemplo:

CotacaoService
→ CotacaoProvider
→ BrapiAdapter

ou:

CotacaoService
→ CotacaoProvider
→ AlphaVantageAdapter

O domínio não deve depender diretamente do formato das respostas externas.

Adapters deverão converter respostas externas para modelos internos.

## Pacotes iniciais

Estrutura candidata:

com.<projeto>.gestorinvestimento

controller
service
repository
entity
dto
exception
integration
config
mapper

Dentro de integration:

integration/
cnpj/
cep/
financialinstitution/
quote/

A estrutura poderá evoluir conforme surgirem necessidades concretas.

## Persistência

Banco principal:

PostgreSQL.

Controle de schema:

Flyway.

Hibernate/JPA não deverá ser utilizado como mecanismo oficial de criação automática do schema em produção.

## Configuração

Informações sensíveis ou dependentes do ambiente não devem ficar diretamente no código.

Exemplos:

- senha do banco;
- API keys;
- tokens;
- URLs configuráveis.

Essas informações deverão ser recebidas através de configuração externa e variáveis de ambiente.

## Testes

O projeto deverá priorizar:

- testes unitários para regras de negócio;
- testes de integração para persistência e endpoints relevantes;
- mocks/fakes para dependências externas quando apropriado.

Chamadas reais a APIs externas não devem tornar toda a suíte de testes dependente da disponibilidade dos terceiros.