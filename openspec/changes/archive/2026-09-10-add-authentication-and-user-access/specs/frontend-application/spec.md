## ADDED Requirements

### Requirement: Fluxos de acesso e rotas protegidas
O frontend SHALL oferecer `/login` e `/cadastro` fora do shell autenticado, manter o tema dark e a identidade visual Adapt Invest, e proteger Dashboard e todas as rotas de investimento contra acesso sem identidade válida. Ao recuperar uma identidade válida, o frontend SHALL renderizar o shell existente; ao receber `401`, SHALL limpar a sessão local e redirecionar para `/login` de forma controlada.

#### Scenario: Visitante acessa dashboard
- **WHEN** um visitante sem sessão válida abre uma rota protegida, inclusive `/`
- **THEN** o frontend o redireciona para `/login` sem renderizar dados de investimento

#### Scenario: Usuário efetua login
- **WHEN** um usuário conclui login válido
- **THEN** o frontend recupera a identidade autenticada e o redireciona para o dashboard ou rota protegida originalmente solicitada

### Requirement: Logout e acesso administrativo condicional
O shell autenticado SHALL oferecer logout que remova o estado de autenticação local e retorne o usuário à tela de login. A rota e a navegação `/admin` SHALL aparecer e ser acessíveis somente quando a identidade atual tiver papel `ADMIN`; um `USER` que tente abrir `/admin` SHALL receber uma tela de acesso negado ou redirecionamento controlado sem carregar dados administrativos.

#### Scenario: Usuário encerra sessão
- **WHEN** um usuário autenticado seleciona logout
- **THEN** o frontend remove o token e a identidade local e redireciona para `/login`

#### Scenario: USER tenta abrir área administrativa
- **WHEN** uma identidade `USER` navega diretamente para `/admin`
- **THEN** o frontend não solicita dados administrativos e apresenta acesso negado ou redirecionamento controlado

#### Scenario: ADMIN abre área administrativa
- **WHEN** uma identidade `ADMIN` abre `/admin`
- **THEN** o frontend apresenta a listagem pública de usuários e métricas simples retornadas pela API
