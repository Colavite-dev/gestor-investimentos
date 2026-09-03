## 1. Persistencia e dominio

- [x] 1.1 Criar migration Flyway para cotacoes_historicas, com chave estrangeira, precisao monetaria, timestamp e indice de consulta; verificar aplicacao em banco limpo.
- [x] 1.2 Criar entidade, repository e DTO de historico sem expor entidade JPA; verificar mapeamento e ordenacao deterministica.

## 2. Integracao com cotacoes

- [x] 2.1 Ajustar cadastro de acoes para registrar o snapshot inicial na mesma transacao; verificar que respostas externas invalidas nao criam historico.
- [x] 2.2 Ajustar atualizacao de cotacao para acrescentar snapshot sem alterar campos cadastrais; verificar preservacao e atomicidade.
- [x] 2.3 Implementar consulta de historico por acao com filtros de/ate, validacao de intervalo e ausencia de chamadas externas; verificar acao inexistente e colecao vazia.

## 3. API, documentacao e qualidade

- [x] 3.1 Expor GET /acoes/{id}/historico-cotacoes com contrato JSON centralizado e erros 400/404; verificar ordenacao e filtros via testes de controller.
- [x] 3.2 Atualizar README e colecao de API com o endpoint, formato ISO-8601 e limitacoes de somente leitura; verificar ausencia de segredos.
- [x] 3.3 Adicionar testes unitarios e de integracao cobrindo cadastro, atualizacao, falhas, duplicidade, filtros, persistencia e nao regressao dos providers; verificar suite sem internet.
- [x] 3.4 Executar mvn test, mvn verify, validacao OpenSpec strict e validacao Flyway/PostgreSQL consolidada; reconciliar esta checklist com evidencias reais.

Evidencias: os testes focados passaram (3 testes) e `mvn verify` passou com 112 testes; Flyway aplicou V1--V5 no H2 de testes; OpenSpec strict e stable specs foram aprovados. A aplicacao iniciou com PostgreSQL 17.11 em `localhost:5433`, conectou via Hikari e aplicou V3--V5, encerrando em v5; nenhum segredo foi registrado.
