## Context

`CarteiraPosicaoService` já produz posições abertas a partir das operações cronológicas e confirma ownership pelo usuário autenticado. `AcaoService.atualizarCotacao` já seleciona o provider pelo mercado persistido, valida a resposta e delega a persistência atômica da cotação atual e do histórico a `AcaoQuoteUpdatePersistenceService`. As consultas de posições e resumo são deliberadamente somente leitura.

## Goals / Non-Goals

**Goals:**

- Permitir que o owner atualize sob demanda as cotações dos ativos ainda mantidos em uma carteira.
- Reusar integralmente a atualização unitária de ação e preservar seu histórico por ativo.
- Isolar falha de um ativo, mantendo os êxitos anteriores e posteriores.
- Atualizar no frontend as posições, resumo, gráficos e demais dados derivados depois de êxito total ou parcial.

**Non-Goals:**

- Atualização automática em dashboard, `GET /carteiras/{id}`, posições, resumo ou registro/resolução de operação.
- Alterar migrations V1–V8, banco, fórmulas financeiras, preço médio, custo, operações, resultado realizado, catálogo, filtros EUA, autenticação, demo admin, corretoras, CORS ou isolamento por usuário.
- Criar provider, retry, cache, agendamento ou redesign.

## Decisions

### Endpoint de comando explícito no recurso carteira

`PUT /carteiras/{id}/atualizar-cotacoes` não recebe corpo e requer a autenticação existente. O serviço primeiro confirma `findByIdAndUsuarioId`; carteira inexistente ou de outro usuário retorna `404` sem consultar provider. Esta é uma ação de escrita explícita, distinta dos endpoints GET, que permanecem livres de chamadas externas e de efeitos colaterais.

### Posições abertas determinam o conjunto de ativos

O serviço reutiliza a visão de posições da carteira após a validação de ownership. Apenas respostas cuja `quantidade` seja positiva entram no lote; uma posição totalmente encerrada não entra. Os `acaoId` elegíveis são convertidos em conjunto antes de consultar qualquer provider, garantindo uma atualização por ativo mesmo que existam várias operações de compra e venda para ele. Carteira sem posições abertas retorna sucesso com contadores zero e não chama provider.

### Atualização unitária existente é a fronteira de provider e histórico

Para cada `acaoId` elegível, a orquestração chama `AcaoService.atualizarCotacao`. Assim, `BRASIL` continua usando brapi e `ESTADOS_UNIDOS` Twelve Data pelo selector já existente, inclusive as validações de ticker, moeda, preço, timestamp e tratamento de erros. A persistência de cotação e observação histórica continua na transação curta existente; operações, quantidade, custo e preço médio não são escritos.

### Falha parcial sem rollback do lote

O orquestrador não abre uma transação abrangendo o lote. Ele captura erros controlados de atualização por ativo, registra somente o ticker no resultado e continua os demais itens. Cada atualização bem-sucedida mantém sua própria persistência atômica de cotação e histórico. Erros internos inesperados não devem expor detalhes; devem ser tratados pelo contrato de falha controlada do lote após preservar os sucessos já concluídos.

### Contrato de resultado e semântica HTTP

O endpoint retorna `200 OK` após processar um lote autorizado, incluindo quando todos os itens consultados falharem, pois a ação foi processada e o corpo permite ao cliente comunicar o resultado sem detalhes internos. O DTO contém `quantidadeAtualizada`, `quantidadeComFalha` e `tickersComFalha`; a lista é determinística, sem mensagens de provider, tokens ou stack traces. `404` continua reservado para carteira não pertencente/inexistente; falhas de autenticação seguem a segurança existente.

### Comando no detalhe de carteira

O botão fica no cabeçalho da página de detalhe existente. Ao iniciar, muda para carregando e fica desabilitado para impedir clique duplicado. Após `200`, o frontend diferencia sucesso (`falhas = 0`), sucesso parcial (`atualizadas > 0` e `falhas > 0`) e erro controlado (`atualizadas = 0` e `falhas > 0`). Em sucesso ou sucesso parcial, ele recarrega posições e resumo na mesma carteira, atualizando gráficos e distribuição que dependem desses dados. BRL e USD continuam apresentados e agregados separadamente; em erro HTTP ou de rede, apresenta mensagem recuperável e preserva os dados exibidos.

## Risks / Trade-offs

- **Rate limit/provider indisponível** → atualização é opt-in, usa uma chamada por ativo único e expõe somente contadores/tickers com falha.
- **Falha posterior após sucesso anterior** → não há rollback do lote; isso preserva os dados de mercado efetivamente recebidos e o histórico de cada atualização.
- **Divergência entre posição e execução concorrente** → o conjunto é calculado no início da ação; não há mudança de operação nesta feature e uma execução futura pode atualizar novamente o conjunto então aberto.
- **Todos os ativos falham** → o frontend o apresenta como erro controlado apesar da resposta agregada, sem vazar causa interna.

## Migration Plan

Nenhuma migration, alteração de schema ou operação de banco é necessária. O rollout é aditivo e o rollback consiste em reverter a implementação do endpoint e do botão; histórico e operações existentes permanecem inalterados.
