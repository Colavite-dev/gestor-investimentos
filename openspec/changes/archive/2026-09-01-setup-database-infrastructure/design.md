## Context

O projeto possui Spring Data JPA e os drivers H2 e PostgreSQL, mas ainda não possui configuração de datasource, Flyway, Docker Compose ou separação de configuração para testes. O teste `contextLoads` carrega o contexto completo sem perfil específico. Conforme `proposal.md`, esta change deve preparar a persistência sem introduzir modelo de negócio. A aplicação continuará executando no host e somente o PostgreSQL será conteinerizado.

O requisito acadêmico que cita H2, MySQL e PostgreSQL é ambíguo. Este design segue a interpretação já registrada na DEC-010: PostgreSQL principal, H2 opcional em testes e ausência de MySQL.

Durante a validação local, foi identificado que uma instalação nativa do PostgreSQL e o backend do Docker disputavam a porta `5432` no host. A autenticação direta dentro do contêiner funcionou, enquanto a conexão publicada por `localhost:5432` falhou com SQLSTATE `28P01`, demonstrando que a aplicação poderia alcançar a instância local incorreta. Por isso, o PostgreSQL do projeto será publicado por padrão em `localhost:5433`, mantendo a porta interna `5432` no contêiner.

## Goals / Non-Goals

**Goals:**

- Tornar o ambiente PostgreSQL local determinístico e persistente.
- Usar um único conjunto explícito de variáveis `DB_*` no Compose e no Spring Boot.
- Garantir que Flyway e Hibernate tenham responsabilidades não sobrepostas.
- Manter o teste básico independente de infraestrutura externa.
- Permitir que um novo desenvolvedor inicialize e valide o ambiente por documentação curta.

**Non-Goals:**

- Criar schema de domínio, tabelas, entidades ou repositories.
- Executar a aplicação Spring Boot em Docker ou criar seu Dockerfile.
- Adicionar Testcontainers ou um carregador de `.env` ao classpath.
- Projetar ambientes de produção, alta disponibilidade, backup ou observabilidade avançada.

## Decisions

### 1. Imagem PostgreSQL fixa

Será usada `postgres:17.11-bookworm`, uma tag exata da imagem oficial. PostgreSQL 17 oferece uma linha madura e a variante Debian Bookworm favorece previsibilidade e diagnóstico em comparação com uma base Alpine. A tag não flutuará com atualizações de patch.

Alternativas consideradas:

- `postgres:latest`: rejeitada porque torna o ambiente não reproduzível e pode introduzir mudança de major sem revisão.
- `postgres:17-bookworm`: rejeitada porque ainda flutua entre patches.
- PostgreSQL 18: não é necessário adotar a major mais nova para esta base acadêmica; a linha 17 reduz novidade sem sacrificar suporte.
- Variante Alpine: menor, mas sem benefício concreto que compense diferenças de base e diagnóstico nesta etapa.

### 2. Arquivo, serviço e volume do Docker Compose

O arquivo será `compose.yaml`. O serviço será `postgres` e o volume nomeado será `postgres_data`, montado no diretório de dados recomendado pela imagem para a major escolhida. Não será definido `container_name`, evitando colisões entre checkouts e preservando o namespace automático do Compose.

O healthcheck usará `pg_isready` com o banco e usuário configurados, intervalo de 5 segundos, timeout de 5 segundos, 10 tentativas e `start_period` de 10 segundos. A política de reinício não será configurada nesta etapa; o Compose é um recurso de desenvolvimento controlado manualmente.

A persistência do volume será validada comparando, antes e depois da recriação do contêiner, o identificador persistente do cluster retornado por uma consulta somente leitura, como `SELECT system_identifier FROM pg_control_system()`. A verificação não criará tabela de controle, não executará DDL e não fará qualquer alteração permanente de schema; toda evolução estrutural continuará exclusiva do Flyway.

Alternativas consideradas: nomes específicos e longos foram descartados por redundância com o nome do projeto; volume anônimo foi descartado porque dificulta reconhecer e preservar os dados.

### 3. Variáveis de ambiente definitivas

O contrato de configuração será:

| Variável | Uso | Obrigatoriedade/padrão |
|---|---|---|
| `DB_HOST` | Host usado pelo datasource da aplicação local | padrão `localhost` |
| `DB_PORT` | Porta publicada pelo Compose e usada pelo datasource | padrão `5433` |
| `DB_NAME` | Banco criado pelo contêiner e usado pela aplicação | padrão `gestor_investimento` |
| `DB_USERNAME` | Usuário criado pelo contêiner e usado pela aplicação | padrão `gestor_investimento` |
| `DB_PASSWORD` | Senha do PostgreSQL e do datasource | obrigatória, sem padrão |

O Compose mapeará `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD` para `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD` dentro do contêiner. Isso evita expor dois conjuntos públicos de variáveis para a mesma conexão.

`.env.example` listará somente essas chaves com valores vazios, conforme `AGENTS.md`. O `.env` real será ignorado com uma entrada específica `/.env`, sem ignorar `.env.example`. Nenhuma biblioteca será adicionada para ler `.env`: o Compose poderá consumi-lo, enquanto a aplicação local receberá as mesmas variáveis pelo shell ou pela configuração da IDE, como explicado na documentação.

Alternativas consideradas: usar diretamente `POSTGRES_*` no Spring foi descartado por acoplar o contrato da aplicação à imagem Docker; credenciais padrão foram descartadas porque facilitariam uso acidental e violariam a disciplina de segredos.

### 4. Porta padrão e sobrescrita

