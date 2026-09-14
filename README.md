# Adapt Invest — Gestor de Investimentos

Aplicação acadêmica para gestão de corretoras, ativos financeiros e carteiras. O backend é uma API REST Java/Spring Boot; o frontend é uma aplicação React independente que consome exclusivamente essa API.

## Stack e arquitetura

- Java 17, Spring Boot, Spring Data JPA, Bean Validation e Maven Wrapper;
- PostgreSQL como banco de runtime; H2 em memória somente nos testes automatizados;
- Flyway com migrations V1–V8 e Hibernate em `ddl-auto=validate`;
- React, TypeScript e Vite em `frontend/`;
- Controllers → Services → Repositories → PostgreSQL; integrações isoladas por providers/adapters.

## Segurança e isolamento

A API usa JWT Bearer stateless, assinado com HS256. Senhas são persistidas apenas como hash BCrypt. Registros públicos criam usuários com role `USER`; endpoints `/admin/**` exigem `ADMIN`.

Cada carteira pertence obrigatoriamente ao usuário autenticado. O frontend não envia `usuarioId` como autorização: a identidade vem do JWT/SecurityContext. Carteiras, operações, posições e resumos de outro usuário retornam `404`, inclusive para `ADMIN`, que não recebe acesso implícito a dados financeiros privados. Ações e dados de mercado são globais e compartilhados.

## Integrações externas

- BrasilAPI: dados cadastrais de CNPJ;
- ViaCEP: validação e enriquecimento de endereço;
- CVM: elegibilidade de participante intermediário;
- brapi: ativos e cotações brasileiras;
- Twelve Data: ativos e cotações dos Estados Unidos.

Os detalhes de endpoints externos, limites, falhas e testes opt-in estão em [Integrações externas](docs/integrations/external-apis.md).

## Configuração e execução

Pré-requisitos: JDK 17, Docker Desktop com Docker Compose e Node.js/npm para o frontend.

```powershell
Copy-Item .env.example .env
```

Preencha apenas o `.env` local. Ele é ignorado pelo Git; não inclua credenciais em documentação ou Postman. O arquivo `.env.example` lista `DB_*`, `JWT_SECRET`, `APP_DEMO_ADMIN_ENABLED`, `BRAPI_TOKEN` e `TWELVE_DATA_API_KEY` sem valores secretos.

### Backend e PostgreSQL

O Compose publica PostgreSQL na porta de host `5433` por padrão. Inicie backend e banco no Windows:

```powershell
.\scripts\start-local.ps1
```

O script carrega do `.env` somente as variáveis permitidas de banco, JWT, administrador de demonstração e providers externos, aguarda o PostgreSQL saudável e inicia o Spring Boot. `APP_DEMO_ADMIN_ENABLED` é exclusivo para demonstração/local, permanece desabilitado por padrão e não deve ser habilitado em ambiente público ou de produção. Alternativamente:

```powershell
docker compose --env-file .env up -d postgres
.\mvnw.cmd spring-boot:run
```

### Frontend

Em outro terminal:

```powershell
cd frontend
Copy-Item .env.example .env
npm install
npm run dev
```

`VITE_API_BASE_URL` tem como padrão `http://localhost:8080`.

## Testes

```powershell
.\mvnw.cmd verify
cd frontend
npm test
npm run build
npm run lint
```

Os testes normais usam H2, Flyway e doubles/mocks; não dependem de Docker, internet ou credenciais externas. Testes PostgreSQL e testes reais de providers são opt-in.

## Endpoints

Todos os endpoints, exceto registro e login, exigem `Authorization: Bearer <token>`.

| Área | Endpoints reais |
|---|---|
| Auth | `POST /auth/register`, `POST /auth/login`, `GET /auth/me` |
| Admin | `GET /admin/users`, `GET /admin/metrics` |
| Corretoras | `POST /corretoras`, `GET /corretoras`, `GET /corretoras/{id}`, `GET /corretoras/cnpj/{cnpj}` |
| Ações | `POST /acoes`, `GET /acoes`, `GET /acoes/{id}`, `GET /acoes/ticker/{ticker}?mercado=...`, `GET /acoes/pesquisar?q=...`, `GET /acoes/catalogo?mercado=...&q=...&page=...&size=...`, `POST /acoes/resolver`, `PUT /acoes/{id}/atualizar-cotacao` |
| Histórico | `GET /acoes/{acaoId}/historico-cotacoes?de=...&ate=...` |
| Carteiras | `POST /carteiras`, `GET /carteiras`, `GET /carteiras/{id}`, `GET /carteiras/{id}/operacoes`, `GET /carteiras/{id}/posicoes`, `GET /carteiras/{id}/resumo` |
| Operações | `POST /operacoes`, `GET /operacoes/{id}` |

Use a [coleção Postman](docs/api/gestor-investimento.postman_collection.json): o login grava o `accessToken` retornado na variável de coleção `token`, sem token ou segredo pré-configurado.

## Banco e Flyway

| Migration | Responsabilidade |
|---|---|
| V1 | Corretoras e unicidade de CNPJ. |
| V2 | Ações, mercado, moeda e unicidade `(ticker, mercado)`. |
| V3 | Carteiras. |
| V4 | Operações, FKs, checks e índice cronológico. |
| V5 | Histórico de cotações e índice. |
| V6 | Precisão financeira `NUMERIC(19,8)` e cotação positiva. |
| V7 | Usuários, roles e hash BCrypt. |
| V8 | Ownership obrigatório `Carteira -> Usuario` e unicidade de nome por usuário. |

A V8 exige que toda carteira tenha um usuário proprietário. Um banco legado que contenha carteiras sem proprietário pode exigir tratamento ou reset controlado antes da aplicação da migration; revise os dados e o ambiente antes de qualquer ação destrutiva.

## Pendências acadêmicas

- A identidade persistida de ativo é `(ticker, mercado)`; o enunciado menciona ticker duplicado literalmente. Esta interpretação ainda depende de confirmação do professor.
- PostgreSQL é o banco principal e H2 é usado nos testes; MySQL não é utilizado. A redação que cita H2/MySQL/PostgreSQL ainda depende de confirmação do professor.
- O projeto foi executado individualmente; isso não deve ser entendido como autorização para divergir da orientação de grupo sem confirmação acadêmica.

## Documentação

- [PRD](docs/product/PRD.md)
- [Arquitetura](docs/architecture/ARCHITECTURE.md)
- [Modelo de entidades](docs/architecture/entity-model.md)
- [Decisões](docs/decisions/DECISIONS.md)
- [Integrações externas](docs/integrations/external-apis.md)
