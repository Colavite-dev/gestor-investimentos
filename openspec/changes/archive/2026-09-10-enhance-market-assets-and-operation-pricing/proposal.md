## Why

A tela de Ações hoje apresenta apenas ativos persistidos, embora os providers já ofereçam descoberta externa, e o formulário de operação não usa a cotação disponível como sugestão. A mudança amplia a descoberta sem importar catálogos para o PostgreSQL e torna o registro de operações mais conveniente sem confundir cotação de mercado com preço efetivamente executado.

## What Changes

- Adicionar um catálogo externo autenticado de ações BR e US, paginado ou progressivo conforme a capacidade real de cada provider, sem persistência automática dos resultados.
- Exibir ticker, empresa, mercado, moeda, exchange e cotação quando viável; somente ações BR poderão receber URL HTTPS de logo da brapi, enquanto ações US usarão sempre o fallback padrão do frontend.
- Usar a listagem paginada da brapi para o catálogo brasileiro e o catálogo de referência cacheado da Twelve Data para ações ordinárias dos Estados Unidos.
- Não consultar logos da Twelve Data em nenhum fluxo desta change, inclusive catálogo, seleção, resolução ou uso de ação US; cotações US continuarão sendo obtidas pontualmente quando o usuário utilizar um ativo ou pedir atualização.
- Ao selecionar um ativo em uma nova operação, obter uma cotação atual, apresentá-la separadamente e sugeri-la no preço unitário, que continuará editável.
- Permitir preço manual válido quando a cotação falhar, mantendo as regras existentes de persistência e saldo.
- Tornar explícitos na carteira o custo baseado em operações, o preço médio, a cotação atual, o patrimônio atual, o resultado e a rentabilidade, sem recalcular operações com a cotação corrente.

## Capabilities

### New Capabilities

- `market-asset-catalog`: Catálogo externo paginado de ações BR/US, metadados opcionais, tolerância a indisponibilidade e ausência de persistência automática.

### Modified Capabilities

- `stock-management`: Expõe o catálogo externo e preserva a identidade `(ticker, mercado)` e a persistência sob demanda.
- `brazilian-stock-data`: Usa a listagem paginada de ações da brapi com cotação e logo quando fornecidas.
- `us-stock-data`: Usa o catálogo de referência da Twelve Data, com filtragem de ações ordinárias US e proibição de qualquer consulta de logo.
- `operation-management`: Define a cotação como sugestão falível e o preço unitário informado no comando como o fato persistido.
- `portfolio-position-calculation`: Explicita cotação atual e rentabilidade, preservando custo e preço médio derivados somente das operações.
- `frontend-application`: Adiciona catálogo, logo brapi somente para BR, fallback obrigatório para US, estados de loading/erro, sugestão editável de preço e apresentação clara de custo versus valuation.
- `frontend-backend-integration`: Integra catálogo externo e atualização de cotação no fluxo de seleção sem quebrar os contratos existentes.

## Impact

- Backend: ports/adapters de mercado, DTOs e endpoint de catálogo, serviço de Ações e respostas de posição/resumo.
- Frontend: tela de Ações, cliente/tipos, combobox e modal de operação, tabelas/resumos/gráficos de carteira.
- Providers: brapi `/api/quote/list`; Twelve Data `/stocks`, mantendo `/symbol_search` e `/quote` para pesquisa/resolução/cotação pontual.
- Persistência: nenhuma migration prevista; V1–V7 permanecem intactas e logos não serão armazenados como binário.
- Segurança: endpoints continuam autenticados, chaves ficam somente no backend e somente URLs HTTPS de logo fornecidas pela brapi podem ser expostas.
