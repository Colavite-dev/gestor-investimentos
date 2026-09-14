## Why

O bootstrap administrativo de demonstração atualmente versiona uma credencial fixa, o que é inadequado antes da publicação do repositório. Dois assets do frontend também não são referenciados pela aplicação e não devem integrar a entrega.

## What Changes

- Substituir as credenciais fixas do administrador demo por configuração externa de ambiente, mantendo o bootstrap explicitamente opt-in e desabilitado por padrão.
- Impedir o bootstrap quando as credenciais externas obrigatórias estiverem ausentes ou inválidas, sem criar fallback administrativo e sem registrar senha.
- Manter o hash BCrypt e a inicialização idempotente quando a configuração local válida estiver presente.
- Atualizar os exemplos de ambiente, o script local, os testes diretamente afetados e a coleção Postman para não versionar credenciais demonstrativas fixas.
- Remover `frontend/public/icons.svg` e `frontend/src/assets/hero.png` após confirmar que não possuem referências.
- Atualizar a especificação normativa de autenticação; preservar archives históricos que não contenham credenciais funcionais após a remoção do runtime.

## Capabilities

### New Capabilities

- Nenhuma.

### Modified Capabilities

- `authentication-user-access`: substituir o bootstrap administrativo com credencial versionada por um bootstrap local opt-in com credenciais fornecidas exclusivamente por configuração externa.

## Impact

- Backend: bootstrap de admin demo e configuração Spring.
- Ambiente local: `.env.example` e `scripts/start-local.ps1`.
- Testes de configuração do bootstrap.
- Documentação executável: coleção Postman.
- Frontend: remoção de dois assets não referenciados.
- OpenSpec: delta da capability `authentication-user-access`.
