## Context

Carteiras, ações e operações já possuem tabelas e APIs. Operações registram compra/venda, quantidade, preço unitário e data; ações mantêm cotação atual e moeda. A nova capacidade deve ser somente leitura.

## Goals / Non-Goals

**Goals:**

- Calcular posições em memória a partir das operações da carteira.
- Expor posições por ação e resumo com valores separados por moeda.
- Reutilizar repositories existentes e manter o domínio livre de detalhes HTTP.

**Non-Goals:**

- Alterar operações ou persistir snapshots de posição.
- Conversão cambial, histórico, scheduler, dashboard ou chamadas a providers.

## Decisions

- **Algoritmo:** processar operações em ordem de data/ID; compras aumentam quantidade e custo total, vendas reduzem quantidade pelo preço médio vigente. Preço médio é custo restante dividido pela quantidade restante; venda sem saldo não falha nesta mudança, mas pode produzir quantidade zero e resultado controlado.
- **Valores:** `valorInvestido` é o custo da quantidade líquida; `patrimonioAtual` usa `Acao.cotacaoAtual`; `lucroPrejuizo` é a diferença. Valores sem cotação são `null`, nunca zero fictício.
- **Moedas:** posições carregam moeda da ação. O resumo agrega por moeda em mapa/coleção, evitando conversão implícita entre BRL e USD.
- **Endpoints:** `GET /carteiras/{id}/posicoes` e `GET /carteiras/{id}/resumo`; ambos retornam 404 para carteira ausente e 200 para coleção vazia.
- **Persistência:** nenhuma migration; consulta das operações existentes e cálculo em serviço. Índices atuais são suficientes para o escopo acadêmico.

Alternativas consideradas: persistir posições em tabela (rejeitada por duplicar dados e exigir sincronização) e converter USD para BRL (rejeitada por falta de fonte cambial nesta mudança).

## Risks / Trade-offs

- [Muitas operações] → cálculo linear por consulta; paginação/ pré-agregação pode ser criada quando houver requisito de escala.
- [Venda sem saldo] → não bloqueada para preservar operações como fatos; regra de consistência financeira fica para evolução específica.
- [Cotações desatualizadas] → resultado representa o último valor persistido; atualização usa endpoint já existente.

## Migration Plan

Nenhuma migration. Validar apenas que as consultas e cálculos funcionam com o schema atual.

## Open Questions

Nenhuma questão bloqueia a implementação.
