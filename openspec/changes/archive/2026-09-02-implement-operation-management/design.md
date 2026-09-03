## Context

Carteiras e ações já são entidades persistentes independentes, com IDs estáveis e migrations Flyway. Esta mudança introduz o primeiro fato transacional, sem autenticação ou cálculos derivados.

## Goals / Non-Goals

**Goals:**

- Persistir operações com referências obrigatórias a `Carteira` e `Acao`.
- Expor criação e consultas simples em camadas Controller → Service → Repository.
- Manter valores monetários e quantidades com precisão decimal e validação positiva.
- Garantir integridade referencial no banco e respostas de erro centralizadas.

**Non-Goals:**

- Atualizar saldo, validar posição disponível ou calcular preço médio/patrimônio.
- Alterar ações, carteiras ou cotações durante o cadastro da operação.
- Autenticação, múltiplos usuários, importação de notas ou integrações externas.

## Decisions

- **Modelo:** `Operacao` terá `id`, `carteira`, `acao`, `tipo` (enum `COMPRA`/`VENDA`), `quantidade` (`DECIMAL(19,8)`), `precoUnitario` (`DECIMAL(19,8)`) e `dataOperacao` (`TIMESTAMP WITH TIME ZONE`). Não incluir taxas ou moeda redundante; a moeda é da ação referenciada.
- **Contrato:** `POST /operacoes` recebe `carteiraId`, `acaoId`, `tipo`, `quantidade`, `precoUnitario` e `dataOperacao`; `GET /operacoes/{id}` consulta por ID e `GET /carteiras/{id}/operacoes` lista por data ascendente e ID. Não criar PUT/DELETE.
- **Referências:** o Service verifica existência de carteira e ação antes de salvar. Foreign keys permanecem como proteção contra gravação inconsistente.
- **Transação:** uma transação curta cobre validação das referências e persistência; não há chamadas externas nem locks especiais.
- **Erros:** validação HTTP 400, carteira/ação/operação inexistente 404. Falha de constraint referencial é convertida para erro controlado, sem detalhes SQL.
- **Migration:** criar `V4__create_operacoes.sql`, mantendo V1–V3 imutáveis. Índices em `carteira_id` e `data_operacao` apoiam a listagem.

Alternativas consideradas: aceitar ticker no request (rejeitada porque o ativo mestre já possui ID e resolveria integração novamente) e embutir dados da ação na operação (rejeitada por duplicação e perda de integridade).

## Risks / Trade-offs

- [Sem validação de saldo em vendas] → operações são fatos; uma mudança de posições definirá regras financeiras posteriores.
- [Precisão decimal fixa] → suficiente para o escopo acadêmico; alterações de escala exigirão migration futura.
- [Sem autenticação] → dados pertencem ao contexto único atual; ownership será introduzido com usuários.

## Migration Plan

Aplicar V4 após V1–V3 em banco limpo e existente, verificar foreign keys e índices, e testar rollback apenas em ambiente descartável. Nunca editar migrations aplicadas.

## Open Questions

Nenhuma questão bloqueia a implementação proposta.
