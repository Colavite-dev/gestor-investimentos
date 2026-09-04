## 1. brapi: identidade e falhas semânticas

- [x] 1.1 Ajustar o mapeamento da brapi para confirmar `requestedSymbol`, `symbol` e `changed` contra o ticker normalizado solicitado; verificar que renomeação, alias ou campo ausente produz `InvalidStockDataResponseException`.
- [x] 1.2 Revisar a classificação brapi de HTTP `400`, `401`, `403`, `404`, `429` e `5xx` para os tipos de exceção aprovados; verificar com testes de adapter que `404`/resultado vazio é `422`, conteúdo incompatível é `502` e indisponibilidade é `503`.
- [x] 1.3 Ampliar `BrapiStockAdapterTest` para ticker confirmado, ticker retornado diferente, `changed=true`, resultado vazio, preço/moeda inválidos, rate limit, timeout, `5xx` e erro `4xx` genérico; verificar que nenhum cenário aceita ou atualiza um ticker diferente.

## 2. Twelve Data: classificação coerente e descoberta segura

- [x] 2.1 Criar ou ajustar o DTO interno de erro e a leitura de corpos de erro HTTP da Twelve Data; verificar que nenhum texto de provider, URL ou API key entra na exceção pública.
- [x] 2.2 Implementar um classificador semântico comum para envelopes HTTP e estruturados (`200`/`400`/`404`), distinguindo erro explicitamente relacionado ao ticker (`422`) de erro genérico, malformado ou incompatível (`502`); verificar cenários equivalentes nos dois formatos.
- [x] 2.3 Mapear `401`, `403`, `429`, `5xx`, timeout, conexão e chave ausente para indisponibilidade (`503`); verificar com `TwelveDataStockAdapterTest` sem usar API real.
- [x] 2.4 Tornar a descoberta por `symbol_search` segura contra múltiplas correspondências elegíveis exatas; verificar que símbolo ambíguo não chama `/quote` e retorna `422`.
- [x] 2.5 Ampliar os testes Twelve Data para ticker inexistente/não suportado, mercado/tipo/moeda incompatíveis, erro estruturado e HTTP semanticamente equivalentes, resposta sem preço, ticker divergente, `401/403`, `429`, timeout e `5xx`; verificar que a suíte normal não depende de credencial real.

## 3. CVM: múltiplos registros determinísticos

- [x] 3.1 Alterar o parser/estrutura interna da CVM para agregar todas as linhas válidas por CNPJ normalizado antes da decisão; verificar que não há mais semântica de última linha vence.
- [x] 3.2 Centralizar e reutilizar o predicado de elegibilidade ativa de Corretora/Distribuidora na seleção representativa e na validação de corretora; verificar que um registro elegível entre registros inválidos aprova o CNPJ.
- [x] 3.3 Definir e implementar desempate estável para grupos sem registro elegível; verificar que permutações do mesmo CSV retornam a mesma decisão e o mesmo representante.
- [x] 3.4 Ampliar `CvmParticipantAdapterTest` e os testes de serviço pertinentes para registro único válido/inválido, múltiplos registros, válido + inválido, nenhum elegível e independência da ordem; verificar respostas `422` e ausência de persistência quando aplicável.

## 4. Contrato HTTP, segurança e regressão

- [x] 4.1 Revisar os mapeamentos existentes no `GlobalExceptionHandler` apenas se um fluxo aprovado não alcançar `422`, `502` ou `503`; verificar que mensagens públicas continuam genéricas e não expõem corpo remoto, token, chave ou URL.
- [x] 4.2 Confirmar nos testes/configurações que brapi, Twelve Data e CVM mantêm connect/read timeouts explícitos e que não existe retry automático introduzido; verificar que BrasilAPI e ViaCEP não foram alteradas.
- [x] 4.3 Executar os testes focados dos adapters, serviço/controlador afetados e testes de handler; verificar todas as classificações e a independência de rede.
- [x] 4.4 Executar `mvn verify`, validação OpenSpec strict da change e `git diff --check`; verificar sucesso sem executar testes reais opt-in nem alterar migration.
