## 1. Cálculo de posições

- [x] 1.1 Criar modelos internos de posição e resumo, incluindo moeda e valores monetários; verificar que DTOs não expõem entidades JPA.
- [x] 1.2 Implementar cálculo linear de quantidade líquida, custo ponderado, valor investido, patrimônio e lucro/prejuízo por ação; verificar compras, vendas e ativo totalmente vendido.
- [x] 1.3 Implementar agregação por moeda para carteiras com BRL e USD, sem conversão cambial; verificar valores sem cotação como indisponíveis.

## 2. Serviço e API

- [x] 2.1 Implementar service de posições e resumo reutilizando operações, ações e validação de carteira existente; verificar carteira vazia e carteira inexistente.
- [x] 2.2 Expor `GET /carteiras/{id}/posicoes` e `GET /carteiras/{id}/resumo` com ordenação determinística e contrato documentado; verificar respostas 200 e 404.
- [x] 2.3 Integrar os endpoints ao tratamento centralizado sem efeitos colaterais ou chamadas externas; verificar que operações e cotações permanecem inalteradas.

## 3. Testes e documentação

- [x] 3.1 Adicionar testes unitários para compras, vendas, preço médio, patrimônio, lucro/prejuízo, moedas distintas e ausência de cotação; verificar precisão decimal.
- [x] 3.2 Adicionar testes de integração dos endpoints, carteira vazia, ativo vendido e preservação dos dados persistidos; verificar suíte sem internet.
- [x] 3.3 Atualizar README com exemplos de posições/resumo e limites da agregação por moeda; verificar consistência do contrato.
- [x] 3.4 Executar `mvn test`, `mvn verify` e `openspec validate implement-portfolio-position-calculation --strict`; registrar evidências e reconciliar tasks.
