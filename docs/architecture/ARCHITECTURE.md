# Arquitetura do Sistema

## Visão geral

O backend Spring Boot segue arquitetura em camadas:

```text
React/TypeScript → API REST → Controller → Service → Repository → PostgreSQL
                              Service → Provider/Port → Adapter → API externa
```

Controllers validam DTOs e expõem JSON; services aplicam regras, autorização de domínio e transações; repositories persistem entidades JPA; adapters convertem contratos externos em modelos internos. Entidades não são contratos HTTP.

## Persistência

PostgreSQL é o banco de runtime. Flyway controla migrations V1–V8; Hibernate apenas valida o schema com `ddl-auto=validate`. H2 em memória, em modo compatível com PostgreSQL, é usado somente pelos testes automatizados.

V7 cria `Usuario`, roles e hashes; V8 torna `Carteira.usuario_id` obrigatório, adiciona sua FK e muda a unicidade de carteira para `(usuario_id, nome_normalizado)`. Valores financeiros usam `BigDecimal` e `NUMERIC(19,8)`.

## Segurança e dados privados

`SecurityFilterChain` usa sessão stateless. `JwtAuthenticationFilter` valida JWT Bearer HS256; `JwtService` emite token com subject e role. `SecurityConfig` fornece BCrypt e permite somente `POST /auth/register` e `POST /auth/login` sem autenticação. `/admin/**` exige `ADMIN`; os demais endpoints exigem autenticação.

`Usuario 1:N Carteira`. Services e repositories filtram carteiras e operações pelo usuário autenticado. Um recurso financeiro de outro usuário é tratado como não encontrado; `ADMIN` não tem bypass implícito. Ações, cotações e catálogo de mercado permanecem globais.

## Domínios e integrações

- Corretoras: BrasilAPI, ViaCEP e CVM são consultadas antes da persistência curta em `CorretoraPersistenceService`.
- Ações/cotações: brapi atende Brasil e Twelve Data atende Estados Unidos; selectors encaminham por mercado. Cadastro e atualização gravam cotação atual e histórico atomicamente em `AcaoQuoteUpdatePersistenceService`.
- Carteiras/operações: `Operacao` relaciona carteira e ação. `CarteiraPosicaoService` calcula posição líquida, preço médio e resumo; a regra de não permitir venda a descoberto considera a ordem cronológica.

## Frontend e integração

O frontend React + TypeScript + Vite fica em `frontend/` e usa `VITE_API_BASE_URL`. Ele mantém o token em `sessionStorage`, envia Bearer para a API e não envia identidade de proprietário como autorização. O backend permite CORS somente para `http://localhost:5173` no ambiente local configurado.

## Testes

Testes normais usam H2, Flyway e mocks/doubles para providers. Há testes de autenticação, CORS, isolamento de dados, integrações e migrations. Testes PostgreSQL e chamadas reais de providers são opt-in, portanto a suíte padrão não requer Docker, internet ou credenciais.
