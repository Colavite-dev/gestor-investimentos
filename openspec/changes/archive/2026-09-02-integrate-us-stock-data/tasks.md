## 1. Contrato e seleção de providers

- [x] 1.1 Evoluir o DTO de cadastro para exigir `ticker` e `mercado`, sem aceitar dados manuais do ativo, e verificar a validação local por testes de controller.
- [x] 1.2 Evoluir a abstração de dados de ações, implementar seletor de `StockDataProvider` por mercado e adaptar a resolução brasileira sem alterar seus dados externos, verificando seleção BR/EUA, ausência controlada de provider e regressão BR por testes unitários e de serviço.

## 2. Integração Twelve Data

- [x] 2.1 Criar propriedades configuráveis de base URL, timeouts e `TWELVE_DATA_API_KEY`, atualizar `.env.example` apenas com chave vazia quando necessário e verificar que nenhuma credencial é exposta por teste/configuração.
- [x] 2.2 Implementar o adapter Twelve Data com `RestClient` e DTOs privados para `/symbol_search` e `/quote`, verificando por testes HTTP simulados a autenticação e o mapeamento dos campos necessários.
- [x] 2.3 Validar no adapter a correspondência exata, `United States`, `Common Stock`, USD, ticker, nome, `close` positivo e `timestamp` conversível, verificando rejeições por testes de fixtures incompatíveis.
- [x] 2.4 Converter ticker ausente ou instrumento incompatível em exceção de negócio e timeout, conexão, HTTP 429, HTTP 5xx, autenticação/configuração e conteúdo inválido em exceções internas adequadas, verificando cada classe de erro por testes isolados do adapter.

## 3. Cadastro e contrato HTTP

- [x] 3.1 Integrar o provider selecionado ao `AcaoService`, persistindo somente dados externos válidos e verificando por testes de serviço os fluxos AAPL, BRASIL e duplicidade por `(ticker, mercado)`.
- [x] 3.2 Garantir que ticker inexistente ou instrumento incompatível não persiste e retorna 422, resposta externa incompatível retorna 502 e indisponibilidade externa retorna 503, verificando o mapeamento pelo tratamento centralizado e testes de controller.
- [x] 3.3 Atualizar README e documentação de configuração para o novo contrato de `POST /acoes`, os providers por mercado, a chave sem valor e as limitações da Twelve Data, verificando ausência de segredo nos arquivos rastreados.

## 4. Testes e validação final

- [x] 4.1 Cobrir com testes automatizados sem internet a normalização, nome, USD, preço, timestamp, seleção de provider, duplicidade, respostas 400/409/422/502/503 e não regressão da brapi, verificando a suíte focada aprovada. Evidência: 83 testes normais aprovados no `mvn verify` de 2026-09-02.
- [x] 4.2 Criar e executar uma vez um teste real opt-in para AAPL, excluído da suíte normal, verificando autenticação, endpoints, parser, ticker, nome, USD, preço e timestamp sem registrar a chave. Evidência: `TwelveDataStockRealIT` aprovado com AAPL em 2026-09-02, sem saída de credencial.
- [x] 4.3 Executar `mvn verify` e `openspec validate integrate-us-stock-data --strict`, registrar as evidências reais e reconciliar as tasks sem criar migration ou repetir PostgreSQL/Docker sem alteração estrutural. Evidência: `mvn verify` aprovado (83 testes) e strict válido em 2026-09-02; schema/JPA/migrations inalterados.
