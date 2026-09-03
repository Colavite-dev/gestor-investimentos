## 1. Coleção da API

- [x] 1.1 Criar a coleção Postman v2.1 em `docs/api/gestor-investimento.postman_collection.json`, com variável `baseUrl` não sensível e sem credenciais reais.
- [x] 1.2 Adicionar requests de corretoras e ações cobrindo cadastro, listagem, consultas, atualização de cotação, validação, inexistência e duplicidade, verificando caminhos e contratos no README.

## 2. Diagrama e instruções

- [x] 2.1 Criar `docs/architecture/entity-model.md` com diagrama Mermaid das entidades `Corretora` e `Acao`, campos essenciais, chaves, identidade `(ticker, mercado)` e ausência de relacionamento obrigatório.
- [x] 2.2 Atualizar o README com links, formato e instruções para importar a coleção e visualizar o diagrama, verificando que nenhum segredo foi adicionado.

## 3. Validação dos entregáveis

- [x] 3.1 Validar que a coleção é JSON importável, que todos os endpoints obrigatórios estão representados e que os artefatos não contêm credenciais ou tokens reais. Evidência: JSON importado com sucesso, 17 requests e nenhum valor secreto encontrado.
- [x] 3.2 Executar a validação OpenSpec strict e reconciliar as tasks com os artefatos efetivamente entregues. Evidência: `Change 'complete-academic-deliverables' is valid`.
