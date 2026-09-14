## Why

O Adapt Invest ainda expõe todas as funcionalidades a qualquer cliente e não possui identidade de usuário, controle de acesso ou uma demonstração segura de login. Esta change introduz uma camada de autenticação e autorização demonstrável, preservando os contratos de negócio existentes e mantendo credenciais sob responsabilidade exclusiva do backend.

## What Changes

- Adicionar cadastro público de usuários `USER`, autenticação por credencial e consulta da identidade autenticada.
- Persistir usuários no PostgreSQL com unicidade de username e email, normalização de identificadores e senha armazenada exclusivamente por hash seguro.
- Proteger os recursos atuais da API e criar recursos administrativos mínimos exclusivos de `ADMIN`.
- Adotar token JWT stateless assinado com `JWT_SECRET` obrigatório por variável de ambiente, via `Authorization: Bearer`, com expiração finita, respostas controladas para `401` e `403`, sem refresh token nesta primeira versão.
- Criar bootstrap backend idempotente de um administrador **somente de demonstração acadêmica/local**, `adm` / `123`, condicionado a perfil/configuração local; a senha não será enviada ao frontend, registrada em log ou persistida em texto puro.
- Adicionar login, cadastro, recuperação de sessão, logout local, rotas protegidas e área `/admin` ao frontend existente, sem redesenhar o shell dark do Adapt Invest.
- Atualizar `.env.example` somente com placeholders seguros para `JWT_SECRET`, atualizar o launcher local para encaminhar o segredo sem imprimi-lo e falhar claramente quando ele faltar, e atualizar CORS para aceitar `Authorization` sem habilitar cookies nem `allowCredentials`.

## Capabilities

### New Capabilities

- `authentication-user-access`: identidade de usuário, autenticação JWT, papéis `USER`/`ADMIN`, proteção dos recursos da API e administração mínima.

### Modified Capabilities

- `frontend-application`: o shell web passa a oferecer fluxos de login/cadastro, guarda de rotas e navegação condicional por papel.
- `frontend-backend-integration`: o cliente do navegador passa a propagar Bearer token e a tratar autenticação/autorização inválida sem expor segredos.

## Impact

- Backend: Spring Security, entidade/repositório/serviços/DTOs de usuário e autenticação, filtro JWT, configuração de segurança/CORS, bootstrap de demonstração e tratamento HTTP de acesso.
- Banco: uma nova migration Flyway para `usuarios`, sem alteração das migrations já aplicadas.
- API: novos endpoints `/auth` e `/admin`; endpoints de negócio existentes preservam payloads e regras, mas passam a requerer autenticação.
- Frontend: cliente HTTP, estado de autenticação, telas e rotas de acesso, menu administrativo e visualização administrativa pequena.
- Dependências: Spring Security e o suporte JOSE/JWT do Spring Security, mantidos no backend; `JWT_SECRET` permanecerá apenas no ambiente local ignorado pelo Git e nenhuma credencial de banco ou provider será exposta ao browser.
