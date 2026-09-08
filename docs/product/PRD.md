# PRD — Gestor de Investimentos

## Visão

API REST em Java/Spring Boot para gestão de corretoras, ações e carteiras de investimento com dados de fontes externas. O núcleo acadêmico obrigatório está implementado; Carteiras, Operações, posições e histórico são evoluções já entregues.

## Núcleo acadêmico implementado

- Cadastrar, listar e buscar Corretoras por CNPJ ou ID.
- Validar CNPJ, consultar dados empresariais, validar CEP/endereço e elegibilidade perante a CVM.
- Cadastrar, listar e buscar Ações por ticker/mercado ou ID.
- Consultar e atualizar cotações de ações BR e EUA por providers apropriados.
- Impedir duplicidade de CNPJ e da identidade lógica da Ação.

## Evoluções implementadas

- Carteiras nomeadas, Operações de compra/venda, posições, preço médio e resumo por moeda.
- Histórico de cotações, integridade de venda sem saldo, precisão financeira e rollback transacional.

## Integrações e qualidade

BrasilAPI, ViaCEP, CVM, brapi e Twelve Data são isoladas por providers/adapters. O backend usa PostgreSQL, Flyway, H2 nos testes, validação de entrada e tratamento centralizado de erros. Docker Compose fornece o PostgreSQL local.

## Fora do escopo atual

Não há frontend, autenticação, múltiplos usuários, importação de notas, dividendos, imposto de renda, recomendações, execução real de ordens, Swagger/OpenAPI, paginação, dashboard, retry automático ou cache genérico.

## Pendências acadêmicas

O sistema usa identidade `(ticker, mercado)` e PostgreSQL em runtime com H2 em testes. As divergências/ambiguidades literais do enunciado sobre ticker e H2/MySQL/PostgreSQL permanecem pendentes de confirmação acadêmica.
