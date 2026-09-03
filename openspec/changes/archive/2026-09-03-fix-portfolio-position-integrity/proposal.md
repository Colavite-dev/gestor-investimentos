# Proposal: Garantir integridade de posições sem venda a descoberto

## Problema

O cadastro de operações atualmente persiste qualquer venda válida em formato, sem verificar a disponibilidade do ativo na carteira. Além disso, `CarteiraPosicaoService` reduz uma venda ao saldo positivo existente durante a leitura. Esse comportamento permite saldo histórico negativo, descarta parte de uma venda e pode ocultar uma operação inválida no cálculo final.

A validação não pode considerar somente o saldo no instante informado: uma venda retroativa pode tornar inválida uma venda posterior já persistida.

## Objetivo

- Impedir venda a descoberto para cada combinação de carteira e ação.
- Aceitar uma venda somente quando toda a sequência cronológica, incluindo a nova operação, mantiver `compras - vendas >= 0` em cada ponto.
- Rejeitar a operação inteira, sem persistência parcial, quando a venda não tiver saldo suficiente — inclusive quando for retroativa.
- Manter o cálculo de posição como projeção somente de operações válidas, sem truncar, ignorar ou mascarar vendas.
- Definir e aplicar ordenação cronológica determinística para operações com o mesmo timestamp.

## Escopo

Incluído:

- regra de negócio de saldo não negativo no registro de `VENDA`;
- consulta ordenada das operações de uma carteira e ação e serialização adequada do registro concorrente;
- erro de negócio centralizado para saldo insuficiente;
- ajuste de `CarteiraPosicaoService` e testes de operações e posições;
- documentação da regra nas delta specs de operações e posições.

Fora do escopo:

- venda a descoberto, reserva de saldo, ordens de mercado, edição ou exclusão de operações;
- novos endpoints, DTOs públicos, migrations ou alterações de schema;
- locking distribuído, filas e processamento assíncrono;
- histórico de cotações e atomicidade entre cotação atual e observações históricas;
- autenticação, frontend ou novas integrações externas.

## Resultado esperado

Operações continuam imutáveis depois de registradas, porém somente uma venda que preserve a invariável histórica é aceita. Para cada carteira e ação, a sequência ordenada terá saldo acumulado não negativo em todos os pontos. Posições mostrarão apenas saldo final positivo; saldo exatamente zero não será exibido como posição atual.
