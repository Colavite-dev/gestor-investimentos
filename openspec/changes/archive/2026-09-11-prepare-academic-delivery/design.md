## Context

Veja a motivação em `proposal.md`. Os documentos registram uma versão anterior à autenticação e ao frontend, enquanto a API real usa JWT Bearer, usuários com roles e carteiras privadas. A coleção Postman também antecede essa proteção.

## Goals / Non-Goals

**Goals:**

- Fazer os artefatos de entrega descreverem os endpoints, relações de entidades, segurança, integrações e execução que já existem.
- Tornar a coleção Postman executável sem credencial embutida, com uma variável de coleção `token` atualizada após login.
- Manter ambiguidades acadêmicas identificáveis, sem apresentá-las como aprovação do professor.

**Non-Goals:**

- Não alterar código, frontend, banco, migrations, contratos HTTP, dependências ou comportamento funcional.
- Não adicionar Swagger, MySQL, endpoints, cache, retry, paginação ou funcionalidades.

## Decisions

- Atualizar a documentação a partir de controllers, configurações, entidades, migrations V1–V8 e adapters atuais, em vez de inferir comportamento do documento anterior. Alternativa descartada: preservar afirmações históricas, pois contradizem a entrega executável.
- Aplicar autenticação Bearer no nível da coleção Postman, herdada pelos requests protegidos; manter register/login sem autenticação e deixar `token` vazio por padrão. O script pós-resposta de login grava somente `accessToken` retornado. Alternativa descartada: token fixo, pois seria inseguro e expiraria.
- Declarar `skip_specs: true` na change: ela não modifica requisito nem comportamento funcional; apenas corrige artefatos de entrega.

## Risks / Trade-offs

- [Documentação divergir novamente do código] → confrontar endpoints, schema e adapters com a implementação antes da revisão final.
- [Coleção Postman armazenar segredo] → usar somente variáveis vazias e script que lê resposta de login.
- [Ambiguidade virar afirmação de conformidade] → registrar ticker/mercado, bancos e execução individual como estado atual pendente de orientação acadêmica.
