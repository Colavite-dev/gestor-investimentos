## Context

O backend é uma API REST Spring Boot sem autenticação, com PostgreSQL/Flyway e CORS MVC restrito a `http://localhost:5173`; o frontend Vite consome a API por `fetch` e não possui estado de identidade. Esta change é transversal, acrescenta uma entidade persistida e modifica o acesso a recursos existentes; os comportamentos exigidos estão em `proposal.md` e nas delta specs.

## Goals / Non-Goals

**Goals:**

- Autenticação demonstrável, sem senha em texto puro e com distinção confiável entre `401` e `403`.
- Uma experiência SPA simples que sobreviva ao reload da aba e não use cookies cross-origin.
- Bootstrap local repetível do administrador acadêmico sem duplicação ou elevação insegura de uma conta existente.
- Preservar os contratos de payload e as regras de domínio atuais.

**Non-Goals:**

- Refresh token, revogação individual de JWT, recuperação/troca de senha, verificação de email, OAuth/social login, MFA, gestão de permissões granular ou isolamento dos dados de carteira por proprietário.
- Cookies de sessão, `allowCredentials=true`, CSRF baseado em cookie, painel administrativo de edição/exclusão de usuários ou métricas analíticas extensas.
- Suporte de múltiplas instâncias do backend ou configuração de produção completa.

## Decisions

### 1. JWT Bearer stateless e assinatura configurada

Será usado JWT Bearer assinado por HMAC SHA-256 e de curta duração (60 minutos), transmitido exclusivamente por `Authorization: Bearer`. `JWT_SECRET` será obrigatório no ambiente do processo, codificado em Base64 e validado como chave HMAC de ao menos 256 bits. Ele nunca será hardcoded em Java, gerado em runtime, impresso em log ou exposto ao frontend. `.env` local poderá contê-lo e continuará ignorado; `.env.example` terá somente `JWT_SECRET=`; o launcher local será ampliado no APPLY para encaminhá-lo sem exibir valor. Ausência ou formato/tamanho inválido interromperá a inicialização com mensagem clara e segura.

O token conterá somente: `sub` como ID estável do usuário, `username` normalizado, `role` decidido pelo backend, `iat` e `exp`. Não conterá email, password, passwordHash, dados de carteira ou operação. O backend cria e assina claims a partir do usuário persistido e nunca confia em papel enviado pelo frontend.

Será usada `spring-boot-starter-security` e `spring-security-oauth2-jose`: este último fornece encoder/decoder JWT JOSE do Spring Security sem adicionar servidor OAuth2, client OAuth2, refresh token ou infraestrutura OAuth completa. JWT evita cookies entre `localhost:5173` e `localhost:8080`, `credentials: include`, configuração CORS permissiva e desenho adicional de CSRF. Sessão HTTP foi descartada porque exigiria cookie cross-origin e proteção CSRF para mutações; refresh token foi descartado por ampliar armazenamento e revogação sem necessidade demonstrável.

### 2. Modelo de usuário e persistência

Será criada a entidade `Usuario` com `id`, `nome`, `username`, `email`, `passwordHash`, `role` e `createdAt`. Username e email serão normalizados (trim e lowercase) antes de persistir; o mesmo valor normalizado será usado para apresentação nesta primeira versão. `role` será enum restrito a `USER` e `ADMIN`.

A migration nova, posterior a V6, criará `usuarios` com chave primária, `username` e `email` únicos, `role` com check `USER`/`ADMIN`, `password_hash` não nulo e `created_at` não nulo. As constraints únicas também atuam como índices; não haverá migration para alterar arquivos antigos. O hash será gerado por BCrypt através de `PasswordEncoder`; password nunca será mapeada para resposta.

### 3. Contratos de autenticação e falhas

`POST /auth/register` aceitará somente `nome`, `username`, `email`, `password` e retornará um `UserResponse` não secreto; `role` não fará parte de `RegisterRequest`. `POST /auth/login` aceitará somente `{ "username": "...", "password": "..." }` e retornará `{ accessToken, tokenType: "Bearer", expiresIn, user }`, onde `user` contém apenas os dados mínimos necessários ao frontend. `GET /auth/me` retornará `UserResponse` com Bearer válido. Logout é local no frontend: apagar token e identidade; não haverá endpoint nem blacklist de token nesta versão.

Falha de credencial, token ausente, inválido ou expirado será `401`; identidade válida sem autoridade administrativa será `403`. Os handlers de segurança devolverão o formato de erro público existente, sem stack trace, token ou indicação de qual credencial de login falhou.

### 4. Regras de autorização

