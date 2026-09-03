## 1. Abstração e adapters de cotação

- [x] 1.1 Criar o modelo interno de dados de cotação e evoluir `StockDataProvider` com uma operação específica de consulta direta de cotação, preservando a operação atual de cadastro.
- [x] 1.2 Implementar a consulta direta de cotação no adapter brapi, com validação de ticker, BRL, preço e timestamp e conversão dos erros externos para as exceções internas existentes.
- [x] 1.3 Implementar a consulta direta de cotação no adapter Twelve Data por `/quote`, sem `symbol_search`, com validação de ticker, USD, preço e timestamp e conversão de timeout, conexão, `429`, `5xx`, erro estruturado e chave ausente.

## 2. Atualização de cotação da ação

- [x] 2.1 Implementar a atualização restrita de `cotacaoAtual` e `dataHoraCotacao` na entidade e a persistência correspondente, preservando os demais campos.
- [x] 2.2 Implementar o fluxo de serviço para localizar a ação, selecionar o provider pelo mercado persistido, consultar uma vez o ticker canônico, validar compatibilidade e gravar em transação curta após a chamada externa.
- [x] 2.3 Expor `PUT /acoes/{id}/atualizar-cotacao`, sem body, retornando `200` com o DTO atualizado e `404` para ID inexistente.
- [x] 2.4 Integrar os erros de ticker não consultável, conteúdo incompatível e indisponibilidade externa ao tratamento HTTP `422`, `502` e `503`, sem vazar detalhes dos providers.

## 3. Testes e documentação

- [x] 3.1 Criar testes unitários dos adapters para consulta direta de cotação e seus cenários de erro, sem acesso à internet.
- [x] 3.2 Criar testes de serviço e controller para atualização BR e EUA, seleção exclusiva do provider correto, preservação dos campos cadastrais, persistência de cotação/timestamp e contratos `404`, `422`, `502` e `503`.
- [x] 3.3 Criar e executar uma vez um teste real opt-in separado para PETR4 e AAPL, comprovando os dois caminhos diretos de quote sem discovery, se as credenciais e fontes estiverem disponíveis. Evidência: `StockQuoteRealIT` executado com 2 testes, 0 falhas e 0 erros.
- [x] 3.4 Atualizar README ou documentação técnica com o endpoint, exemplo de uso e erros principais, sem segredos.

## 4. Validação final

- [x] 4.1 Executar os testes automatizados relevantes e `mvn verify`; confirmar que a suíte normal não depende da internet. Evidência: 99 testes, 0 falhas, 0 erros, `mvn verify` gerou o JAR executável.
- [x] 4.2 Executar `openspec validate implement-stock-quote-update --strict` e reconciliar esta lista com evidências reais, sem criar migration nem repetir validação PostgreSQL/Docker se o schema permanecer inalterado. Evidência: validação strict válida.
