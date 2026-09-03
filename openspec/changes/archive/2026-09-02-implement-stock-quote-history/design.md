## Context

As ações já armazenam `cotacaoAtual` e `dataHoraCotacao`, e os providers brapi/Twelve Data devolvem dados validados. O histórico deve complementar esse estado atual sem alterar a identidade `(ticker, mercado)` nem introduzir dependência em APIs externas.

## Goals / Non-Goals

**Goals:**

- Persistir cada observação aceita com ação, preço e timestamp da fonte.
- Consultar observações de uma ação com ordenação estável e filtros opcionais.
- Manter a gravação atômica com o cadastro ou atualização que originou a observação.
- Reutilizar os dados já retornados pelos providers, sem uma segunda chamada externa.

**Non-Goals:**

- Scheduler, cache, agregações OHLC, gráficos, rentabilidade ou conversão cambial.
- Alteração manual de observações ou exclusão pelo cliente.
- Novos providers, mercados ou endpoint de atualização de cotação.

## Decisions

- **Tabela própria e append-only:** criar `cotacoes_historicas` com `id`, `acao_id`, `cotacao`, `data_hora_cotacao` e `data_registro`. Uma foreign key para `acoes` preserva integridade; não haverá update/delete público.
- **Snapshot após sucesso externo:** o service grava a observação somente depois de validar preço/timestamp e junto com a atualização da ação. No cadastro inicial, o primeiro snapshot também é criado. Assim nenhum valor fictício entra no histórico.
- **Endpoint dedicado:** `GET /acoes/{id}/historico-cotacoes` retorna DTOs e aceita `de` e `ate` opcionais em ISO-8601. Sem filtros, retorna todas as observações; ação inexistente retorna 404. Ordenação: `data_hora_cotacao` ascendente e `id` ascendente.
- **Sem chamadas adicionais:** a consulta de histórico é somente leitura. Cadastro e atualização reutilizam o modelo interno de cotação já obtido.
- **Precisão e compatibilidade:** usar `NUMERIC(19,8)`/`BigDecimal` e `TIMESTAMP WITH TIME ZONE`; o H2 de testes deverá usar tipos equivalentes já compatíveis com o projeto.
- **Transação:** manter a persistência da ação e do snapshot na mesma transação local, depois da chamada externa; a chamada HTTP ocorre antes de abrir a transação de gravação.

Alternativas consideradas: armazenar uma lista JSON na ação (dificulta consulta e integridade), sobrescrever a cotação atual sem histórico (não atende ao objetivo) e buscar histórico diretamente em providers (não garante disponibilidade nem representa as observações efetivamente usadas pelo sistema).

## Risks / Trade-offs

- [Crescimento indefinido da tabela] → manter registros imutáveis, índices por `acao_id` e timestamp e deixar retenção/agregação para mudança futura.
- [Providers retornarem timestamps repetidos] → permitir observações repetidas e ordenar por `id`, preservando cada atualização aceita.
- [Falha ao gravar histórico] → executar ação e snapshot na mesma transação para evitar sucesso parcial.

## Migration Plan

Criar uma nova migration Flyway após as migrations existentes. Em rollback, remover apenas a nova tabela em ambiente descartável; não alterar migrations já aplicadas.

## Open Questions

Nenhuma questão aberta altera o escopo ou os contratos definidos.
