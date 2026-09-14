# Integrações externas

As integrações são isoladas por ports/providers e adapters. Falhas externas são convertidas em respostas controladas; credenciais, URLs sensíveis e corpos brutos não são devolvidos pela API.

## BrasilAPI — dados cadastrais de CNPJ

- **Adapter:** `CnpjDataProvider` / `BrasilApiCnpjAdapter`.
- **Fluxo:** cadastro de Corretora antes da persistência.
- **Endpoint externo:** `GET /api/cnpj/v1/{cnpj}`.
- **Erros:** inexistente 422; payload inválido 502; timeout, conexão, rate limit ou indisponibilidade 503.

## ViaCEP — endereço por CEP

- **Adapter:** `CepDataProvider` / `ViaCepAdapter`.
- **Fluxo:** valida e enriquece endereço da Corretora antes do salvamento.
- **Endpoint externo:** `GET /ws/{cep}/json/`.
- **Erros:** CEP inexistente 422; resposta inválida ou conflito geográfico 502; indisponibilidade 503.

## CVM — participantes intermediários

- **Adapter:** `CvmParticipantProvider` / `CvmParticipantAdapter`.
- **Fluxo:** consulta o dataset oficial de participantes; o CNPJ é elegível quando há registro ativo de Corretora ou Distribuidora.
- **Endpoint externo:** URL configurável em `CVM_PARTICIPANTS_DATASET_URL`, cujo padrão é o ZIP `cad_intermed.zip` da CVM.
- **Erros:** não elegível 422; ZIP/CSV inválido 502; indisponibilidade 503.

## brapi — mercado brasileiro

- **Adapter:** `StockDataProvider` e `StockCatalogProvider` / `BrapiStockAdapter`.
- **Pesquisa:** `GET /api/quote/list?search=...&type=stock&limit=20`.
- **Catálogo:** `GET /api/quote/list?type=stock&page=...&limit=...&search=...`.
- **Resolução/cotação:** `GET /api/v2/stocks/quote?symbols={ticker}`.
- **Uso interno:** `GET /acoes/pesquisar`, `GET /acoes/catalogo`, `POST /acoes/resolver`, cadastro e atualização de ativos `BRASIL`.
- **Erros:** ticker inexistente 422; conteúdo inconsistente 502; autorização, quota, timeout ou conexão 503.

## Twelve Data — mercado dos Estados Unidos

- **Adapter:** `StockDataProvider` e `StockCatalogProvider` / `TwelveDataStockAdapter`.
- **Pesquisa/resolução:** `GET /symbol_search?symbol=...`.
- **Catálogo:** `GET /stocks?country=United%20States&type=Common%20Stock&format=JSON&show_plan=false`.
- **Cotação:** `GET /quote?symbol={ticker}`.
- **Uso interno:** `GET /acoes/pesquisar`, `GET /acoes/catalogo`, `POST /acoes/resolver`, cadastro e atualização de ativos `ESTADOS_UNIDOS`.
- **Erros:** ticker inválido/inexistente ou venue ambíguo 422; conteúdo inconsistente 502; chave, quota, timeout ou conexão 503.

## Configuração, limites e testes

URLs-base, timeouts e credenciais são lidos do ambiente. `BRAPI_TOKEN` é opcional; `TWELVE_DATA_API_KEY` é necessária para consultas americanas. Nenhum token é documentado ou armazenado na coleção Postman. Não há retry automático. Testes normais usam mocks/doubles; testes reais são opt-in e devem ser usados somente para validar compatibilidade do provider.
