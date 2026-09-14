## ADDED Requirements

### Requirement: Atualização explícita de cotações no detalhe da carteira

O frontend SHALL apresentar o botão `Atualizar cotações` no detalhe autenticado de uma carteira e SHALL chamar somente `PUT /carteiras/{id}/atualizar-cotacoes` para essa ação. O botão SHALL ter estados normal e carregando; durante a requisição, MUST ficar desabilitado e impedir clique duplicado. A interface MUST apresentar feedback acessível de sucesso, sucesso parcial e erro controlado, sem expor detalhes internos de provider.

Quando a resposta indicar pelo menos uma atualização, o frontend MUST recarregar as posições e o resumo da carteira e atualizar os cards, gráfico comparativo e distribuição dependentes. A apresentação de BRL e USD MUST permanecer separada, sem conversão implícita. Em resposta sem atualização bem-sucedida, erro HTTP ou erro de rede, a interface SHALL manter os dados atuais e oferecer feedback recuperável.

#### Scenario: Sucesso total

- **WHEN** o endpoint retorna `quantidadeAtualizada` positiva e `quantidadeComFalha` zero
- **THEN** a interface informa sucesso e recarrega posições, resumo e visualizações dependentes

#### Scenario: Sucesso parcial

- **WHEN** o endpoint retorna quantidades positivas de atualização e falha
- **THEN** a interface informa sucesso parcial, pode identificar os tickers retornados e recarrega posições, resumo e visualizações dependentes

#### Scenario: Erro controlado do lote

- **WHEN** o endpoint retorna zero atualizações e uma ou mais falhas, ou a requisição falha
- **THEN** a interface informa erro recuperável, reabilita o botão e não substitui os dados exibidos por dados artificiais
