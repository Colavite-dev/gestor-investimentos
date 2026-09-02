## Why

A aplicação ainda não possui uma infraestrutura local de persistência reproduzível nem configuração suficiente para validar o schema com segurança. Esta change prepara a base técnica exigida pelo PRD e pelas decisões do projeto antes da introdução de entidades e regras de negócio.

## What Changes

- Disponibilizar PostgreSQL em versão fixa por Docker Compose, com volume persistente e healthcheck.
- Configurar o Spring Boot por variáveis de ambiente para usar o PostgreSQL como datasource principal, mantendo a aplicação em execução local.
- Adotar Flyway como único mecanismo oficial de evolução do schema e configurar o Hibernate com `ddl-auto=validate`.
- Fornecer `.env.example` sem credenciais reais e impedir o versionamento do arquivo `.env` local.
- Definir uma estratégia de teste para que `contextLoads` não dependa de um PostgreSQL externo, admitindo H2 somente no ambiente de teste.
- Documentar a inicialização do banco e a validação local da aplicação e das migrations.
- Não introduzir entidades, repositories, services, controllers, DTOs, integrações externas nem infraestrutura de aplicação em contêiner.

## Capabilities

### New Capabilities

- `database-infrastructure`: Define o ambiente PostgreSQL local, a configuração externa do datasource, a governança do schema por Flyway e os procedimentos mínimos de inicialização e validação.

### Modified Capabilities

Nenhuma.

## Impact

- Infraestrutura local: novo arquivo Docker Compose, serviço PostgreSQL e volume persistente.
- Configuração: propriedades do Spring Boot, variáveis de ambiente, exemplo de ambiente e proteção de segredos locais.
- Dependências: ativação/configuração de Flyway e uso dos drivers PostgreSQL e, exclusivamente para testes, H2.
- Testes: configuração isolada para o teste de carregamento do contexto.
- Documentação: instruções mínimas para preparar, iniciar e validar o ambiente local.
- Banco: uso inicial do schema `public`, sem tabelas ou entidades de negócio nesta change.
