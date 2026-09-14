## 1. Seleção determinística de venue US

- [x] 1.1 Modelar internamente as prioridades pelos pares completos `exchange` e `mic_code` para NASDAQ/XNGS, NYSE/XNYS, NYSE American/XASE e fallback IEX/IEXG, verificando que a ordem do payload não é usada.
- [x] 1.2 Atualizar a descoberta de instrumento americano para selecionar exatamente um venue principal, ou um IEX único na ausência de principal, verificando que AAPL NASDAQ+IEX seleciona NASDAQ antes de consultar a cotação.
- [x] 1.3 Preservar os filtros de ticker exato, United States, Common Stock e USD antes da prioridade, verificando que instrumentos incompatíveis não consultam `/quote`.
- [x] 1.4 Rejeitar ambiguidades sem vencedor único, incluindo duplicidade na maior prioridade ou venues desconhecidos, verificando que nenhuma cotação ou persistência ocorre.

## 2. Erros e fluxos preservados

- [x] 2.1 Introduzir erro sanitizado específico para ambiguidade de venue e mapeá-lo para 422, verificando que não afirma ticker inexistente nem expõe dados da Twelve Data.
- [x] 2.2 Preservar a classificação atual de ticker inexistente/incompatível, payload inválido e indisponibilidade, verificando respectivamente 422, 502 e 503 no handler central.
- [x] 2.3 Manter a chamada única a `/quote` e seu parser após seleção bem-sucedida, verificando ticker, USD, preço positivo e timestamp válido.
- [x] 2.4 Preservar `POST /acoes/resolver` para ativo já existente e para persistência sob demanda de um novo ativo selecionado, verificando identidade `(ticker, mercado)` e idempotência atuais.

## 3. Testes e validação

- [x] 3.1 Adicionar testes do adapter para match único, NASDAQ+IEX, ordem invertida, IEX único, empate não resolvível, ticker inexistente e filtros de país/tipo/moeda, verificando se `/quote` é chamado somente após seleção válida.
- [x] 3.2 Adicionar testes de service/controller para resolver AAPL selecionando venue principal, retornar 422 de ambiguidade coerente e preservar resolução idempotente de ativo existente.
- [x] 3.3 Executar `mvn verify`, `openspec validate resolve-us-stock-venue-ambiguity --strict` e `git diff --check`, verificando que não há migration, alteração de frontend, catálogo `/stocks` ou dependência nova.
