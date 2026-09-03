## Context

O núcleo de corretoras e ações já segue Controller → Service → Repository, com DTOs separados de entidades e Flyway no PostgreSQL. O PRD define Carteira como conjunto de investimentos, mas Operação e cálculos pertencem a evoluções posteriores.

## Goals / Non-Goals

**Goals:**

- Criar uma entidade independente `Carteira` com nome, descrição opcional, ID e data de cadastro.
- Expor `POST /carteiras`, `GET /carteiras` e `GET /carteiras/{id}`.
- Garantir validação de entrada e unicidade do nome normalizado na aplicação e no banco.
- Preparar o modelo para futuros vínculos por Operação sem acoplar Carteira a Acao nesta mudança.

**Non-Goals:**

- Usuários, autenticação ou ownership multiusuário.
- Operações de compra/venda, posições, preço médio, patrimônio, rentabilidade ou dashboard.
- Integrações externas e alterações em corretoras ou ações.

## Decisions

- **Modelo mínimo:** `id` gerado, `nome` obrigatório, `descricao` opcional e `dataCadastro` gerada pelo servidor. Não incluir campo `ativa` sem requisito atual.
- **Nome como identidade:** normalizar com trim e comparação case-insensitive e impor unicidade. Isso evita carteiras indistinguíveis no contexto atual de usuário único; uma futura autenticação poderá revisar a chave para `(usuário, nome)`.
- **Persistência:** criar `V3__create_carteiras.sql`; migrations V1/V2 permanecem imutáveis. A coluna `nome_normalizado` suporta a constraint única sem depender apenas do Service.
- **Camadas:** seguir Controller → Service → Repository, com `CarteiraRequest`/`CarteiraResponse`, mapper e exceções próprias. O controller não conhece JPA.
- **Contrato inicial:** somente os três endpoints de cadastro e consulta. Atualização e remoção ficam fora para não inventar ciclo de vida antes de Operação.
- **Erros:** Bean Validation e JSON inválido resultam em 400; conflito de unicidade em 409; ID ausente em 404, usando o tratamento centralizado existente.

Alternativas consideradas: permitir nomes duplicados (rejeitada por ambiguidade para um único usuário) e associar diretamente ações à carteira (rejeitada porque compras/vendas serão representadas por Operação).

## Risks / Trade-offs

- [Nome único global] → adequado ao contexto sem autenticação; futura multiusuário deve migrar a constraint para incluir usuário.
- [Sem atualização/remoção] → mantém escopo pequeno; endpoints podem ser adicionados em change posterior sem quebrar o cadastro.
- [Migration V3 em banco existente] → aplicar Flyway de forma incremental e testar banco limpo; rollback consiste em remover a versão apenas antes de produção, sem editar V1/V2.

## Migration Plan

Adicionar V3, executar testes de migration em banco limpo e validar criação da tabela/constraint. Em caso de falha antes de dados de produção, corrigir criando nova migration; não modificar versões aplicadas.

## Open Questions

Nenhuma decisão pendente bloqueia a implementação.
