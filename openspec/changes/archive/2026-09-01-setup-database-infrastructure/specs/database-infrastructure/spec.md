## Purpose

Estabelecer uma infraestrutura local de persistência reproduzível, segura e validável para que a aplicação use PostgreSQL com schema governado exclusivamente por migrations.

## ADDED Requirements

### Requirement: PostgreSQL local reproduzível
A infraestrutura SHALL disponibilizar o PostgreSQL como banco principal por Docker Compose, usando uma imagem com versão fixa diferente de `latest`, um serviço com nome estável, healthcheck e volume nomeado persistente. Somente o banco de dados SHALL ser executado em contêiner nesta capability.

#### Scenario: Banco fica disponível para a aplicação local
- **WHEN** o desenvolvedor inicia os serviços definidos no Docker Compose com configuração de ambiente válida
- **THEN** o serviço PostgreSQL inicia, alcança o estado saudável e expõe o banco para a aplicação executada localmente

#### Scenario: Dados sobrevivem à recriação do contêiner
- **WHEN** o contêiner PostgreSQL é removido e recriado sem exclusão explícita do volume nomeado e a persistência é verificada por metadado somente leitura do cluster
- **THEN** o mesmo cluster permanece disponível sem que a validação crie tabelas permanentes, execute DDL ou altere o schema fora do Flyway

### Requirement: Configuração externa do datasource
A aplicação SHALL configurar seu datasource PostgreSQL principal exclusivamente por variáveis de ambiente para host, porta, nome do banco, usuário e senha. A porta do host SHALL possuir o padrão `5433` e SHALL permitir sobrescrita por variável de ambiente, enquanto o PostgreSQL SHALL continuar ouvindo na porta `5432` dentro do contêiner.

#### Scenario: Inicialização com configuração válida
- **WHEN** todas as variáveis obrigatórias possuem valores válidos e o PostgreSQL está saudável
- **THEN** a aplicação local conecta seu datasource principal ao banco configurado

#### Scenario: Sobrescrita da porta padrão
- **WHEN** o desenvolvedor define uma porta diferente de `5433` na variável correspondente e inicia o ambiente
- **THEN** o Docker Compose publica o PostgreSQL nessa porta e a aplicação usa o mesmo valor configurado

#### Scenario: Credencial obrigatória ausente
- **WHEN** a senha do banco não é fornecida ao ambiente local
- **THEN** a inicialização falha de forma explícita em vez de usar uma credencial real embutida no projeto

### Requirement: Proteção da configuração local
O projeto SHALL fornecer `.env.example` contendo somente nomes de variáveis, sem credenciais reais, e SHALL impedir que o arquivo `.env` local seja versionado.

#### Scenario: Preparação segura do ambiente
- **WHEN** um desenvolvedor consulta `.env.example`
- **THEN** ele identifica todas as variáveis necessárias sem encontrar valores secretos ou credenciais reais

#### Scenario: Arquivo local não é rastreado
- **WHEN** o desenvolvedor cria um arquivo `.env` na raiz do projeto
- **THEN** o sistema de versionamento o trata como arquivo ignorado

### Requirement: Governança do schema
Flyway SHALL ser o mecanismo oficial e exclusivo de criação e evolução do schema da aplicação, SHALL usar o datasource principal e SHALL operar inicialmente no schema `public`. Hibernate SHALL usar `ddl-auto=validate` e MUST NOT criar nem atualizar estruturas do banco.

#### Scenario: Aplicação inicia com schema compatível
- **WHEN** o PostgreSQL está acessível e todas as migrations existentes foram aplicadas com sucesso
- **THEN** o Flyway valida e migra o datasource principal antes de o Hibernate validar o mapeamento persistente

#### Scenario: Schema incompatível
- **WHEN** o schema do banco diverge das migrations ou dos mapeamentos persistentes
- **THEN** a aplicação falha na inicialização sem alterar automaticamente o schema pelo Hibernate

#### Scenario: Evolução futura do schema
- **WHEN** uma mudança estrutural de persistência for necessária após esta capability
- **THEN** ela é representada por uma nova migration versionada, sem modificar migrations já aplicadas

### Requirement: Teste de carregamento de contexto isolado
O teste automatizado `contextLoads` SHALL executar sem exigir PostgreSQL externo, Docker ou Testcontainers. O perfil isolado desse teste SHALL usar H2 para fornecer o datasource necessário ao carregamento do contexto. A dependência H2 SHALL possuir escopo Maven `test` e MUST NOT estar disponível no runtime normal da aplicação.

#### Scenario: Execução da suíte básica sem infraestrutura externa
- **WHEN** o desenvolvedor executa os testes automatizados básicos sem iniciar o Docker Compose
- **THEN** `contextLoads` conclui usando a configuração isolada de teste, não tenta acessar o PostgreSQL local e H2 permanece ausente do classpath de runtime normal

### Requirement: Operação local documentada
O projeto SHALL documentar os pré-requisitos, a criação da configuração local, a inicialização e parada do PostgreSQL, a execução local da aplicação e os comandos de validação do healthcheck, da conexão, das migrations e dos testes.

#### Scenario: Inicialização a partir da documentação
- **WHEN** um desenvolvedor com os pré-requisitos segue a documentação em um checkout novo
- **THEN** ele consegue configurar o ambiente, iniciar o PostgreSQL e executar localmente a aplicação

#### Scenario: Validação do ambiente
- **WHEN** o desenvolvedor executa o procedimento documentado de validação
- **THEN** ele consegue confirmar a saúde do contêiner, a conectividade da aplicação, o estado do Flyway e o resultado dos testes básicos
