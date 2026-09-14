## Purpose

Definir identidade persistida, autenticação e controle de acesso para que o Adapt Invest proteja seus recursos e demonstre papéis de usuário de forma segura.

## ADDED Requirements

### Requirement: Cadastro público de usuário comum
O sistema SHALL expor `POST /auth/register` para receber somente `nome`, `username`, `email` e `password`, criar exclusivamente um usuário com papel `USER` e devolver os dados públicos do usuário criado sem senha ou hash. `username` e `email` SHALL ser obrigatórios, normalizados por remoção de espaços nas extremidades e comparação sem diferença entre maiúsculas/minúsculas, e únicos; o email SHALL ser sintaticamente válido e a senha SHALL ser obrigatória e não vazia. O cliente MUST NOT selecionar nem elevar o papel no cadastro.

#### Scenario: Cadastro público cria USER
- **WHEN** um visitante envia nome, username, email e senha válidos e ainda não utilizados para `POST /auth/register`
- **THEN** a API cria um usuário `USER`, retorna os seus dados públicos e não retorna password ou passwordHash

#### Scenario: Identificador já utilizado
- **WHEN** um visitante tenta cadastrar username ou email que, após normalização, já pertence a um usuário
- **THEN** a API rejeita o cadastro com `409 Conflict` e sem expor dados do usuário existente

#### Scenario: Cliente tenta definir papel administrativo
- **WHEN** um visitante envia um campo de papel, incluindo `ADMIN`, no payload público de cadastro
- **THEN** a API rejeita o payload com `400 Bad Request` e não cria nem eleva usuário algum

### Requirement: Autenticação Bearer com identidade consultável
O sistema SHALL expor `POST /auth/login` para autenticar exclusivamente `username` e `password` e retornar um access token Bearer de duração finita, seu prazo de expiração e os dados mínimos públicos do usuário autenticado. O sistema SHALL expor `GET /auth/me` para retornar a identidade pública associada a um Bearer token válido. Credenciais inválidas, token ausente, expirado, malformado ou inválido SHALL produzir `401 Unauthorized` com mensagem segura e sem distinguir username inexistente de senha incorreta.

#### Scenario: Login válido
- **WHEN** um usuário cadastrado envia credenciais corretas para `POST /auth/login`
- **THEN** a API retorna um Bearer access token temporário e os dados públicos daquele usuário, sem senha ou hash

#### Scenario: Login inválido
- **WHEN** um visitante envia username inexistente ou password incorreto para `POST /auth/login`
- **THEN** a API retorna `401 Unauthorized` sem informar qual credencial falhou

#### Scenario: Token expirado não restaura sessão
- **WHEN** um cliente chama `GET /auth/me` com token expirado ou inválido
- **THEN** a API retorna `401 Unauthorized` e não retorna identidade de usuário

### Requirement: Proteção de recursos e papéis
As rotas públicas SHALL ser somente `POST /auth/register`, `POST /auth/login` e preflight `OPTIONS` necessário à origem local documentada. Os recursos de negócio existentes de Corretoras, Ações, Carteiras, Operações, Posições e Histórico SHALL exigir usuário autenticado sem mudar seus payloads ou regras de negócio. Recursos sob `/admin/**` SHALL exigir papel `ADMIN`; uma identidade autenticada sem esse papel SHALL receber `403 Forbidden`.

#### Scenario: Recurso de investimento sem autenticação
- **WHEN** um cliente sem Bearer token válido chama um endpoint existente de investimentos
- **THEN** a API retorna `401 Unauthorized` antes de executar a regra de negócio

#### Scenario: USER não acessa recurso administrativo
- **WHEN** um usuário com papel `USER` chama um endpoint sob `/admin/**`
- **THEN** a API retorna `403 Forbidden` sem revelar dados administrativos

