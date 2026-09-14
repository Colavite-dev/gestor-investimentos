## 1. Contratos e orquestração de ações

- [x] 1.1 Criar DTOs públicos de sugestão e resolução com validação de ticker/mercado compatível com `AcaoRequest`, e verificar rejeição `400` para entrada local inválida.
- [x] 1.2 Expor `GET /acoes/pesquisar?q=...` e `POST /acoes/resolver` sem alterar os contratos existentes de cadastro, consulta, cotação ou operação; verificar mappings e códigos `200` previstos.
- [x] 1.3 Estender o modelo interno do port de ações para descoberta de sugestões, sem expor DTOs dos providers; verificar compilação e testes do selector (compilação e testes Maven locais confirmados).
- [x] 1.4 Implementar normalização por trim/case-insensitive, limites de termo e resultado, deduplicação por `(ticker, mercado)` e ranking ticker exato/prefixo/nome/demais; verificar ordenação determinística por teste.
- [x] 1.5 Implementar no serviço a agregação limitada e sem escrita das sugestões BR/US, tolerando falha parcial e retornando `200` quando ao menos um provider responder utilizavelmente; verificar que pesquisa não invoca persistência, histórico nem operação.
- [x] 1.6 Classificar falha agregada como `503` somente sem provider utilizável por indisponibilidade e como `502` somente sem provider utilizável por conteúdo incompatível; verificar cenários BR indisponível + US saudável e vice-versa.
- [x] 1.7 Implementar resolução idempotente por ticker normalizado e mercado; verificar que ação existente é retornada sem provider, atualização de cotação ou novo histórico.
- [x] 1.8 Roteiar resolução nova exclusivamente ao provider do mercado selecionado, sem repetir busca BR/US; verificar que o provider do outro mercado não é chamado.
- [x] 1.9 Preservar persistência curta e atômica para ação nova resolvida; verificar que dados externos são consultados antes da transação e que ação e histórico surgem juntos.
- [x] 1.10 Tratar colisão concorrente de `uk_acoes_ticker_mercado` no fluxo de resolução retornando a ação única existente, sem alterar `409` do cadastro explícito; verificar com teste de serviço ou integração determinístico.

## 2. Descoberta pelos providers existentes

- [x] 2.1 Implementar descoberta na brapi usando sua busca de ativos B3 e mapear somente sugestões brasileiras elegíveis; verificar filtragem de FII, ETF, fundo, BDR, índice e resultado inválido.
- [x] 2.2 Preservar na resolução brasileira a confirmação estrita de `requestedSymbol`, `symbol` e `changed=false`; verificar resposta `502` e ausência de escrita para alias, renomeação ou inconsistência.
- [x] 2.3 Adaptar a descoberta da Twelve Data para retornar múltiplas sugestões elegíveis por pesquisa parcial, mantendo filtros Estados Unidos/Common Stock/USD; verificar exclusão de instrumentos incompatíveis.
- [x] 2.4 Garantir que a resolução americana continua a confirmar ticker exato, moeda, cotação e timestamp antes de persistir; verificar que sugestão não confirmada não cria ação.
- [x] 2.5 Cobrir a classificação semântica de `422`, `502` e `503` para busca e resolução sem expor dados sensíveis; verificar ticker inexistente, payload inconsistente, quota/autorização, timeout e conexão com doubles.

## 3. Autocomplete remoto no frontend

- [x] 3.1 Adicionar tipos e métodos do cliente de API para pesquisa e resolução de ações, usando somente `VITE_API_BASE_URL`; verificar que nenhum token ou URL de provider é introduzido no frontend.
- [x] 3.2 Evoluir o combobox de nova operação para consulta remota após dois caracteres e debounce de aproximadamente 350 ms; verificar loading, cancelamento/ignorância de resposta obsoleta e ausência de busca antes do limite.
- [x] 3.3 Exibir sugestões reais com ticker, mercado, moeda e nome quando disponível, mais estados de lista vazia e provider indisponível; verificar que texto livre não habilita o envio.
- [x] 3.4 Resolver a sugestão ao selecioná-la e usar somente o ID retornado no `OperacaoRequest`; verificar atualização de moeda e limpeza da seleção em erro.
- [x] 3.5 Preservar clique, clique fora, Escape, setas e Enter, além da acessibilidade visual do combobox; verificar os fluxos por testes de componente ou validação manual controlada.

## 4. Testes e validação final

- [x] 4.1 Adicionar testes de controller/service/provider para pesquisa BR/US disponível, falha parcial em cada direção, indisponibilidade total, deduplicação, ranking, lista vazia e ausência absoluta de escrita; verificar testes focados sem internet.
- [x] 4.2 Adicionar testes de resolução para ativo existente, novo BR, novo US, provider correto por mercado, provider oposto não chamado, invariantes de cotação/histórico e concorrência idempotente; verificar que `POST /acoes` continua `409` para duplicidade explícita.
- [x] 4.3 Adicionar testes frontend para debounce, carregamento, resultados, lista vazia, erro, resposta obsoleta ignorada, seleção que chama resolver uma única vez e envio de operação com `acaoId` retornado; verificar com o runner já configurado ou documentar a limitação de infraestrutura antes de encerrar. Não há runner frontend instalado; build, lint, tsc e validação manual/E2E permanecem como cobertura disponível.
- [x] 4.4 Executar `npm run build` e `npm run lint`; verificar que TypeScript e bundle passam sem dados mockados.
- [x] 4.5 Executar testes Maven focados e `./mvnw.cmd verify`, validar OpenSpec strict, `git diff --check` e `git status --short`; verificar ausência de migration, segredo, mudança de identidade ou alteração de contrato de operação. Evidência local fornecida: 159 testes, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS.
