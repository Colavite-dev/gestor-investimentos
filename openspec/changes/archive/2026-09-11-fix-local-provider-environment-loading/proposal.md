## Why

O script local já lê o `.env` e injeta temporariamente as credenciais de banco e autenticação no processo do Spring Boot, mas deixa de fora as credenciais de brapi e Twelve Data. Isso faz o ambiente iniciado pelo fluxo oficial falhar em integrações autenticadas apesar de as chaves existirem no `.env` local.

## What Changes

- Incluir `BRAPI_TOKEN` e `TWELVE_DATA_API_KEY` na lista permitida já usada por `scripts/start-local.ps1` para leitura, injeção temporária e restauração de variáveis de ambiente.
- Preservar o parser único do `.env`, a inicialização do PostgreSQL, o início do Spring Boot e a restauração do ambiente no encerramento.
- Validar o fluxo local com uma resolução BR autenticada, uma pesquisa americana e o catálogo US, sem modificar adapters, contratos REST, timeout, cache ou frontend.

## Capabilities

### New Capabilities

Nenhuma. A change corrige somente a inicialização local; não introduz comportamento de produto.

### Modified Capabilities

Nenhuma. As stable specs de produto e os contratos externos permanecem inalterados.

## Impact

Afeta somente `scripts/start-local.ps1`. Não altera código Java, configuração dos providers, DTOs, endpoints, banco, migrations, cache, timeout, catálogo ou frontend. `.env.example` já documenta os nomes das duas variáveis e não requer alteração.
