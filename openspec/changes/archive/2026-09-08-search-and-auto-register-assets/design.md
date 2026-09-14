## Context

O cadastro explícito de ação já resolve dados por `StockDataProvider`, persiste a ação e seu histórico inicial em transação curta, e protege `(ticker, mercado)` com `uk_acoes_ticker_mercado`. O modal de nova operação, porém, só filtra a lista local de `GET /acoes`. A Twelve Data já possui descoberta por `symbol_search`; a brapi oferece busca de ativos B3, mas o adapter atual usa somente a cotação por símbolo. Veja `proposal.md` e as deltas para o comportamento contratado.

## Goals / Non-Goals

**Goals:**

- Expor pesquisa remota limitada e somente de leitura para ações BR e US elegíveis.
- Converter somente uma sugestão confirmada em referência persistida reutilizável por `OperacaoRequest.acaoId`.
- Reaproveitar as validações de cadastro e de identidade/provider existentes, mantendo I/O externo fora da transação de escrita.
- Garantir que resolução seja idempotente sob concorrência para a mesma identidade.
- Evoluir o combobox sem transferir providers, segredos ou regras financeiras ao browser.

**Non-Goals:**

- Não criar catálogo local de mercado, cache, retry, conversão cambial, novos mercados ou novos providers.
- Não alterar `POST /acoes`, `POST /operacoes`, contratos de operação, cálculo de posição ou a identidade `(ticker, mercado)`.
- Não apoiar seleção de texto livre, cadastramento enquanto digita, login ou busca universal por instrumentos não elegíveis.

## Decisions

### Dois contratos explícitos: pesquisar e resolver

Será introduzido `GET /acoes/pesquisar?q=...`, que retorna uma lista limitada de DTOs de sugestão (`ticker`, `nomeEmpresa` opcional, `mercado`, `moeda`) e nunca inclui ID persistido como pré-requisito. `POST /acoes/resolver`, com o mesmo par `ticker`/`mercado` de `AcaoRequest`, retorna `AcaoResponse` persistida ou reutilizada com `200 OK`.

`POST /acoes` continuará significando cadastro explícito e preservará `201 Created` e `409` para duplicidade. A resolução terá semântica idempotente: é uma obtenção de referência de ativo para uso em operação, não uma tentativa de cadastrar uma segunda vez. Uma resposta que indique se foi criado ou reutilizado não é necessária para o frontend e não será adicionada.

Alternativa descartada: um único `POST /acoes` disparado ao digitar ou selecionar. Ela confundiria pesquisa com escrita, criaria ativos não escolhidos e manteria duplicidade como erro no fluxo que precisa ser idempotente.

### Pesquisa paralela por mercado, tolerante a falhas parciais

Sem um seletor de mercado obrigatório no formulário, o serviço consultará os providers de Brasil e Estados Unidos para cada termo elegível e combinará as sugestões normalizadas. Cada provider filtrará seu próprio conjunto elegível; o resultado será limitado, ordenado deterministicamente por mercado e ticker, e deduplicado por `(ticker, mercado)`.

A lista vazia é resposta normal quando ambos os providers respondem utilizavelmente, mas não encontram ação elegível. Se um provider falhar por indisponibilidade, quota, timeout, conexão, autenticação/configuração ou conteúdo inválido, mas o outro retornar resultado utilizável — inclusive lista vazia válida — a API responderá `200` apenas com os resultados do provider saudável. Não haverá campo que exponha a falha parcial, detalhes técnicos ou dados inventados. `503` será usado somente quando nenhum provider puder produzir resposta utilizável por indisponibilidade operacional; `502` quando nenhum provider puder produzir resposta utilizável porque as respostas externas recebidas são malformadas ou incompatíveis. Se ambos falharem por categorias distintas, a prioridade será `503` se houver qualquer indisponibilidade operacional; caso contrário, `502`.

Se futuramente houver filtro explícito por mercado, a busca poderá chamar só a fonte escolhida; isso fica fora desta change.

Alternativa descartada: pesquisar somente as ações persistidas primeiro e completar com externos. Isso esconderia novos ativos e tornaria a origem das sugestões ambígua.

### Novas capacidades no port, sem vazamento de DTO externo

`StockDataProvider` ganhará uma operação de descoberta que retorna um modelo interno de sugestão. `BrapiStockAdapter` a implementará com a busca oficial de ativos B3, filtrando somente ações aceitas pelo domínio. `TwelveDataStockAdapter` reutilizará `symbol_search`, porém sem exigir correspondência exata na fase de sugestão e preservando os filtros `United States`, `Common Stock` e USD. Os DTOs externos continuarão restritos aos adapters.

