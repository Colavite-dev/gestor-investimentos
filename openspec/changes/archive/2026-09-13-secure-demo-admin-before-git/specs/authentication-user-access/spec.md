## MODIFIED Requirements

### Requirement: Administrador de demonstração controlado e idempotente

O sistema SHALL inicializar um administrador de demonstração somente quando `APP_DEMO_ADMIN_ENABLED=true` estiver explicitamente configurada para desenvolvimento ou demonstração local. O bootstrap SHALL obter `APP_DEMO_ADMIN_USERNAME` e `APP_DEMO_ADMIN_PASSWORD` exclusivamente de configuração externa; nenhuma senha administrativa fixa, valor de fallback secreto ou credencial utilizável SHALL ser versionada em código, exemplos de ambiente, documentação executável ou especificação normativa. Quando habilitado com credenciais externas presentes e válidas, o bootstrap SHALL criar ou reutilizar de modo idempotente somente o usuário configurado com papel `ADMIN`, armazenar a senha com BCrypt e não registrar nem devolver a senha. Quando a flag estiver desabilitada, ou username/password estiverem ausentes ou inválidos, o bootstrap SHALL não criar administrador algum e SHALL concluir de modo seguro sem expor qualquer valor de credencial.

#### Scenario: Primeiro bootstrap local do administrador

- **WHEN** o bootstrap de demonstração está explicitamente habilitado em ambiente local e username/password externos válidos estão configurados para um usuário ainda inexistente
- **THEN** o sistema cria um usuário `ADMIN` com o username configurado e senha persistida somente como hash BCrypt

#### Scenario: Reinicialização não duplica administrador

- **WHEN** o bootstrap de demonstração é executado novamente para o username configurado e o usuário já existe como `ADMIN`
- **THEN** o sistema não cria um segundo administrador nem sobrescreve sua senha

#### Scenario: Bootstrap não é habilitado fora do ambiente local previsto

- **WHEN** `APP_DEMO_ADMIN_ENABLED` não está explicitamente habilitada
- **THEN** o sistema não cria automaticamente usuário administrativo algum

#### Scenario: Credenciais externas ausentes ou inválidas

- **WHEN** `APP_DEMO_ADMIN_ENABLED=true` mas username ou password externos estão ausentes, vazios ou inválidos
- **THEN** o sistema não cria administrador, não usa fallback e não registra a senha