#### Scenario: ADMIN acessa recurso administrativo
- **WHEN** um usuário com papel `ADMIN` chama um endpoint administrativo com Bearer token válido
- **THEN** a API autoriza a operação e retorna somente os dados públicos previstos pelo recurso

### Requirement: Administração mínima demonstrável
O sistema SHALL expor uma área administrativa mínima, exclusivamente para `ADMIN`, com `GET /admin/users` para listar dados administrativos não secretos de usuários e `GET /admin/metrics` para retornar contagens simples baseadas em dados reais do sistema. Nenhuma listagem de usuários será pública. Dados administrativos de usuário SHALL incluir somente identificador, nome, username, email, papel e data de criação, e MUST NOT incluir password, passwordHash, token ou qualquer segredo.

#### Scenario: ADMIN lista usuários sem segredos
- **WHEN** um ADMIN autenticado solicita `GET /admin/users`
- **THEN** a API retorna usuários com seus dados administrativos não secretos e nunca inclui password, passwordHash, token ou qualquer segredo

#### Scenario: Visitante tenta listar usuários
- **WHEN** um cliente não autenticado solicita `GET /admin/users` ou `GET /admin/metrics`
- **THEN** a API retorna `401 Unauthorized` e não retorna dados administrativos

#### Scenario: ADMIN consulta métricas reais
- **WHEN** um ADMIN autenticado solicita `GET /admin/metrics`
- **THEN** a API retorna métricas simples calculadas a partir dos registros persistidos, sem dados fabricados nem segredos

### Requirement: Administrador de demonstração controlado e idempotente
Somente quando o perfil/configuração acadêmica local de bootstrap estiver habilitado, o sistema SHALL poder inicializar de maneira idempotente um administrador de demonstração com username `adm` e password `123`. O bootstrap SHALL criar ou reutilizar somente o usuário `adm` com papel `ADMIN`, armazenar a senha exclusivamente por hash seguro, não registrar nem devolver a senha e permanecer explicitamente identificado como recurso de demonstração acadêmica, não como credencial de produção.

#### Scenario: Primeiro bootstrap local do administrador
- **WHEN** o bootstrap de demonstração está habilitado em ambiente acadêmico local e o usuário `adm` não existe
- **THEN** o sistema cria um ADMIN `adm` cuja senha não está armazenada em texto puro

#### Scenario: Reinicialização não duplica administrador
- **WHEN** o bootstrap de demonstração é executado novamente e o usuário `adm` já existe
- **THEN** o sistema não cria um segundo administrador nem registra ou altera a senha em texto puro

#### Scenario: Bootstrap não é habilitado fora do ambiente local previsto
- **WHEN** o perfil/configuração acadêmica local de bootstrap não está habilitado
- **THEN** o sistema não cria automaticamente o usuário de demonstração

### Requirement: Limite de segredo de credenciais e assinatura JWT
Senhas SHALL ser persistidas somente como hash adaptativo seguro; plaintext password SHALL ser usado somente durante cadastro ou login e descartado em seguida. Respostas HTTP, logs de aplicação, mensagens de erro, métricas e código do frontend MUST NOT expor password, passwordHash, access token ou `JWT_SECRET`. O token SHALL ser assinado no backend com `JWT_SECRET` obrigatório, fornecido exclusivamente por variável de ambiente, aceito somente no cabeçalho `Authorization: Bearer` e não SHALL ser incluído em URL. A inicialização SHALL falhar com mensagem clara e sem revelar valor quando a configuração JWT obrigatória estiver ausente ou inválida.

#### Scenario: Resposta é inspecionada
- **WHEN** um cliente recebe resposta de cadastro, login, identidade ou administração
- **THEN** nenhum campo de senha ou hash é retornado e o token, quando aplicável, aparece somente na resposta de login

#### Scenario: Configuração JWT ausente
- **WHEN** o backend inicia sem `JWT_SECRET` válido
- **THEN** a inicialização falha de forma clara sem gerar, imprimir ou usar segredo de substituição
