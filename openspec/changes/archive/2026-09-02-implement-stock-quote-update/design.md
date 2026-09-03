## Contexto

O cadastro de ações já resolve dados externos por `StockDataProvider`, selecionado por `StockDataProviderSelector` a partir do mercado informado. Ações persistidas possuem ticker canônico, mercado, moeda, cotação e data/hora. Esta mudança adiciona a atualização síncrona de cotação sem redescobrir o ativo ou seu mercado.

## Objetivos

- Expor `PUT /acoes/{id}/atualizar-cotacao` sem corpo e retornar a representação atualizada.
- Reutilizar o seletor atual e os adapters brapi e Twelve Data.
- Consultar uma única vez a fonte adequada para cada atualização.
- Atualizar somente cotação e data/hora após validar compatibilidade com a ação existente.
- Manter a chamada HTTP fora da transação de escrita do banco.

## Não objetivos

- Histórico de cotações, scheduler, cache, retry, atualização assíncrona ou locking adicional.
- Redescoberta de mercado, novo cadastro, alteração de identidade/campos cadastrais, migrations ou mudanças de schema.
- Novos mercados, providers, operações, carteira ou dashboard.

## Decisões

### Evolução mínima do port

`StockDataProvider` passará a oferecer uma operação específica de cotação, como `consultarCotacao(String ticker)`, retornando um modelo interno `StockQuoteData` com ticker, moeda, cotação e data/hora. A operação de cadastro/resolução continuará separada, pois precisa de dados como nome da empresa e, para EUA, descoberta do instrumento.

O domínio e `AcaoService` conhecerão somente `StockQuoteData`; DTOs HTTP e formatos brapi/Twelve Data permanecerão nos adapters.

### Fluxo de atualização

1. Localizar a ação pelo ID; se ausente, lançar erro de não encontrado sem chamar provider.
2. Capturar uma visão mínima de identidade: ID, ticker, mercado e moeda.
3. Selecionar o provider pelo mercado persistido com `StockDataProviderSelector`.
4. Consultar a cotação diretamente pelo ticker persistido.
5. Validar ticker normalizado, moeda, preço positivo e timestamp contra a visão da ação.
6. Em uma transação curta de escrita, reler a ação e aplicar exclusivamente cotação e data/hora; se tiver sido removida no intervalo, retornar `404`.
7. Retornar o DTO atual da ação.

Uma estratégia simples baseada em `TransactionTemplate` (ou equivalente já disponível no Spring) será usada para limitar a transação ao passo de escrita. A chamada HTTP não ficará dentro de uma transação de banco. Não será introduzido controle concorrente adicional.

### Providers e chamadas externas

Para `BRASIL`, `BrapiStockAdapter` chamará diretamente o endpoint de quote já adotado (`GET /api/v2/stocks/quote?symbols={ticker}`), uma vez por atualização.

Para `ESTADOS_UNIDOS`, `TwelveDataStockAdapter` chamará diretamente `/quote`, uma vez por atualização. `symbol_search` não será chamado porque o ativo já foi validado no cadastro e seu mercado já está persistido. A autenticação existente com `TWELVE_DATA_API_KEY` será reutilizada; ausência da chave afetará somente o fluxo EUA e será tratada como `503`.

### Compatibilidade e preservação

O ticker retornado deve corresponder ao ticker canônico persistido após normalização. A moeda retornada deve coincidir com a moeda persistida e com a moeda do mercado (`BRL` para Brasil e `USD` para Estados Unidos). Preço nulo, zero, negativo, não finito ou timestamp ausente/inválido são conteúdo externo incompatível.

Somente `cotacaoAtual` e `dataHoraCotacao` serão modificados. Identidade, nome e moeda nunca serão substituídos por valores retornados durante a atualização.

### Erros HTTP

| Situação | Resposta da API |
| --- | --- |
| Ação inexistente | `404` |
| Ticker inexistente ou não consultável no provider | `422` |
| Ticker, moeda, preço, timestamp ou payload incompatível | `502` |
| Timeout, conexão, `429`, `5xx`, indisponibilidade ou configuração/autenticação necessária | `503` |

As exceções internas existentes serão reutilizadas ou estendidas minimamente, sem retornar payload externo, URL, stack trace ou segredo.

## Testes e validação

Testes normais usarão mocks/fixtures e cobrirão os fluxos BR e EUA, seleção exclusiva do provider correto, persistência somente dos dois campos permitidos e todos os erros previstos. Nenhum teste normal acessará a internet.

Será planejado um teste opt-in separado que consulta diretamente PETR4 e AAPL uma única vez cada, para provar as duas operações diretas de quote e os parsers correspondentes. Ele ficará fora da suíte normal e não executará descoberta de instrumento.

Não haverá migration nem validação PostgreSQL/Docker adicional, pois entidade e schema não mudam.

## Riscos e mitigação

- Um ticker pode deixar de ser consultável depois do cadastro: responder `422` e preservar os dados já persistidos.
- Dados inconsistentes da fonte não devem corromper a ação: validar antes da transação de escrita e responder `502`.
- Falhas externas não devem afetar o outro mercado: seleção e configuração permanecem isoladas por provider.
