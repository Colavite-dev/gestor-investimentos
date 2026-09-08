# Integrações externas

As integrações são isoladas por ports/providers e adapters. Falhas externas são convertidas em respostas controladas; credenciais, URLs sensíveis e corpos brutos não são devolvidos pela API.

## BrasilAPI — dados cadastrais de CNPJ

- **Uso:** cadastro de Corretora, antes da persistência.
- **Abstração:** `CnpjDataProvider` / `BrasilApiCnpjAdapter`.
- **Autenticação:** não requerida para o endpoint consumido.
- **Configuração:** `BRASIL_API_BASE_URL`, `BRASIL_API_CONNECT_TIMEOUT` (default `2s`) e `BRASIL_API_READ_TIMEOUT` (default `5s`).
- **Erros:** CNPJ inexistente retorna 422; resposta inválida retorna 502; indisponibilidade, timeout, conexão ou rate limit retornam 503.
- **Teste real:** `.\mvnw.cmd -Dtest=BrasilApiCnpjRealIT test`; execução explícita, fora da suíte normal.

## ViaCEP — endereço por CEP

- **Uso:** validação e enriquecimento de endereço no cadastro de Corretora.
- **Abstração:** `CepDataProvider` / `ViaCepAdapter`.
- **Autenticação:** não requerida.
- **Configuração:** `VIA_CEP_BASE_URL`, `VIA_CEP_CONNECT_TIMEOUT` (default `2s`) e `VIA_CEP_READ_TIMEOUT` (default `5s`).
- **Erros:** CEP não encontrado retorna 422; resposta inválida ou conflito geográfico retorna 502; indisponibilidade, timeout ou conexão retornam 503.
- **Teste real:** `.\mvnw.cmd -Dtest=ViaCepRealIT test`; execução explícita, fora da suíte normal.

## CVM — participantes intermediários

- **Uso:** validação final de elegibilidade da Corretora a partir do dataset oficial de participantes intermediários.
- **Abstração:** `CvmParticipantProvider` / `CvmParticipantAdapter`.
- **Autenticação:** não requerida.
- **Configuração:** `CVM_PARTICIPANTS_DATASET_URL`, `CVM_PARTICIPANTS_CONNECT_TIMEOUT` (default `2s`), `CVM_PARTICIPANTS_READ_TIMEOUT` (default `10s`) e `CVM_PARTICIPANTS_REFRESH_INTERVAL` (default `24h`).
- **Regra:** um CNPJ é elegível quando possui ao menos um registro ativo de Corretora ou Distribuidora; a decisão não depende da ordem das linhas do CSV.
- **Erros:** participante não elegível retorna 422; ZIP/CSV inválido retorna 502; indisponibilidade, timeout, conexão ou rate limit retornam 503.
- **Teste real:** `.\mvnw.cmd -Dtest=CvmParticipantRealIT -DrunCvmRealIT=true test`.

## brapi — ações brasileiras

- **Uso:** cadastro e atualização de cotação de Ações no mercado `BRASIL`.
- **Abstração:** `StockDataProvider` / `BrapiStockAdapter`.
- **Autenticação:** `BRAPI_TOKEN` é opcional e, quando configurado, é usado apenas pelo adapter brapi.
- **Configuração:** `BRAPI_BASE_URL`, `BRAPI_CONNECT_TIMEOUT` (default `2s`) e `BRAPI_READ_TIMEOUT` (default `5s`).
- **Regra de identidade:** ticker solicitado, `requestedSymbol` e `symbol` devem coincidir após normalização e `changed` deve ser `false`. Alias, renomeação ou divergência são resposta externa inconsistente (502); não há renomeação automática.
- **Erros:** resultado vazio ou 404 inequivocamente de ticker inexistente retorna 422; payload/4xx genérico inconsistente retorna 502; autorização, quota, 5xx, timeout e conexão retornam 503.
- **Teste real:** `.\mvnw.cmd -Dtest=BrapiStockRealIT -DrunBrapiRealIT=true test`.

## Twelve Data — ações americanas

- **Uso:** descoberta/validação de símbolo e cotação de Ações no mercado `ESTADOS_UNIDOS`; atualização usa a cotação da ação já persistida.
- **Abstração:** `StockDataProvider` / `TwelveDataStockAdapter`.
- **Autenticação:** `TWELVE_DATA_API_KEY` é necessária para a consulta americana e fica somente no ambiente local.
- **Configuração:** `TWELVE_DATA_BASE_URL`, `TWELVE_DATA_CONNECT_TIMEOUT` (default `2s`) e `TWELVE_DATA_READ_TIMEOUT` (default `5s`).
- **Regra:** o símbolo precisa corresponder ao ticker e aos critérios americanos implementados; moeda incompatível, ausência de preço ou conteúdo inconsistente retornam 502.
- **Erros:** ticker inequivocamente inválido, inexistente, não suportado ou ambíguo retorna 422; conteúdo malformado/inconsistente retorna 502; chave inválida, quota, 5xx, timeout ou conexão retornam 503.
- **Teste real:** `.\mvnw.cmd -Dtest=TwelveDataStockRealIT -DrunTwelveDataRealIT=true test`, com `TWELVE_DATA_API_KEY` no ambiente.

## Limites e disponibilidade

Não há retry automático. Rate limits e quotas dependem da política e do plano de cada provider; o projeto não fixa números que não controla. Os testes normais usam doubles/mocks ou H2 e não dependem de internet.
