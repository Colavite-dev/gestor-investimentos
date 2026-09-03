## 1. Dependências e fronteiras de persistência

- [x] 1.1 Remover a construção alternativa de `AcaoService` que permite ausência de `CotacaoHistoricaService` e verificar por teste que cadastro com cotação válida sempre solicita o registro histórico.
- [x] 1.2 Delimitar em componente de persistência separado as transações curtas de cadastro e atualização, mantendo consulta/validação do provider fora delas; verificar por testes de serviço a delegação após dados externos válidos.

## 2. Consistência de cotação e histórico

- [x] 2.1 Ajustar o cadastro para persistir ação e observação histórica inicial obrigatoriamente na mesma transação e verificar com teste de integração que valor e timestamp são idênticos aos dados do provider.
- [x] 2.2 Preservar ou ajustar a atualização para persistir cotação atual e nova observação na mesma transação curta e verificar com teste de integração a coerência entre ação atualizada e histórico criado.
- [x] 2.3 Adicionar cenários de falha de gravação histórica em contexto Spring, com repositório real para observar o estado, e verificar rollback completo no cadastro e na atualização sem ação/cotação/histórico parcial.

## 3. Consulta de histórico

- [x] 3.1 Ampliar `CotacaoHistoricaControllerTest` para histórico vazio, ausência de filtros, somente `de`, somente `ate`, dois limites inclusivos, `de > ate`, timestamp malformado e ação inexistente, verificando os status e a ausência de efeitos de escrita.
- [x] 3.2 Cobrir múltiplas observações com mesmo timestamp e verificar ordenação determinística `dataHoraCotacao ASC, id ASC` no endpoint e no repositório aplicável.

## 4. Verificação

- [x] 4.1 Executar os testes focados de ação, persistência e histórico e verificar que providers externos permanecem simulados.
- [x] 4.2 Executar `mvn verify` e verificar sucesso completo sem criar ou alterar migrations.
- [x] 4.3 Validar `strengthen-stock-quote-history-atomicity` em modo OpenSpec strict e verificar que somente a delta `stock-quote-history` foi afetada.
