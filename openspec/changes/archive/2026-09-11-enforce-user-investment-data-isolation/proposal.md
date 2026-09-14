## Why

As carteiras e operações atuais são globais: qualquer identidade autenticada consegue listar ou consultar por ID os investimentos criados por outra conta. Isso viola a separação básica entre usuários e permite exposição e alteração indireta de patrimônio privado.

## What Changes

- Tornar cada `Carteira` propriedade obrigatória de exatamente um `Usuario`, derivado exclusivamente da identidade autenticada no backend.
- Restringir listagem, consulta direta, operações, posições, resumo e Dashboard ao owner da carteira, retornando `404 Not Found` tanto para recurso inexistente quanto para recurso de outro usuário.
- Manter `Acao`, catálogo, cotação e histórico de cotação como dados globais de mercado compartilháveis.
- Preservar os contratos de request existentes: `CarteiraRequest` não recebe owner e `OperacaoRequest` continua referenciando a carteira, sem `usuarioId`.
- Introduzir migration posterior à V7 para ownership, chave estrangeira, unicidade de nome por usuário e índices de acesso, sem editar V1–V7 nem atribuir registros legados silenciosamente.
- Fazer a migration falhar sem efeitos em qualquer banco que ainda contenha carteiras sem owner; no banco local inventariado, as duas carteiras e suas sete operações foram explicitamente classificadas pelo usuário como dados de teste descartáveis e serão removidas antes da V8, sem apagar usuários, ações, cotações, banco ou volume.
- Limpar ou invalidar estado financeiro no frontend durante troca/logout de identidade e ignorar respostas assíncronas da sessão anterior, sem usar isso como substituto da autorização backend.
- Adicionar testes de segurança com dois usuários que compartilham a mesma `Acao` e mantêm carteiras, operações, posições e valuation independentes.

## Capabilities

### New Capabilities

- `investment-data-isolation`: Define ownership de carteiras, autorização por objeto, não enumeração e separação entre dados globais de mercado e patrimônio privado.

### Modified Capabilities

- `portfolio-management`: Torna criação, unicidade, listagem e consulta de carteiras específicas do usuário autenticado.
- `operation-management`: Restringe criação e consulta de operações ao owner da carteira associada.
- `portfolio-position-calculation`: Restringe posições e resumos às operações de carteiras pertencentes ao usuário autenticado.
- `frontend-backend-integration`: Impede reutilização visual de dados financeiros entre sessões e descarta respostas assíncronas de identidade anterior.

## Impact

- Backend: entidades `Usuario`/`Carteira`, repositories, services e controllers de Carteira e Operação, resolução do ID pelo `SecurityContext` e tratamento uniforme de `404`.
- Banco: nova migration V8 ou posterior; V1–V7 permanecem imutáveis. A unicidade global de nome de carteira passa a ser por `(usuario_id, nome_normalizado)`.
- API: caminhos e payloads atuais são preservados, mas resultados passam a ser limitados ao usuário autenticado e IDs de terceiros passam a responder `404`.
- Frontend: ciclo de vida do estado financeiro ao trocar ou encerrar sessão; sem envio de `usuarioId`.
- Testes: unitários, integração com dois usuários, segurança por objeto, schema Flyway/H2 e validação PostgreSQL opt-in por causa da nova migration.
