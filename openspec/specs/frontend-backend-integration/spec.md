# frontend-backend-integration Specification

## Purpose

Definir um consumo seguro e previsível da API Spring pelo navegador, preservando os contratos existentes e mantendo credenciais de providers exclusivamente no backend.

## Requirements

### Requirement: Cliente autenticado da API

O cliente web SHALL enviar o access token atual exclusivamente no cabeçalho `Authorization: Bearer <token>` para requisições protegidas à API Spring. O token SHALL ser mantido em `sessionStorage` somente durante a sessão da aba; ao recarregar a página, o frontend SHALL consultar `/auth/me` antes de liberar rotas protegidas, e a falha `401` SHALL encerrar a sessão local. O frontend MUST NOT enviar senha, hash, token de provider ou credencial de banco a endpoints não relacionados.

#### Scenario: Requisição protegida usa Bearer token

- **WHEN** um usuário autenticado solicita dados de investimento
- **THEN** o cliente envia o token somente em `Authorization` e não em query string ou URL

#### Scenario: API informa sessão inválida

- **WHEN** uma requisição protegida recebe `401 Unauthorized`
- **THEN** o cliente limpa a autenticação local e encaminha o usuário ao login sem exibir detalhes do token

### Requirement: Erros de autorização do navegador

O cliente SHALL apresentar `401 Unauthorized` como sessão ausente, expirada ou inválida, limpar o estado autenticado e redirecionar para `/login`; SHALL apresentar `403 Forbidden` como acesso sem permissão sem apagar a autenticação. O token MUST NOT ser exibido ou registrado, e a implementação SHALL evitar `dangerouslySetInnerHTML` ou prática equivalente desnecessária. A integração local SHALL permitir o cabeçalho `Authorization` somente para a origem Vite documentada e SHALL continuar sem `allowCredentials` ou cookies de sessão.

#### Scenario: API rejeita acesso administrativo

- **WHEN** uma chamada administrativa recebe `403 Forbidden`
- **THEN** o frontend informa que o usuário não possui permissão e não exibe dados administrativos parciais

### Requirement: Browser API client

The frontend SHALL centralize requests to the Spring API behind a typed client configured by `VITE_API_BASE_URL`, use the existing endpoint paths and DTO-compatible payloads, and SHALL NOT call external providers directly. Any HTTP response received from the Spring API, including controlled 4xx and 5xx responses, SHALL mark the backend as reachable; only a network failure without an HTTP response SHALL mark the backend as unavailable. Provider failures returned by the backend SHALL remain distinguishable from browser-to-backend connectivity failures.

#### Scenario: Frontend loads a portfolio summary

- **WHEN** a portfolio detail page is opened
- **THEN** the client requests the existing `/carteiras/{id}/resumo`, `/posicoes` and `/operacoes` resources through the configured Spring base URL

#### Scenario: Backend returns provider failure

- **WHEN** the catalog request receives a controlled HTTP error caused by an external provider
- **THEN** the frontend keeps the backend marked reachable and presents the provider error without calling the backend offline

#### Scenario: Browser cannot reach backend

- **WHEN** a request fails before receiving an HTTP response from the configured Spring API
- **THEN** the frontend marks the backend unavailable and presents the connectivity error

### Requirement: Backend error presentation

The frontend SHALL map the backend's controlled HTTP statuses 400, 404, 409, 422, 502 and 503 to user-readable messages without exposing raw credentials, provider bodies or sensitive URLs.

#### Scenario: Backend rejects a sale

- **WHEN** an operation request returns 422
- **THEN** the UI identifies it as a business-rule rejection and does not claim that the operation was created

### Requirement: Local browser access

The browser demonstration SHALL work from the configured local Vite origin to the local Spring API. If same-origin serving is not used, the backend SHALL allow only the documented local development origin(s) required by the frontend rather than an unrestricted wildcard.

#### Scenario: Browser calls the local API

- **WHEN** the frontend runs on its local development origin and requests a backend endpoint
- **THEN** the request succeeds or returns the API's controlled response without requiring provider credentials in the browser

### Requirement: Environment and secret boundary

The frontend SHALL expose only a non-secret API base URL through Vite environment configuration and SHALL NOT contain BRAPI_TOKEN, TWELVE_DATA_API_KEY, DB_PASSWORD or other provider/database secrets.

