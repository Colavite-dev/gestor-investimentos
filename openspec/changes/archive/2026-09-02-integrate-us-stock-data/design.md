## Context

O cadastro atual resolve apenas ações brasileiras pela brapi e recebe somente o ticker. `Mercado` e `Moeda` já suportam respectivamente Brasil/BRL e Estados Unidos/USD, e a tabela preserva a identidade `(ticker, mercado)`. A arquitetura existente isola o provider brasileiro atrás de `StockDataProvider` e usa `RestClient` configurável.

A documentação oficial da Twelve Data informa que `/symbol_search` devolve `symbol`, `instrument_name`, `instrument_type`, `country` e `currency`; `/quote` devolve `symbol`, `name`, `currency`, `close` e `timestamp`. A primeira resposta contém os metadados necessários para distinguir ação comum americana de outros instrumentos, mas não a cotação; portanto, uma chamada única não prova todos os requisitos de segurança do domínio.

## Goals / Non-Goals

**Goals:**

- Resolver e persistir ações comuns dos Estados Unidos com ticker, empresa, USD, preço e data/hora obtidos da Twelve Data.
- Selecionar o provider por mercado sem dependência direta da camada de serviço em adapters concretos.
- Preservar a resolução brasileira existente e preparar a escolha de provider pelo mercado persistido para futura atualização real de cotação.
- Manter credenciais e falhas externas isoladas do contrato HTTP público.

**Non-Goals:**

- Implementar atualização de cotação, outros mercados ou tipos de instrumento, cache e retry.
- Consultar `/profile`, que não é necessário para os dados do domínio e possui disponibilidade dependente de plano.
- Alterar entidade, migration, schema, integrações de corretora ou contratos de consulta já existentes.

## Decisions

### Contrato explícito de mercado

`POST /acoes` receberá `{ "ticker": "AAPL", "mercado": "ESTADOS_UNIDOS" }`; para o Brasil, receberá `{ "ticker": "PETR4", "mercado": "BRASIL" }`. O campo é um seletor de integração, não uma origem dos demais dados do ativo, que continuam exclusivos do provider.

Foram avaliadas duas alternativas. A opção A, receber somente ticker e tentar providers em sequência, preservaria a experiência atual, mas introduziria ambiguidade entre mercados, chamadas externas adicionais, falsos positivos e consumo imprevisível de rate limit. A opção B, ticker mais mercado, seleciona uma única fonte, fornece resultado determinístico e reutilizável pela futura atualização por mercado. A opção B é adotada como evolução intencional e breaking do contrato transitório brasileiro; a identificação final continua validada pelos dados externos, em conformidade com a decisão de arquitetura do projeto.

### Seleção simples por mercado

`StockDataProvider` evoluirá para declarar o mercado que atende. Um componente de seleção receberá os providers disponíveis, indexará-os por `Mercado` e entregará o provider adequado ao `AcaoService`. Assim, o serviço depende somente da abstração e não conhece brapi ou Twelve Data. A inexistência de provider para mercado suportado será tratada como indisponibilidade de integração, sem `if/else` dispersos. Esta escolha também permite que a futura atualização use o mesmo seletor a partir de `Acao.mercado`.

### Duas chamadas mínimas à Twelve Data

O adapter fará, nesta ordem, uma chamada a `/symbol_search?symbol={ticker}` e selecionará apenas correspondência exata com `country=United States`, `instrument_type=Common Stock` e `currency=USD`. Em seguida fará uma chamada a `/quote?symbol={ticker}` para extrair `symbol`, `name`, `currency`, `close` e `timestamp`.

Essa combinação de duas chamadas é necessária: `/quote` dá a cotação, nome e timestamp, mas a documentação não o define como fonte de `instrument_type` e `country`; `/symbol_search` fornece esses metadados para a rejeição segura de forex, cripto, índices, ETFs e mercados não americanos. Não serão feitas chamadas a `/profile` ou outras fontes.

### Dados, integridade e identidade

O ticker de entrada será normalizado antes da seleção. A busca de descoberta exigirá equivalência exata com o ticker normalizado; o ticker devolvido por `/quote` também deve ser válido e corresponder ao instrumento aprovado. Como a Twelve Data não documenta neste fluxo resolução de ticker antigo, divergência entre os símbolos será considerada conteúdo incompatível (`502`), não um renome automático. `nomeEmpresa` virá de `name`; `cotacaoAtual`, de `close`; e `dataHoraCotacao`, da conversão do `timestamp` epoch da Twelve Data. Todos são obrigatórios; preço deve ser positivo e moeda deve ser exatamente USD. A duplicidade será verificada com o ticker validado e `ESTADOS_UNIDOS`, além da constraint de banco existente.

### Autenticação e falhas

Será criada configuração de base URL, timeouts e `TWELVE_DATA_API_KEY`. A chave será fornecida à Twelve Data pelo header recomendado `Authorization: apikey <chave>`; ela não será escrita em código, logs, documentação ou respostas. `.env.example` receberá somente `TWELVE_DATA_API_KEY=` se ainda não tiver a chave vazia.

Erros HTTP de negócio externos que indiquem ticker ausente ou instrumento incompatível serão convertidos em `422`. Dados JSON ausentes ou incompatíveis serão convertidos em `502`. Timeout, conexão, HTTP 429, HTTP 5xx, credencial inválida/sem permissão e indisponibilidade serão convertidos em `503`, sem corpo ou detalhes internos do provider. Não haverá retry automático.

## Risks / Trade-offs

- [Duas chamadas consomem dois créditos e podem falhar separadamente] → validar primeiro o instrumento e só pedir cotação após elegibilidade; sem chamadas redundantes.
- [O plano ou o payload da Twelve Data pode variar] → propriedades configuráveis, DTOs restritos aos campos documentados, validação defensiva e teste real opt-in isolado.
- [Exigir mercado é breaking e aumenta um campo no request] → o mercado evita sondagem de providers, ambiguidades e rate limits; documentar a transição no README e nas specs.
- [Dados em horário sem negociação podem representar último fechamento] → persistir somente `close` e `timestamp` efetivamente retornados, sem inventar preço ou horário local.

## Migration Plan

1. Adicionar a configuração segura, o provider americano e o seletor por mercado.
2. Evoluir o request de cadastro e integrar as regras no serviço e no tratamento centralizado de erros.
3. Cobrir adapters, seleção, serviço e controller com fixtures e mocks sem internet; adicionar teste real opt-in para AAPL.
4. Não há migration: a estrutura atual já armazena os campos e enums necessários. Em rollback, reverter o código e manter os registros existentes, que são compatíveis com o schema atual.
