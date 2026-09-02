## 1. Dependências de persistência

- [x] 1.1 Adicionar `flyway-core` e `flyway-database-postgresql` ao `pom.xml`, manter PostgreSQL disponível no runtime normal e declarar H2 com escopo Maven `test`, verificando pela árvore de dependências que H2 aparece no classpath de teste, permanece ausente e indisponível no runtime normal e que não foram introduzidos MySQL, Testcontainers ou carregador de `.env`.
## 2. PostgreSQL local e configuração de ambiente

- [x] 2.1 Criar `compose.yaml` com o serviço `postgres` usando `postgres:17.11-bookworm`, publicação `${DB_PORT:-5433}:5432`, mapeamento das variáveis `DB_*`, volume `postgres_data` e healthcheck por `pg_isready`; verificar a estrutura com `docker compose --env-file .env config` usando um `.env` local de validação.
- [x] 2.2 Criar `.env.example` somente com `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD` sem valores, adicionar `/.env` ao `.gitignore` e verificar que o exemplo não contém credenciais e que `.env.example` não é abrangido pela regra de ignore.
- [x] 2.3 Iniciar o serviço `postgres`, aguardar o estado saudável, registrar por consulta somente leitura o `system_identifier` do cluster, recriar o contêiner sem remover `postgres_data` e confirmar que o identificador permanece igual; verificar que o procedimento não cria tabelas permanentes, não executa DDL nem altera o schema fora do Flyway.
  - Evidência de validação manual: o mesmo `system_identifier = 7680654737095946278` foi obtido antes e depois de `docker compose up -d --force-recreate postgres`, usando somente `SELECT system_identifier FROM pg_control_system();` e preservando o volume `postgres_data`.
## 3. Datasource e governança do schema

- [x] 3.1 Configurar `application.properties` para montar o datasource PostgreSQL com `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD`, usando `5433` como porta padrão do host e os demais padrões definidos apenas para valores não secretos, e verificar que a inicialização falha explicitamente quando `DB_PASSWORD` está ausente.
- [x] 3.2 Configurar Flyway habilitado no schema `public` sem URL ou credenciais próprias, configurar Hibernate com `ddl-auto=validate` e schema `public`, e verificar nos logs de uma inicialização contra PostgreSQL que ambos usam o datasource principal e que Hibernate não executa DDL de criação ou atualização.
  - Evidência de validação manual: a aplicação iniciou contra `jdbc:postgresql://localhost:5433/gestor_investimento`; Hikari e Flyway usaram o mesmo datasource, Flyway operou em `public` e Hibernate inicializou com `ddl-auto=validate` sem criar ou atualizar o schema de negócio.
- [x] 3.3 Manter esta change sem arquivo SQL de migration e verificar que a aplicação inicia com Flyway ativo e zero migrations pendentes; reservar `V1__...sql` para a primeira mudança que introduzir estrutura persistente real.
  - Evidência de validação manual: não existem arquivos `V*.sql`; Flyway validou zero migrations, criou somente `public.flyway_schema_history` e informou que o schema `public` estava atualizado, sem migration necessária.

## 4. Isolamento do teste de contexto

- [x] 4.1 Criar `src/test/resources/application-test.properties` com H2 em memória no modo PostgreSQL, Hibernate `validate` e Flyway desabilitado, e verificar que a configuração não referencia Docker nem Testcontainers.
- [x] 4.2 Ativar o perfil `test` em `GestorInvestimentoApplicationTests` e verificar com `mvnw.cmd test` que `contextLoads` passa sem PostgreSQL ou Docker em execução.

## 5. Documentação e validação integrada

- [x] 5.1 Criar `README.md` documentando pré-requisitos, preparação de `.env`, exportação das variáveis `DB_*` no PowerShell/IDE, inicialização e parada do PostgreSQL, execução local da aplicação e preservação do volume; verificar o procedimento em um ambiente local limpo.
  - Evidência de validação manual: em uma nova sessão do PowerShell, o fluxo documentado foi concluído desde a criação de `.env` até a inicialização da aplicação e os builds; `docker compose down` preservou `postgres_data`, e o reinício manteve `system_identifier = 7680654737095946278`.
- [x] 5.2 Documentar comandos para validar o Compose, o healthcheck, a persistência do volume por consulta somente leitura ao identificador do cluster, a conexão da aplicação, a execução do Flyway, a validação do Hibernate e os testes, distinguindo `docker compose down` de `docker compose down -v`; verificar que o procedimento não orienta criar tabelas ou alterar schema fora do Flyway e que cada resultado esperado está explícito.
- [x] 5.3 Executar a validação integrada com PostgreSQL saudável e aplicação no host, seguida de `mvnw.cmd test`, e registrar qualquer desvio em relação à spec antes de marcar a change como implementada.
  - Evidência de validação manual: PostgreSQL 17.11 ficou saudável em `localhost:5433`, a aplicação iniciou no host até `Started GestorInvestimentoApplication`, `mvnw.cmd test` e `mvnw.cmd verify` concluíram com sucesso e `openspec validate setup-database-infrastructure --strict` declarou a change válida, sem desvio reportado.