#### Scenario: Frontend build configuration is inspected

- **WHEN** the demonstrable frontend is built or its environment example is reviewed
- **THEN** only browser-safe configuration is present and all external provider authentication remains server-side

### Requirement: Estado financeiro acompanha a identidade da sessão

O frontend SHALL limpar carteiras, operações, posições, resumos e seleções financeiras ao encerrar ou substituir a identidade autenticada e SHALL ignorar respostas assíncronas iniciadas por uma sessão anterior. O frontend MUST NOT enviar `usuarioId` para autorizar requests e MUST NOT ser considerado a barreira de segurança para ownership.

#### Scenario: Logout após carregar Dashboard

- **WHEN** USER_A carrega dados financeiros e encerra a sessão
- **THEN** token, identidade, seleções e dados financeiros em memória deixam de estar disponíveis antes da próxima sessão

#### Scenario: Resposta tardia da conta anterior

- **WHEN** uma requisição de USER_A termina após logout ou após USER_B assumir a sessão
- **THEN** a resposta de USER_A é descartada e não atualiza a interface de USER_B

#### Scenario: Nova identidade carrega seus próprios dados

- **WHEN** USER_B autentica após o logout de USER_A
- **THEN** o frontend solicita novamente `GET /carteiras` com o token atual e não reutiliza carteiras, operações, posições ou resumos de USER_A


### Requirement: Browser usa contratos de busca e resolução protegidos pelo backend
O frontend SHALL chamar `GET /acoes/pesquisar` para sugestões e `POST /acoes/resolver` para a sugestão escolhida, utilizando a base configurada da API Spring. Esses contratos SHALL transportar somente dados públicos de ação; chaves da brapi, chave da Twelve Data, tokens, URLs de provider e credenciais de banco MUST NOT ser enviados, exibidos ou armazenados no frontend.

#### Scenario: Pesquisa remota do navegador
- **WHEN** o autocomplete inicia pesquisa válida
- **THEN** o browser envia somente o termo para a API Spring e recebe sugestões normalizadas sem contato direto com provider externo

#### Scenario: Resolução retorna referência persistida
- **WHEN** o usuário confirma uma sugestão
- **THEN** o browser envia ticker e mercado à API Spring e recebe a ação persistida ou reutilizada com ID para uso em `POST /operacoes`

### Requirement: Erros de busca e resolução são apresentados sem detalhes sensíveis
O cliente de API SHALL preservar a classificação pública da API para `400`, `422`, `502` e `503` durante pesquisa e resolução. A interface SHALL aceitar `200` de pesquisa agregada mesmo quando a API tiver usado apenas resultados de provider saudável, e SHALL comunicar entrada inválida, ativo não elegível, resposta externa inválida ou indisponibilidade sem mostrar stack trace, payload externo bruto ou detalhes de credencial.

#### Scenario: Provider não disponível para autocomplete
- **WHEN** a API Spring responde `503 Service Unavailable` durante busca ou resolução
- **THEN** o frontend informa indisponibilidade temporária e não marca uma ação como selecionada
### Requirement: Cliente integra catálogo e cotação sem quebrar fluxos existentes

O cliente HTTP autenticado SHALL consumir `GET /acoes/catalogo` com parâmetros codificados e SHALL continuar usando pesquisa, resolução e atualização de cotação existentes. Ao selecionar um ativo já persistido, SHALL solicitar sua atualização de cotação; ao resolver um novo ativo, SHALL reutilizar a cotação devolvida pela resolução, evitando uma segunda chamada imediata.

#### Scenario: Seleção de ativo persistido
- **WHEN** o usuário seleciona uma identidade já presente na lista local
- **THEN** o frontend mantém a seleção, solicita atualização e usa a resposta como sugestão de preço

#### Scenario: Seleção de ativo novo
- **WHEN** o usuário seleciona uma identidade ainda não persistida
- **THEN** o frontend resolve o ativo uma vez e usa a cotação da resposta como sugestão

#### Scenario: Resposta obsoleta de seleção
- **WHEN** uma resposta de cotação chega depois que o usuário trocou de ativo ou editou manualmente o preço
- **THEN** ela não sobrescreve a seleção ou o preço atuais
