## 1. Contrato e orquestração backend

- [x] 1.1 Criar o DTO de resultado do refresh com `quantidadeAtualizada`, `quantidadeComFalha` e `tickersComFalha`, sem mensagens ou detalhes internos de provider.
- [x] 1.2 Adicionar `PUT /carteiras/{id}/atualizar-cotacoes`, autenticado, sem request body e com validação de ID positiva.
- [x] 1.3 Implementar serviço de refresh que confirme ownership antes de consultar providers e retorne `404` para carteira inexistente ou de outro usuário.
- [x] 1.4 Derivar os ativos elegíveis somente das posições líquidas abertas com quantidade positiva e retornar contadores zero sem chamadas externas quando não houver posições abertas.
- [x] 1.5 Deduplicar os `acaoId` antes do processamento e chamar a atualização unitária existente uma única vez por ativo.
- [x] 1.6 Reutilizar `AcaoService.atualizarCotacao` para manter seleção brapi/Twelve Data, validação, atualização da cotação atual e registro de histórico.
- [x] 1.7 Processar falhas por ativo sem rollback dos sucessos, retornar `200` com o resultado agregado e não expor segredos ou diagnósticos internos.

## 2. Testes backend

- [x] 2.1 Cobrir posição brasileira aberta, posição americana aberta e carteira mista BRL/USD, verificando provider correto e valuation por moeda.
- [x] 2.2 Cobrir múltiplas operações do mesmo ativo e comprovar uma única atualização externa/persistente por ativo elegível.
- [x] 2.3 Cobrir posição encerrada e carteira sem posições abertas, comprovando ausência de atualização externa.
- [x] 2.4 Cobrir acesso a carteira de outro usuário com `404` e ausência de chamadas a provider.
- [x] 2.5 Cobrir falha brapi, falha Twelve Data, sucesso parcial e todos os ativos com falha, incluindo somente tickers seguros no resultado.
- [x] 2.6 Cobrir que nova cotação altera valuation do resumo e mantém operações, quantidade, custo e preço médio inalterados.
- [x] 2.7 Cobrir que cada sucesso preserva/registra histórico, inclusive quando outro ativo do lote falha.

## 3. Frontend

- [x] 3.1 Adicionar o contrato e a chamada `PUT /carteiras/{id}/atualizar-cotacoes` no módulo de API tipado.
- [x] 3.2 Adicionar o botão `Atualizar cotações` ao detalhe da carteira sem alterar o layout geral.
- [x] 3.3 Implementar estados normal, carregando e botão desabilitado, impedindo submissões duplicadas.
- [x] 3.4 Apresentar feedback distinto para sucesso, sucesso parcial e erro controlado, sem exibir detalhes internos.
- [x] 3.5 Após sucesso ou sucesso parcial, recarregar posições e resumo da carteira para atualizar cards, gráfico e distribuição.
- [x] 3.6 Preservar a exibição separada de BRL e USD em todos os dados recarregados.

## 4. Testes frontend e validação

- [x] 4.1 Cobrir botão visível, loading, desabilitação e prevenção de clique duplicado.
- [x] 4.2 Cobrir feedback de sucesso, sucesso parcial e erro, incluindo tickers com falha quando retornados.
- [x] 4.3 Cobrir recarga de posições e resumo após sucesso/sucesso parcial e preservar separação BRL/USD nos dados exibidos.
- [x] 4.4 Executar testes backend focados e `./mvnw.cmd verify` sem provider real, banco externo ou alteração de migrations.
- [x] 4.5 Executar testes, lint e build do frontend.
- [x] 4.6 Executar `openspec validate add-portfolio-quote-refresh --strict`, `openspec validate --all --strict` e revisão de diff para confirmar escopo planejado.