`POST /auth/register`, `POST /auth/login` e os preflights necessários serão `permitAll`. Todos os endpoints atuais de Corretoras, Ações, Carteiras, Operações, Posições e Histórico exigirão `authenticated`; isso adiciona apenas a pré-condição de acesso e não muda seus DTOs nem regras de negócio. `GET /admin/users` e `GET /admin/metrics` exigirão `ADMIN`: não autenticado recebe 401, USER recebe 403 e nenhuma listagem de usuários é pública.

O painel administrativo será deliberadamente pequeno: lista de dados administrativos não secretos dos usuários e métricas reais de contagem de usuários, carteiras, ações e operações. A lista nunca incluirá password, passwordHash, token ou outro segredo, e não permitirá alterar role, senha ou usuários.

### 5. Administrador de demonstração

Um bootstrap backend habilitado somente por perfil local acadêmico e flag de configuração explícita criará `adm` com o password de demonstração `123` por BCrypt quando ele não existir. A credencial é explicitamente pública e limitada à demonstração; ela não é segredo de produção, não entra no React, não aparece em logs/respostas e nunca é persistida sem hash. Fora desse perfil/configuração, nenhum administrador fraco será criado automaticamente.

O bootstrap será idempotente: se `adm` já for `ADMIN`, não altera hash ou cria duplicata. Se `adm` existir com outro papel, a inicialização falhará de forma explícita, em vez de elevar uma conta possivelmente criada por outro usuário. O bootstrap ficará desabilitado fora do perfil/configuração local de demonstração.

### 6. Security, CORS e cliente web

`SecurityFilterChain` será stateless, com filtro Bearer anterior à autorização, `PasswordEncoder` BCrypt e handlers explícitos integrados ao formato de erro existente. Token ausente, inválido ou expirado e login inválido retornam 401 seguro; papel insuficiente retorna 403. CORS será centralizado na configuração de segurança para `http://localhost:5173`, métodos atualmente necessários (`GET`, `POST`, `PUT`, `OPTIONS`), cabeçalhos `Content-Type`, `Accept` e `Authorization`, preflight OPTIONS permitido, sem cookies e com `allowCredentials=false`.

O frontend manterá o token somente em `sessionStorage`: permite recuperar a sessão na mesma aba com `/auth/me` após reload e o remove ao fechar a aba. Como qualquer JavaScript da origem pode ler `sessionStorage`, continua existindo risco de XSS; a mitigação é não usar `dangerouslySetInnerHTML`, preservar escaping padrão React, não colocar token em URL/log e usar expiração curta. `localStorage` foi descartado por maior persistência; token apenas em memória foi descartado porque não satisfaz bem a recuperação após reload solicitada.

As telas `/login` e `/cadastro` ficarão fora do layout autenticado. Um contexto/estado de autenticação inicializará a identidade por `/auth/me`, envolverá requisições protegidas com Bearer, limpará sessão ao `401` e protegerá as rotas atuais. `/admin` só renderizará e solicitará dados para `ADMIN`; o shell dark existente será reutilizado, com logout e item administrativo condicional.

## Risks / Trade-offs

- [JWT não pode ser revogado individualmente] → expiração de 60 minutos, logout local e ausência deliberada de refresh token.
- [`JWT_SECRET` ausente, fraco ou exposto interrompe a segurança] → exigir chave Base64 de 256 bits por ambiente, falhar sem fallback, manter `.env` ignorado pelo Git, manter exemplo vazio e nunca registrar o valor.
- [`sessionStorage` é acessível a XSS] → duração por aba, expiração curta, token fora de URL/log e disciplina de renderização React segura.
- [Senha pública `adm`/`123` é insegura fora de demonstração] → bootstrap restrito ao perfil/configuração local, documentação explícita e nenhuma reutilização como credencial real.
- [Todos os recursos atuais deixam de ser anônimos] → manter contratos e apresentar login/redirect controlado no frontend; validar `401` em cada limite de acesso.
- [Migração adiciona schema] → criar somente uma migration nova, validar em PostgreSQL opt-in e nunca reescrever V1–V6.

## Migration Plan

1. Adicionar `spring-boot-starter-security` e `spring-security-oauth2-jose`, configurar `JWT_SECRET` obrigatório e ampliar `.env.example`/launcher local sem registrar valores.
2. Criar a próxima migration Flyway para `usuarios`; não alterar migrations existentes.
3. Implementar persistência, BCrypt, bootstrap local e endpoints de autenticação/admin.
4. Aplicar a cadeia de autorização e CORS, depois adaptar o cliente e rotas do frontend.
5. Validar testes automatizados, build/lint do frontend e os fluxos manuais de USER e ADMIN contra PostgreSQL local.

Rollback: reverter a aplicação para uma versão anterior não deverá executar rollback automático da migration nem remover usuários. Em ambiente de desenvolvimento, a reversão de schema requer uma decisão explícita e procedimento seguro; tokens emitidos pela versão nova deixam de ter efeito quando o backend anterior não reconhece a configuração de segurança.
