## 1. Modelo e persistência de ações

- [x] 1.1 Criar `V2__create_acoes.sql` com tabela, constraints de mercado/moeda e unicidade `(ticker, mercado)`, verificando a aplicação da migration no perfil H2 e a compatibilidade com PostgreSQL.
- [x] 1.2 Implementar entidade `Acao` e enums controlados de mercado e moeda, verificando por testes que Brasil deriva BRL e Estados Unidos deriva USD sem relação com Corretora.
- [x] 1.3 Criar o repository com consultas ordenadas, por identidade composta e por ticker normalizado, verificando persistência, unicidade e busca em teste de repository.

## 2. Aplicação e regras de negócio

- [x] 2.1 Criar DTOs de request/response e mapper sem expor a entidade, verificando validação dos campos transitórios de cadastro e ausência de moeda no request.
- [x] 2.2 Implementar o service de cadastro com normalização de ticker, derivação de moeda e tradução de violação concorrente de unicidade, verificando cadastro válido e `409` em teste unitário.
- [x] 2.3 Implementar listagem e busca por id, verificando ordem crescente, retorno do ativo e `404` para id inexistente.
- [x] 2.4 Implementar busca determinística por ticker com parâmetro opcional `mercado`, verificando ticker único, busca pela identidade completa, ausência e ambiguidade entre mercados.

## 3. API HTTP e erros centralizados

- [x] 3.1 Criar `POST /acoes`, `GET /acoes` e `GET /acoes/{id}` com DTOs e headers esperados, verificando `201 Created`, `200 OK`, `400` e `404` em testes de controller.
- [x] 3.2 Criar `GET /acoes/ticker/{ticker}` com comportamento documentado para o parâmetro `mercado`, verificando normalização, resposta única e erro de ambiguidade em testes de controller.
- [x] 3.3 Adicionar exceções e mapeamentos centralizados para ação inexistente e ativo duplicado, verificando contrato JSON e status `404`/`409` sem detalhes internos.
- [x] 3.4 Confirmar por teste de rota e revisão de componentes que `PUT /acoes/{id}/atualizar-cotacao` e providers de resolução/cotação não são expostos nesta capability.

## 4. Regressão e validação consolidada

- [x] 4.1 Executar os testes automatizados do módulo e a suíte de regressão, verificando que nenhum teste depende de internet ou altera o comportamento de Corretoras. Evidência: `mvn verify` executou 68 testes sem falhas, erros ou dependência de internet.
- [x] 4.2 Executar `mvn verify`, uma validação consolidada com PostgreSQL/Flyway para a nova migration e `openspec validate implement-stock-management --strict`, reconciliando esta lista apenas com evidências reais. Evidências: `mvn verify` aprovou 68 testes; PostgreSQL 17.11 em `localhost:5433` validou V1/V2 e aplicou V2 com sucesso; validação strict aprovada.
