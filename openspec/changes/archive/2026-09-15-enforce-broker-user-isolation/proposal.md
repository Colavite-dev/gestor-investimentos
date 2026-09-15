## Why

Corretoras são autenticadas como recursos de negócio, mas hoje são persistidas e consultadas globalmente. Isso permite que uma conta visualize os registros cadastrados por outra, violando o isolamento de dados esperado pelo Adapt Invest e criando um problema de segurança e de comportamento funcional.

## What Changes

- Associar cada corretora ao usuário autenticado que a cadastra, sem aceitar identidade de proprietário no payload.
- Restringir listagem e consultas de corretoras ao proprietário, retornando `404 Not Found` para recursos inexistentes ou pertencentes a outra conta.
- Trocar a unicidade global de CNPJ pela identidade composta `(usuario_id, cnpj)`, permitindo que contas diferentes cadastrem a mesma instituição e preservando `409 Conflict` para duplicidade na mesma conta e em corrida concorrente do mesmo usuário.
- Preservar o contrato do cliente, a validação BrasilAPI, ViaCEP e CVM, inclusive a aceitação do status CVM `EM FUNCIONAMENTO NORMAL`.
- Evoluir posteriormente o schema por uma nova V9, sem alterar V1–V8 e sem atribuir, apagar ou escolher proprietário para corretoras legadas de forma silenciosa.

## Capabilities

### New Capabilities

- Nenhuma.

### Modified Capabilities

- `broker-management`: ownership, consultas e unicidade de corretoras passam a ser definidos por usuário autenticado.
- `investment-data-isolation`: amplia a proteção de recursos privados para corretoras e define os limites seguros da evolução de dados legados.

## Impact

- Backend: entidade, persistência, repositório, serviço e controller de corretoras; nova migration V9 em etapa posterior; testes de unidade, controller, integração, concorrência e migration.
- API: os endpoints existentes mantêm URLs e payloads, mas suas respostas passam a ser limitadas ao proprietário autenticado.
- Frontend: permanece compatível e não envia `usuarioId`.
- Dados existentes: a estratégia para corretoras legadas será decidida explicitamente no PLAN antes da V9; a migration não pode atribuir ou apagar dados implicitamente.
