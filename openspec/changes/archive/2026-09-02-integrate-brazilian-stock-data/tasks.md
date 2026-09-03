## 1. Contrato interno e configuração da integração

- [x] 1.1 Criar o port `StockDataProvider`, o modelo interno mínimo de resolução e DTOs externos privados da cotação v2, verificando por testes que nenhum DTO da brapi alcança o service e que fixture oficial desserializa `requestedSymbol`, `symbol`, `changed`, nome, moeda, preço e horário.
- [x] 1.2 Criar propriedades e `RestClient` específicos da brapi com URL, timeouts e token opcional por ambiente, verificando que token vazio não gera header e que `.env.example` contém somente chaves vazias quando atualizado.

## 2. Adapter da brapi e falhas externas

- [x] 2.1 Implementar `BrapiStockAdapter` com uma chamada a `/api/v2/stocks/quote`, validando resultado único e campos obrigatórios BRL/preço/horário e verificando mapeamento de ação válida, ticker canônico, ticker ausente e conteúdo incompatível.
- [x] 2.2 Tratar HTTP 429, 5xx, 401/403, timeout e falha de conexão como indisponibilidade controlada, verificando que nenhuma mensagem, corpo ou token da brapi é exposto.

## 3. Cadastro de ações brasileiras por ticker

- [x] 3.1 Substituir o request transitório por DTO com somente `ticker`, verificando Bean Validation e rejeição de atributos manuais removidos antes de consultar o provider.
- [x] 3.2 Integrar `AcaoService` ao port para normalizar ticker, resolver dados externos, fixar `Mercado.BRASIL`/BRL, persistir o ticker canônico e preservar a proteção concorrente de unicidade, verificando fluxos válido, renomeado e duplicado.
- [x] 3.3 Adicionar exceções e mapeamentos centralizados para ticker não encontrado (`422`), resposta externa incompatível (`502`) e indisponibilidade (`503`), verificando o contrato JSON sem detalhes internos.

## 4. Cobertura automatizada

- [x] 4.1 Criar testes isolados do adapter para cotação válida, autenticação opcional, ticker inexistente, ticker renomeado, conteúdo inválido, timeout, conexão e rate limit, sem chamadas reais à internet.
- [x] 4.2 Atualizar testes de service e controller para dados brasileiros resolvidos, moeda/mercado automáticos, duplicidade pelo ticker canônico, ausência de chamada externa para erro local e respostas `201`, `400`, `409`, `422`, `502` e `503`, preservando listagem e buscas sem internet.

## 5. Documentação e validação consolidada

- [x] 5.1 Documentar no README a brapi como provider brasileiro, o contrato de ticker, `BRAPI_TOKEN` opcional, limites e a evolução prevista para EUA, verificando que não há segredo documentado.
- [x] 5.2 Criar ou adaptar teste real opt-in da brapi usando ticker permitido sem token, verificando separadamente a resposta real sem torná-la dependência da suíte normal. Evidência: `BrapiStockRealIT` com PETR4 aprovado em 2026-09-02.
- [x] 5.3 Executar testes automatizados, `mvn verify` e `openspec validate integrate-brazilian-stock-data --strict`, reconciliando esta lista apenas com evidências reais e confirmando que não há migration necessária. Evidências: `mvn verify` aprovou 71 testes e a validação strict foi aprovada em 2026-09-02.
