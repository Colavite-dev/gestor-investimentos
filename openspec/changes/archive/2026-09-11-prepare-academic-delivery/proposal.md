## Why

Os artefatos acadêmicos existem, mas parte deles descreve um estado anterior ao sistema atual. A entrega precisa documentar corretamente autenticação, usuários, isolamento de carteiras, frontend e endpoints para que README, diagrama e coleção Postman sejam utilizáveis na apresentação.

## What Changes

- Atualizar README, PRD, arquitetura, modelo de entidades, decisões e documentação de integrações para refletir apenas o comportamento já implementado.
- Atualizar a coleção Postman com os endpoints existentes de autenticação, administração e descoberta de ativos, configurando JWT Bearer por variável sem incluir credenciais reais.
- Registrar de forma neutra as ambiguidades acadêmicas já identificadas, sem alterar comportamento, contratos, banco ou migrations.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

Nenhuma. Esta é uma atualização exclusivamente documental; não altera comportamento especificado.

## Impact

Afeta somente os documentos de entrega e `docs/api/gestor-investimento.postman_collection.json`. Não afeta código Java, frontend, schema, migrations, contratos HTTP ou dependências.
