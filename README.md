# Gestor de Investimentos

API REST acadêmica em Java/Spring Boot para cadastro e consulta de corretoras e ações com dados de fontes externas. O projeto também inclui evoluções de carteira, operações, posições e histórico de cotações.

## Objetivos acadêmicos

O núcleo acadêmico cobre cadastro e consulta de corretoras por CNPJ, enriquecimento cadastral e de CEP, validação perante a CVM, cadastro e consulta de ações brasileiras e americanas, consulta/atualização de cotações e prevenção de duplicidade.

As evoluções implementadas — Carteiras, Operações, posições, preço médio, bloqueio de venda a descoberto, histórico de cotações, integridade transacional, Docker/Flyway e testes PostgreSQL opt-in — são diferenciais do projeto, não requisitos originais obrigatórios.

## Funcionalidades

### Corretoras

- Cadastro por CNPJ com validação local e consulta à BrasilAPI.
- Enriquecimento/validação de endereço pelo ViaCEP.
- Elegibilidade pela CVM: basta existir ao menos um registro ativo de Corretora ou Distribuidora para o CNPJ; o resultado não depende da ordem do CSV.
- Listagem e busca por ID ou CNPJ; CNPJ duplicado retorna conflito.

### Ações

- Cadastro e consulta de ações dos mercados `BRASIL` e `ESTADOS_UNIDOS`.
- brapi para mercado brasileiro e Twelve Data para mercado americano.
- Atualização de cotação, histórico persistido e atomicidade entre cotação atual e histórico.
- Identidade atual: `(ticker, mercado)`; detalhes e pendência acadêmica em [Decisões](#decisões-arquiteturais).

### Carteiras, operações e posições

- Cadastro, listagem e consulta de carteiras.
- Registro de compras e vendas com quantidade fracionária de até oito casas decimais.
- Posições líquidas, preço médio ponderado, resumo por moeda e exclusão de posição zerada da lista atual.
- Venda sem saldo ou que torne uma sequência histórica negativa é rejeitada; não há venda a descoberto.

## Arquitetura e tecnologias

```text
Controller → Service → Repository → PostgreSQL
Service → Port/Provider → Adapter → API externa
```

Java 17 (alvo do projeto), Spring Boot, Spring Data JPA, Bean Validation, PostgreSQL, H2 em testes, Flyway, Docker Compose e Maven Wrapper. Chamadas externas ocorrem fora das transações curtas de escrita.

## Integrações externas

O projeto usa BrasilAPI, ViaCEP, CVM, brapi e Twelve Data. Consulte [a documentação detalhada](docs/integrations/external-apis.md) para fluxo, configuração, timeout, erros e testes opt-in.

## Pré-requisitos e configuração

- JDK compatível com Java 17;
- Docker Desktop com Docker Compose;
- use o Maven Wrapper incluído, sem Maven global.

```powershell
Copy-Item .env.example .env
```

`.env.example` é um modelo seguro. `.env` é local, ignorado pelo Git e não deve conter valores em documentação ou Postman.

### Variáveis de ambiente

| Categoria | Variáveis | Uso |
|---|---|---|
| Banco | `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | `DB_PASSWORD` é necessária para o Compose; as demais têm defaults. |
| Credenciais | `BRAPI_TOKEN`, `TWELVE_DATA_API_KEY` | Token brapi opcional; chave Twelve Data necessária para ações americanas. |
| BrasilAPI | `BRASIL_API_BASE_URL`, `BRASIL_API_CONNECT_TIMEOUT`, `BRASIL_API_READ_TIMEOUT` | Overrides; defaults 2s/5s. |
| ViaCEP | `VIA_CEP_BASE_URL`, `VIA_CEP_CONNECT_TIMEOUT`, `VIA_CEP_READ_TIMEOUT` | Overrides; defaults 2s/5s. |
| CVM | `CVM_PARTICIPANTS_DATASET_URL`, `CVM_PARTICIPANTS_CONNECT_TIMEOUT`, `CVM_PARTICIPANTS_READ_TIMEOUT`, `CVM_PARTICIPANTS_REFRESH_INTERVAL` | Overrides; defaults 2s/10s/24h. |
| brapi | `BRAPI_BASE_URL`, `BRAPI_CONNECT_TIMEOUT`, `BRAPI_READ_TIMEOUT` | Overrides; defaults 2s/5s. |
| Twelve Data | `TWELVE_DATA_BASE_URL`, `TWELVE_DATA_CONNECT_TIMEOUT`, `TWELVE_DATA_READ_TIMEOUT` | Overrides; defaults 2s/5s. |

## PostgreSQL com Docker

O Compose usa `postgres:17.11-bookworm`, volume `postgres_data`, healthcheck `pg_isready` e publica `${DB_PORT:-5433}:5432`: porta padrão do host `5433`, interna `5432`.

```powershell
docker compose --env-file .env config --quiet
docker compose --env-file .env up -d postgres
docker compose --env-file .env ps
```

Espere o serviço ficar `healthy`. Para parar preservando dados:

```powershell
docker compose --env-file .env down
```

Não use `docker compose down -v` como procedimento normal, pois remove o volume local.

## Como executar

No PowerShell, exporte os valores de banco usados no `.env` e execute o Wrapper:

```powershell
$env:DB_HOST = 'localhost'
$env:DB_PORT = '5433'
$env:DB_NAME = 'gestor_investimento'
$env:DB_USERNAME = 'gestor_investimento'
$env:DB_PASSWORD = Read-Host 'DB_PASSWORD'
.\mvnw.cmd spring-boot:run
```

## Como testar

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

Os testes normais usam H2 em memória, Flyway e Hibernate `ddl-auto=validate`; não dependem de Docker, internet ou credenciais externas.

### PostgreSQL opt-in

Com PostgreSQL local saudável:

```powershell
$env:RUN_POSTGRES_IT = 'true'
.\mvnw.cmd -Dtest=CorretoraPostgresIT test
```

O teste usa stubs para providers e valida commit, rollback e unicidade de CNPJ no PostgreSQL real.

## Endpoints

| Área | Mappings |
|---|---|
| Corretoras | `POST /corretoras`, `GET /corretoras`, `GET /corretoras/{id}`, `GET /corretoras/cnpj/{cnpj}` |
| Ações | `POST /acoes`, `GET /acoes`, `GET /acoes/{id}`, `GET /acoes/ticker/{ticker}?mercado=...`, `PUT /acoes/{id}/atualizar-cotacao` |
| Histórico | `GET /acoes/{acaoId}/historico-cotacoes?de=...&ate=...` |
| Carteiras | `POST /carteiras`, `GET /carteiras`, `GET /carteiras/{id}`, `GET /carteiras/{id}/operacoes`, `GET /carteiras/{id}/posicoes`, `GET /carteiras/{id}/resumo` |
| Operações | `POST /operacoes`, `GET /operacoes/{id}` |

Exemplos importáveis estão na [coleção Postman](docs/api/gestor-investimento.postman_collection.json).

## Tratamento de erros

| Status | Significado |
|---|---|
| 400 | Corpo, parâmetro ou validação de entrada inválidos. |
| 404 | Recurso persistido não encontrado. |
| 409 | Duplicidade de Corretora, Ação ou Carteira. |
| 422 | Regra de negócio, CNPJ/CEP/ticker/instituição inválidos conforme o contexto. |
| 502 | Resposta externa inválida, malformada ou inconsistente. |
| 503 | Provider indisponível, sem autorização, em quota/rate limit, timeout ou falha de conexão. |

## Banco e Flyway

PostgreSQL é o banco de runtime. Flyway evolui o schema; Hibernate apenas o valida com `ddl-auto=validate`.

| Migration | Responsabilidade |
|---|---|
| V1 | Corretoras e unicidade de CNPJ. |
| V2 | Ações, mercado, moeda e unicidade `(ticker, mercado)`. |
| V3 | Carteiras. |
| V4 | Operações, chaves estrangeiras, checks e índice cronológico. |
| V5 | Histórico de cotações e índice determinístico. |
| V6 | Precisão `NUMERIC(19,8)` e check positivo da cotação atual. |

## Decisões arquiteturais

- [Arquitetura](docs/architecture/ARCHITECTURE.md)
- [Modelo de entidades](docs/architecture/entity-model.md)
- [Decisões](docs/decisions/DECISIONS.md)
- [Requisitos originais](docs/requirements/PROFESSOR_REQUIREMENTS.md)

### Pendências acadêmicas

- `(ticker, mercado)` é a identidade lógica/persistida atual. O enunciado literalmente menciona ticker duplicado; a decisão está **pendente de confirmação acadêmica**.
- PostgreSQL é o banco principal e H2 é usado em testes; MySQL não é usado. A interpretação do enunciado permanece **pendente de confirmação acadêmica**.

## Limitações conhecidas

- Suporte inicial a ações BR/EUA.
- Alias ou renomeação automática de ticker não são aceitos.
- Providers externos podem falhar; quotas dependem do provider/plano.
- Testes reais são opt-in.
- Não há autenticação, frontend, Swagger/OpenAPI, paginação, dashboard, retry automático ou cache genérico.

## Documentação adicional

- [Integrações externas](docs/integrations/external-apis.md)
- [Coleção Postman](docs/api/gestor-investimento.postman_collection.json)
- [Modelo Mermaid](docs/architecture/entity-model.md)
