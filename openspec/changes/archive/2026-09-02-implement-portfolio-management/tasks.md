## 1. Persistência e modelo

- [x] 1.1 Criar a entidade `Carteira` com id, nome, nome normalizado, descrição e data de cadastro; verificar mapeamento JPA e separação do domínio de `Acao`.
- [x] 1.2 Criar `V3__create_carteiras.sql` com tipos, timestamps, limites e constraint única para nome normalizado; verificar aplicação em PostgreSQL/H2 sem alterar V1/V2.
- [x] 1.3 Implementar `CarteiraRepository` com consultas por ID e nome normalizado; verificar testes de persistência e conflito de unicidade.

## 2. Contratos e regras de negócio

- [x] 2.1 Criar `CarteiraRequest`, `CarteiraResponse` e mapper sem expor entidade JPA; verificar serialização dos campos públicos.
- [x] 2.2 Implementar validação de nome/descrição, normalização e geração de data de cadastro; verificar cenários válidos e inválidos da spec.
- [x] 2.3 Implementar service de criação, listagem e consulta por ID, convertendo duplicidade para erro de negócio; verificar testes unitários sem dependências externas.

## 3. API e erros

- [x] 3.1 Implementar `POST /carteiras`, `GET /carteiras` e `GET /carteiras/{id}` com códigos 201, 200 e 404 conforme a spec; verificar testes de controller.
- [x] 3.2 Integrar conflitos e validações ao tratamento centralizado, sem vazar detalhes de persistência; verificar respostas 400, 404 e 409.

## 4. Qualidade e documentação

- [x] 4.1 Adicionar testes de integração entre controller, service e repository, incluindo nome duplicado concorrente quando suportado pelo ambiente; verificar suíte sem internet.
- [x] 4.2 Atualizar README com o contrato dos endpoints de carteira e o escopo inicial sem operações; verificar exemplos consistentes com os DTOs.
- [x] 4.3 Executar `mvn test`, `mvn verify` e validação OpenSpec strict; registrar evidências e manter esta lista reconciliada.