A sugestão é apenas uma pista. Ao resolver, o serviço reutiliza o fluxo completo atual de `consultar(ticker)`: para Brasil, exige `requestedSymbol == symbol == ticker` e `changed=false`; para Estados Unidos, confirma instrumento exato e consulta cotação válida. Portanto, uma sugestão nunca reduz as regras de integridade de cadastro.

### Resolução idempotente e atômica

O serviço normalizará a solicitação e consultará `findByTickerAndMercado` antes de qualquer provider. Havendo ação, retornará a existente sem cotação nova nem histórico novo. Não havendo, roteará somente ao provider indicado pelo `mercado` recebido — nunca repetirá a busca BR+US — e fará a consulta externa fora da transação; a persistência curta continuará gravando ação e histórico no mesmo limite transacional.

Para a corrida entre duas resoluções, a constraint existente será a garantia final. O caminho de colisão será distinguido do cadastro explícito: após a tentativa concorrente não persistir, o serviço relerá a ação por `(ticker, mercado)` e a retornará. Erros de integridade sem essa ação correspondente não serão mascarados como resolução bem-sucedida. Essa abordagem preserva o comportamento `409` de `POST /acoes` e evita duplicidade ou erro espúrio para a seleção idempotente.

### Frontend: debounce, seleção e moeda

O combobox terá estado de texto de consulta separado da ação resolvida. A partir de dois caracteres, um debounce de 350 ms chamará o módulo `acoesApi.search`; cada consulta terá identificador/abort controller e respostas antigas serão canceladas ou ignoradas para que, por exemplo, uma resposta tardia de `PET` não substitua `PETR`. A lista exibirá ticker, empresa quando presente, mercado abreviado e moeda. Clique, setas, Enter, Escape e clique fora continuarão a funcionar.

Ao selecionar, o campo entra em estado de resolução e chama `acoesApi.resolve`. Só a `AcaoResponse` retornada torna o formulário válido; digitar, limpar ou falhar na resolução limpa `acaoId`. A moeda de preço e total vem dessa resposta. Nenhum total cruza moedas e nenhuma operação é enviada até haver ID válido.

### Erros, limites e segurança

Validação local de termo/DTO é `400`; não encontrado/não elegível/ambíguo com semântica confiável é `422`; dados externos incompatíveis são `502`; autorização, quota, rate limit, timeout, conexão, `5xx` ou chave ausente/inválida são `503`. O cliente reutilizará as mensagens públicas de `ApiError`, sem renderizar corpo externo.

O browser falará somente com a URL configurada por `VITE_API_BASE_URL`. Nenhuma chave, token, header de provider ou URL de provider será acrescentado a `frontend/`. A configuração CORS existente permanece suficiente para a mesma origem de desenvolvimento; esta change não amplia origens.

## Risks / Trade-offs

- [Busca global consome duas fontes por termo] → mínimo de dois caracteres, debounce de 350 ms, limite de sugestões e ausência de retry automático reduzem consumo; quotas continuam sujeitas ao plano de cada provider.
- [Descoberta e resolução podem divergir por mudança externa] → seleção sempre executa a validação completa e não persiste quando o provider não confirma o ativo.
- [Colisão de unicidade após consulta externa] → a constraint continua definitiva e a resolução relê somente a identidade confirmada para retornar ação única.
- [Falha parcial reduz a abrangência sem poder ser exposta como dado técnico] → resultados válidos disponíveis são retornados com `200`; erros públicos são reservados para quando nenhum provider produz resposta utilizável.
- [Teste de autocomplete pode ficar frágil por timers] → usar fake timers e mocks do módulo de API; testes normais de backend usarão doubles dos providers e não internet.

## Migration Plan

1. Publicar endpoints novos mantendo todos os existentes compatíveis.
2. Publicar frontend que usa pesquisa/resolução antes de `POST /operacoes`.
3. Não há migration de banco; a constraint e as tabelas V1–V6 permanecem.
4. Em rollback, remover o uso dos dois endpoints pelo frontend; ativos já resolvidos permanecem cadastros mestres válidos e operações existentes continuam usando seus IDs.

## Open Questions

_Nenhuma que altere o contrato aprovado. O limite exato de sugestões será escolhido na implementação dentro do limite de UX e validado por testes._
