## Context

A capability `stock-management` já persiste ações como ativos mestres e expõe um cadastro transitório com dados manuais. A arquitetura atual usa `RestClient`, propriedades tipadas, adapters por integração e exceções centralizadas. Esta mudança altera apenas o cadastro brasileiro; a entidade `Acao`, os enums `Mercado`/`Moeda`, a identidade `(ticker, mercado)` e a tabela já comportam os dados resolvidos.

## Goals / Non-Goals

**Goals:**

- Resolver e persistir uma ação brasileira a partir de um único ticker e de dados reais da brapi v2.
- Isolar o contrato HTTP e os DTOs externos atrás de um port que devolve somente dados internos necessários ao cadastro.
- Tratar falhas de provider sem vazar token, URL, corpo de erro ou detalhes do cliente HTTP.
- Manter o cadastro preparado para um provider americano futuro sem implementá-lo agora.

**Non-Goals:**

- Não implementar `PUT /acoes/{id}/atualizar-cotacao`, provider dos Estados Unidos, cache, retry, circuit breaker, histórico de cotações ou alterações de schema.
- Não expor endpoints da brapi, nem aceitar novamente dados manuais de mercado, moeda, empresa, preço ou horário no `POST /acoes` brasileiro.

## Decisions

### Uma chamada ao endpoint de cotação v2

O adapter consultará `GET /api/v2/stocks/quote?symbols={ticker}`. A documentação oficial atual mostra que a resposta de cotação retorna em `results[]` o `requestedSymbol`, o `symbol` canônico, `changed`, `data.shortName`, `data.currency`, `data.regularMarketPrice` e `data.regularMarketTime`. Portanto, essa única chamada cobre a validade do ticker, a resolução de renome, o nome, a moeda, o preço e o horário necessários ao cadastro.

`/api/v2/tickers/resolve` não será chamado neste fluxo: embora seja adequado para conversões em lote, seria uma segunda chamada redundante, porque o endpoint de cotação já resolve renomes e informa `requestedSymbol`, `symbol` e `changed`. `/api/v2/stocks/profile` também não será chamado, pois seus dados cadastrais não são necessários para o modelo atual.

O fluxo usará exclusivamente o recurso de ações da brapi, e não os recursos separados de FIIs, fundos, opções, futuros ou cripto. Um resultado ausente não é aceito como ação.

### Port interno e modelo mínimo

Será criado um port `StockDataProvider` com uma operação de consulta por ticker normalizado e um modelo interno imutável, por exemplo `StockRegistrationData`, contendo ticker canônico, nome da empresa, moeda, cotação atual e data/hora da cotação. `BrapiStockAdapter` implementará esse port e manterá DTOs da brapi no pacote da integração.

`AcaoService` dependerá apenas de `StockDataProvider`. Nesta change haverá uma única implementação, para Brasil. Uma change americana poderá introduzir outro adapter e uma seleção explícita de provider/resolvedor sem alterar o modelo interno nem fazer o domínio conhecer JSON ou HTTP. O contrato atual de `POST /acoes` com apenas `ticker` permanece a direção de evolução: a feature americana deverá definir a seleção de mercado preservando a compatibilidade para tickers brasileiros, podendo acrescentar um discriminador opcional apenas se isso for indispensável e retrocompatível.

### Orquestração e ticker canônico

O request será reduzido a um DTO com somente `ticker`, validado e normalizado para maiúsculas antes da chamada externa. O service seguirá:

1. validar e normalizar localmente;
2. consultar o `StockDataProvider`;
3. validar o modelo interno retornado;
4. usar o ticker canônico retornado e `Mercado.BRASIL` para verificar duplicidade;
5. construir `Acao` com moeda derivada do mercado, nome, preço e horário retornados;
6. persistir e converter para response.

Quando `requestedSymbol` divergir de `symbol`, o adapter entregará o símbolo canônico e o service persistirá somente ele. Isso evita duplicidade entre código antigo e código atual. A constraint existente no banco continua sendo a proteção concorrente final.

### Integridade dos dados externos

O adapter exigirá exatamente um resultado aproveitável para o ticker solicitado e campos não vazios para ticker canônico e nome; moeda igual a `BRL`; preço positivo; e `regularMarketTime` conversível para `Instant`. Falhas de conteúdo, quantidade inesperada de resultados ou incompatibilidade dos campos resultarão em exceção de resposta externa inválida (`502`). Resultado ausente ou o status externo que indique ticker não encontrado se converterá em rejeição de negócio (`422`).

### Configuração e autenticação

`BrapiProperties` centralizará URL base e timeouts, com defaults compatíveis com a documentação e sobrescrita por variáveis de ambiente. `BRAPI_TOKEN` será opcional. O `RestClient` específico da brapi incluirá `Authorization: Bearer <token>` somente quando existir token não vazio; ele nunca será recebido na API, persistido ou registrado. O arquivo `.env.example`, se atualizado, conterá apenas a chave vazia.

O provider será configurado com timeouts explícitos no mesmo padrão das integrações existentes. Não haverá retry automático: HTTP 429, timeout, conexão recusada, 5xx e respostas de acesso que impeçam a consulta serão convertidos para indisponibilidade controlada (`503`).

### Contrato e erros HTTP

O controller continuará com `POST /acoes`, mas usará o DTO de ticker. A configuração existente que rejeita propriedades JSON desconhecidas garante `400` para os campos manuais removidos antes de chamar o provider. As exceções internas serão mapeadas pelo handler centralizado:

- ticker não encontrado/não reconhecido: `422`;
- duplicidade do ticker canônico: `409`;
- conteúdo externo inválido: `502`;
- timeout, conexão, 429, 5xx, indisponibilidade ou impossibilidade de acesso ao provider: `503`.

As respostas manterão o contrato JSON existente e não incluirão detalhes da brapi.

## Risks / Trade-offs

- [Alguns tickers exigem token ou plano compatível] → `BRAPI_TOKEN` será opcional e seguro; 401/403 serão reportados como indisponibilidade controlada, sem sugerir que o ticker do usuário é inválido.
- [A cotação pode não estar disponível em determinado momento] → o cadastro exige preço e horário válidos, evitando persistir dados incompletos; uma atualização de cotação será tratada em change posterior.
- [Um ticker antigo exige consulta externa para descobrir a identidade canônica] → a verificação definitiva de duplicidade ocorrerá depois da resolução; a constraint `(ticker, mercado)` preserva segurança concorrente.
- [A futura seleção entre Brasil e EUA ainda não existe] → o port usa modelo interno independente de provider, e a regra de roteamento ficará explicitamente para a capability americana, sem heurística prematura nesta change.

## Migration Plan

Não haverá migration: os campos atuais de `acoes` suportam o fluxo. A implementação substituirá o DTO manual de criação pelo DTO de ticker; clientes que enviavam o contrato transitório precisarão migrar para `{ "ticker": "PETR4" }`. Em caso de rollback de aplicação, a tabela e os dados persistidos continuam compatíveis estruturalmente; a reversão do contrato HTTP requer restaurar a versão anterior da aplicação.
