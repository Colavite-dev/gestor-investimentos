## 1. Modelo e persistência

- [x] 1.1 Criar enums e entidade `Operacao` com referências a `Carteira` e `Acao`, tipo, quantidade, preço e data; verificar mapeamento JPA sem alterar entidades existentes indevidamente.
- [x] 1.2 Criar `V4__create_operacoes.sql` com tipos, precisão decimal, foreign keys e índices; verificar aplicação após V1–V3 em H2 e PostgreSQL.
- [x] 1.3 Implementar `OperacaoRepository` para consulta por ID e listagem por carteira ordenada por data e ID; verificar testes de persistência e integridade referencial.

## 2. Contratos e regras

- [x] 2.1 Criar `OperacaoRequest` e `OperacaoResponse` com validações e mapper, sem expor entidade JPA; verificar serialização e rejeição de valores inválidos.
- [x] 2.2 Implementar `OperacaoService` para validar referências, persistir compra/venda e consultar operações; verificar testes unitários sem chamadas externas.
- [x] 2.3 Garantir que cadastro de operação não altere carteira, ação, cotação ou saldo e não execute cálculos derivados; verificar testes de preservação.

## 3. API e tratamento de erros

- [x] 3.1 Implementar `POST /operacoes`, `GET /operacoes/{id}` e `GET /carteiras/{id}/operacoes` com códigos 201, 200 e Location conforme a spec; verificar testes de controller.
- [x] 3.2 Integrar validações, referências inexistentes e falhas de integridade ao handler centralizado com respostas 400/404 sem detalhes internos; verificar contrato JSON.

## 4. Qualidade e documentação

- [x] 4.1 Adicionar testes de integração controller/service/repository para compra, venda, ordenação, referências e regressão de carteira/ação; verificar suíte sem internet.
- [x] 4.2 Atualizar README com o contrato de operações, exemplos e limites do escopo (sem cálculos de posição); verificar documentação consistente.
- [x] 4.3 Executar `mvn test`, `mvn verify` e `openspec validate implement-operation-management --strict`; registrar evidências e reconciliar tasks.
