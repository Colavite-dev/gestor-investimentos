## ADDED Requirements

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
