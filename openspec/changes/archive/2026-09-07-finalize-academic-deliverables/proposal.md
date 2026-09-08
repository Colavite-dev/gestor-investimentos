## Why

Os entregáveis acadêmicos não representam integralmente o backend consolidado: o README mistura fases antigas com trechos corrompidos por encoding, o diagrama de entidades não inclui o modelo persistido completo e a coleção Postman ainda não cobre Carteiras e Operações. A Fase 7 consolida documentação verificável do sistema atual, sem mudar seu comportamento.

## What Changes

- Reestruturar o README como guia curto de apresentação, configuração Windows/PowerShell, execução, banco, testes, endpoints, erros, decisões e limitações confirmadas.
- Corrigir mojibake somente em documentos textuais revisados, preservando seu significado; o enunciado original do professor será corrigido apenas quanto ao encoding.
- Atualizar o modelo Mermaid com Corretora, Acao, Carteira, Operacao e CotacaoHistorica, atributos persistidos relevantes e cardinalidades reais.
- Reorganizar e ampliar a coleção Postman para os 18 endpoints atuais, com variáveis seguras e requests compatíveis com os DTOs.
- Criar documentação detalhada das cinco integrações externas e alinhar apenas os trechos factualmente desatualizados de arquitetura, decisões e PRD.
- Documentar como pendências acadêmicas, sem alegar aprovação, a identidade `(ticker, mercado)` e a escolha PostgreSQL principal com H2 em testes e sem MySQL.

## Capabilities

### New Capabilities

Nenhuma. Esta change não introduz comportamento de produto.

### Modified Capabilities

Nenhuma. A documentação descreve comportamento já implementado; stable specs não mudam.

## Impact

Afeta exclusivamente README e documentos em `docs/`, incluindo uma nova referência de integrações externas. Não altera Java, testes Java, migrations, endpoints, dependências, Docker ou configurações de runtime.
