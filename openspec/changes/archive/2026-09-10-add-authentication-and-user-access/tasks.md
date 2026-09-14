## 1. Dependências e persistência

- [x] 1.1 Adicionar `spring-boot-starter-security` e `spring-security-oauth2-jose` compatíveis com Java 17/Spring Boot, sem infraestrutura OAuth2 completa ou credenciais versionadas, e verificar resolução Maven.
- [x] 1.2 Criar a próxima migration Flyway para a tabela `usuarios`, constraints únicas, papel restrito, hash e timestamp, sem alterar V1–V6, e verificar a migration contra PostgreSQL opt-in.
- [x] 1.3 Implementar `Usuario`, `Role`, repositório e DTOs públicos com normalização de username/email, e verificar testes de unicidade e de ausência de campos de segredo nas respostas.

## 2. Autenticação e autorização do backend

- [x] 2.1 Configurar BCrypt `PasswordEncoder`, serviço de cadastro público USER e rejeição de payload que tente enviar role, verificando cadastro válido, role USER, hash diferente da senha e tentativa de ADMIN rejeitada.
- [x] 2.2 Implementar configuração obrigatória de `JWT_SECRET` Base64 de 256 bits via ambiente, atualizar `.env.example` somente com placeholder e `scripts/start-local.ps1` para encaminhá-lo sem imprimir valor, verificando falha segura para segredo ausente/inválido.
- [x] 2.3 Implementar emissão e validação de JWT Bearer HS256 com expiração de 60 minutos e claims `sub`, username, role, iat e exp, verificando assinatura válida, token expirado, malformado, adulterado e ausência de token.
- [x] 2.4 Implementar `POST /auth/register`, `POST /auth/login` e `GET /auth/me` com DTOs seguros, login exclusivo por username/password e sem campo role no cadastro, verificando 401 genérico e que cliente não define papel.
- [x] 2.5 Criar bootstrap local idempotente do ADMIN acadêmico `adm`/`123`, condicionado a perfil/flag local, com BCrypt e falha segura para `adm` pré-existente sem papel ADMIN, verificando criação inicial, reinicialização sem duplicidade, desativação fora da configuração prevista e ausência de senha em logs/respostas.
- [x] 2.6 Configurar `SecurityFilterChain` stateless, filtro Bearer e handlers de 401/403 compatíveis com o formato de erro existente, verificando que recursos atuais retornam 401 sem token e preservam os contratos quando autenticados.
- [x] 2.7 Implementar `GET /admin/users` e `GET /admin/metrics` somente para ADMIN, verificando não autenticado recebe 401, USER recebe 403, ADMIN recebe sucesso e hash/password/token nunca aparecem.
- [x] 2.8 Centralizar CORS na configuração de segurança com origem Vite local, métodos atuais, `Content-Type`, `Accept`, `Authorization`, OPTIONS e sem credenciais/cookies, verificando preflight e chamadas Bearer autorizadas.

## 3. Frontend de autenticação e administração

- [x] 3.1 Adicionar tipos e métodos do cliente para register, login, me e recursos administrativos, com injeção de Bearer token e tratamento centralizado de 401/403, verificando que o token não é enviado por URL.
- [x] 3.2 Criar estado/contexto de autenticação com token em `sessionStorage`, recuperação por `/auth/me`, limpeza em 401 e logout local, verificando reload na mesma aba e expiração de sessão.
- [x] 3.3 Criar telas `/login` e `/cadastro` no tema Adapt Invest com feedback controlado, verificando validação, erro de credenciais e redirecionamento após sucesso.
- [x] 3.4 Proteger as rotas existentes e manter o shell dark autenticado, verificando visitante redirecionado ao login e usuário autenticado retornando à rota pretendida.
- [x] 3.5 Adicionar logout e navegação/guarda condicional de `/admin`, verificando que USER não requisita dados administrativos e ADMIN visualiza usuários públicos e métricas reais.

## 4. Verificação integrada e documentação

- [x] 4.1 Adicionar testes backend para cadastro USER, username/email duplicados, hash BCrypt, login, assinatura JWT, JWT ausente/inválido/expirado, segredo JWT ausente, 401, 403, ADMIN, payload role, respostas sem segredo e bootstrap idempotente/desabilitado, e executar os testes focados.
- [x] 4.2 Executar a suíte Maven completa uma vez ao final e verificar que testes normais permanecem independentes de Docker e internet.
- [x] 4.3 Executar build e lint do frontend e corrigir somente falhas diretamente relacionadas à change.
- [x] 4.4 Validar manualmente login USER → dashboard → logout e `adm` → `/admin` usando PostgreSQL local, sem imprimir senhas/tokens nem recriar volumes.
- [x] 4.5 Atualizar README e documentação de demonstração para registrar rotas, credencial acadêmica, limites de segurança e fluxo local, verificando que não inclui segredo de banco/provider.
- [x] 4.6 Validar a change OpenSpec em modo strict e comparar implementação, testes e documentação contra todas as delta specs antes de solicitar aprovação para SYNC/ARCHIVE.
