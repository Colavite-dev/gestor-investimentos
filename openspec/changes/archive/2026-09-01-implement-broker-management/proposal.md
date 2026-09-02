## Why

O projeto já possui persistência PostgreSQL governada por Flyway, mas ainda não possui a primeira funcionalidade de negócio exigida pelo trabalho acadêmico. Esta change estabelece o gerenciamento básico de corretoras e prepara uma base testável para que as validações reais de CNPJ, CEP e autorização no mercado financeiro sejam integradas em mudanças posteriores, sem acoplar o domínio a fornecedores externos ainda não definidos.

## What Changes

- Introduzir a entidade persistente `Corretora` e a primeira migration de negócio do projeto.
- Disponibilizar cadastro básico, listagem, consulta por ID e consulta por CNPJ nos endpoints acadêmicos mínimos.
- Validar o formato e os dígitos verificadores do CNPJ, os campos obrigatórios, e-mail, CEP e UF.
- Impedir CNPJ duplicado no serviço e por restrição única no banco.
- Separar contratos HTTP por DTOs, manter regras no service e centralizar respostas de erro.
- Persistir novos cadastros básicos com `validadaNaCvm=false`; o cliente não poderá declarar validação CVM.
- Cobrir regras de negócio, persistência e endpoints com testes automatizados e manter o perfil H2 apto a executar a migration antes da validação do Hibernate.
- Documentar os endpoints e a limitação desta etapa no README.
- Não integrar APIs reais de CNPJ, CEP ou CVM nesta change e não considerar RF02–RF04/RN01–RN04 concluídos. Essas integrações serão propostas separadamente após seleção e especificação dos providers.

## Capabilities

### New Capabilities

- `broker-management`: Define o cadastro básico e as consultas de corretoras, validações locais, unicidade de CNPJ e respostas HTTP padronizadas.

### Modified Capabilities

Nenhuma.

## Impact

- Banco de dados: nova tabela `corretoras` criada por `V1__create_corretoras.sql`.
- Backend: novos pacotes de entity, repository, service, DTO, mapper, controller, validação e tratamento de exceções.
- API REST: novos endpoints sob `/corretoras`.
- Testes: migration executada no H2 do perfil de teste, além de testes de service, repository e controller.
- Documentação: descrição dos endpoints, códigos de erro e escopo adiado das integrações externas.
- Integrações: nenhuma dependência externa ou contrato de fornecedor será inventado; mudanças futuras deverão usar ports/providers e adapters conforme a arquitetura existente.
