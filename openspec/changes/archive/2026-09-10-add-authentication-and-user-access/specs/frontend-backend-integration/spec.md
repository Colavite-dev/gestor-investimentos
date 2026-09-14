## ADDED Requirements

### Requirement: Cliente autenticado da API
O cliente web SHALL enviar o access token atual exclusivamente no cabeçalho `Authorization: Bearer <token>` para requisições protegidas à API Spring. O token SHALL ser mantido em `sessionStorage` somente durante a sessão da aba; ao recarregar a página, o frontend SHALL consultar `/auth/me` antes de liberar rotas protegidas, e a falha `401` SHALL encerrar a sessão local. O frontend MUST NOT enviar senha, hash, token de provider ou credencial de banco a endpoints não relacionados.

#### Scenario: Requisição protegida usa Bearer token
- **WHEN** um usuário autenticado solicita dados de investimento
- **THEN** o cliente envia o token somente em `Authorization` e não em query string ou URL

#### Scenario: API informa sessão inválida
- **WHEN** uma requisição protegida recebe `401 Unauthorized`
- **THEN** o cliente limpa a autenticação local e encaminha o usuário ao login sem exibir detalhes do token

### Requirement: Erros de autorização do navegador
O cliente SHALL apresentar `401 Unauthorized` como sessão ausente, expirada ou inválida, limpar o estado autenticado e redirecionar para `/login`; SHALL apresentar `403 Forbidden` como acesso sem permissão sem necessariamente apagar a autenticação. O token MUST NOT ser exibido ou registrado, e a implementação SHALL evitar `dangerouslySetInnerHTML` ou prática equivalente desnecessária. A integração local SHALL permitir o cabeçalho `Authorization` somente para a origem Vite documentada e SHALL continuar sem `allowCredentials` ou cookies de sessão.

#### Scenario: API rejeita acesso administrativo
- **WHEN** uma chamada administrativa recebe `403 Forbidden`
- **THEN** o frontend informa que o usuário não possui permissão e não exibe dados administrativos parciais