O contêiner continuará ouvindo em `5432`. O host publicará `${DB_PORT:-5433}:5432`, e a URL JDBC usará `${DB_PORT:5433}`. Portanto, uma única sobrescrita mantém Compose e aplicação coerentes. O padrão `5433` evita a colisão diagnosticada com uma instalação nativa do PostgreSQL que já utiliza `localhost:5432`.

`DB_HOST` permanece `localhost` porque a aplicação roda fora do Compose. Uma futura conteinerização da aplicação deverá revisar host e rede em change separada.

### 5. Datasource, Hibernate e Flyway

`application.properties` formará a URL `jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5433}/${DB_NAME:gestor_investimento}`, usará `DB_USERNAME` e exigirá `DB_PASSWORD`. O driver PostgreSQL continuará em runtime.

Serão adicionados `flyway-core` e `flyway-database-postgresql`, com versões gerenciadas pelo Spring Boot. Não serão configuradas propriedades próprias de URL, usuário ou senha para Flyway; desse modo, a autoconfiguração usará o datasource principal. Flyway ficará habilitado para o runtime normal, com `default-schema` e `schemas` definidos como `public`.

Hibernate será configurado com `spring.jpa.hibernate.ddl-auto=validate` e schema padrão `public`. Ele poderá impedir a inicialização diante de incompatibilidades, mas nunca criará nem atualizará tabelas.

Alternativas consideradas: `ddl-auto=create`, `update` e `create-drop` foram rejeitadas por concorrer com Flyway; um datasource separado para Flyway foi rejeitado por acrescentar credenciais e complexidade sem necessidade.

### 6. Estratégia para `contextLoads`

O teste será marcado com o perfil `test`, e `src/test/resources/application-test.properties` configurará um H2 em memória em modo de compatibilidade PostgreSQL. A dependência H2 no `pom.xml` terá escopo Maven `test`, ficando ausente do classpath e indisponível no runtime normal da aplicação. Nesse perfil, Hibernate continuará em `validate` e Flyway será desabilitado, pois o objetivo de `contextLoads` é verificar a composição do contexto, não a compatibilidade das migrations com outro banco.

Essa decisão mantém a suíte básica executável sem Docker e não apresenta H2 como banco principal. A validação real de Flyway ocorrerá no procedimento local contra PostgreSQL. Testcontainers não será adicionado nesta change.

Alternativas consideradas: exigir o Compose para todo teste foi descartado por tornar o teste básico dependente do ambiente; executar migrations PostgreSQL no H2 foi descartado porque pode mascarar incompatibilidades; Testcontainers foi explicitamente excluído.

### 7. Migration técnica inicial

Não será criada migration técnica inicial. O schema `public` já existe no banco criado pela imagem, e esta change não possui objeto persistente legítimo. Uma migration vazia, `SELECT 1` ou recriação redundante do schema acrescentaria histórico sem representar evolução estrutural.

Flyway será instalado, habilitado e validado contra o datasource principal mesmo com zero migrations de domínio. A primeira change que introduzir estrutura persistente criará `V1__...sql`; depois de aplicada, nenhuma migration será reescrita.

Alternativa considerada: uma `V1` de bootstrap foi descartada por não ter efeito técnico necessário e consumir a primeira versão com conteúdo artificial.

### 8. Inicialização, validação e documentação local

Será criado um `README.md` com o seguinte fluxo:

1. Confirmar Java 17, Docker Compose e Maven Wrapper.
2. Copiar `.env.example` para `.env` e preencher os cinco valores localmente.
3. Iniciar somente o serviço `postgres` com `docker compose --env-file .env up -d postgres`.
4. Confirmar `docker compose ps`, o estado saudável e uma consulta com `psql` executado no contêiner.
5. Exportar as mesmas variáveis `DB_*` no PowerShell/IDE e executar `mvnw.cmd spring-boot:run` no host.
6. Confirmar nos logs a conexão, a execução do Flyway e a validação do Hibernate.
7. Executar `mvnw.cmd test` para validar o perfil isolado.
8. Parar o serviço com `docker compose down`, preservando `postgres_data`.

A documentação distinguirá `docker compose down` de `docker compose down -v`: o segundo remove dados e será apresentado somente como limpeza destrutiva opcional.

## Risks / Trade-offs

- [A tag fixa deixa de receber patches automaticamente] → Atualizar a tag deverá ser uma mudança deliberada, validada e revisada.
- [H2 não reproduz integralmente o PostgreSQL] → Limitar H2 ao carregamento do contexto e validar Flyway manualmente contra PostgreSQL; testes futuros sensíveis ao banco exigirão estratégia própria.
- [O arquivo `.env` não é carregado automaticamente pelo Spring executado no host] → Documentar claramente a exportação no shell/IDE e reutilizar os mesmos nomes `DB_*`.
- [Usar `public` reduz isolamento dentro do banco] → Aceitar a simplicidade inicial e revisar somente quando surgir necessidade de múltiplos schemas.
- [A ausência de migration inicial deixa zero migrations nesta etapa] → Validar a inicialização do Flyway e reservar `V1` para a primeira estrutura real.

## Migration Plan

1. Adicionar as dependências Flyway compatíveis com PostgreSQL.
2. Adicionar `compose.yaml`, o contrato `DB_*`, `.env.example` e a proteção de `/.env`.
3. Configurar datasource, Flyway e Hibernate para o runtime normal.
4. Isolar `contextLoads` no perfil H2 de teste.
5. Adicionar o procedimento ao `README.md`.
6. Validar configuração do Compose, healthcheck, subida local, Flyway, Hibernate e testes.

Rollback: parar o Compose e reverter os arquivos de configuração e dependências introduzidos pela implementação. O volume `postgres_data` deverá ser preservado por padrão; sua remoção exigirá uma ação explícita e consciente. Como não haverá migration técnica ou de negócio, esta change não exige rollback de schema.
