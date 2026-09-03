# Design: Integridade de posições sem venda a descoberto

## Invariável e validação de venda

Para cada par `Carteira + Ação`, a sequência de operações deve manter, em todos os seus pontos, `saldo acumulado = compras - vendas >= 0`. Uma compra é sempre permitida pelas regras de saldo; uma venda somente é válida se sua inclusão preservar a invariável em toda a sequência.

`OperacaoService` continuará sendo o ponto de orquestração transacional. Após confirmar carteira e ação, ele obterá um bloqueio pessimista na carteira, carregará as operações já persistidas do mesmo par e avaliará uma sequência em memória que inclua a candidata. Caso algum saldo fique negativo, lançará um erro de negócio e não chamará a persistência. O bloqueio é adquirido também para compras para que todo novo registro de uma carteira observe a mesma serialização; ele não altera schema e evita que duas requisições concorrentes validem o mesmo saldo desatualizado.

O repository fornecerá a consulta por carteira e ação já ordenada para essa validação. DTOs e contrato do endpoint permanecem iguais; a regra é de domínio, não de controller.

## Operações retroativas e ordenação determinística

A validação sempre percorre a sequência completa, não apenas o saldo imediatamente anterior à candidata. Logo, uma venda retroativa que deixe qualquer venda posterior com saldo negativo é rejeitada; uma operação retroativa cuja sequência inteira continue não negativa é aceita.

A ordem canônica das operações persistidas é `dataOperacao ASC, id ASC`. O ID é um desempate estável e suficiente para registros persistidos. Como a candidata ainda não possui ID, quando seu timestamp coincidir com registros existentes ela é avaliada depois deles; após persistida, seu ID gerado será maior e a ordem avaliada coincidirá com a ordem definitiva. Não é criado um novo campo de ordenação nem alterado o contrato de operações.

## Cálculo de posição

`CarteiraPosicaoService` continuará somente leitura e usará a mesma ordem determinística. Ele calculará quantidade líquida e custo para operações válidas, sem limitar, truncar ou ignorar uma venda. Como o registro já garante saldo não negativo, uma venda reduz a quantidade e o custo pelo preço médio da posição existente; saldo final zero não gera posição atual. A camada de posição não valida nem corrige operações inválidas; uma violação encontrada em dados legados ou corrompidos deve falhar de forma explícita, não ser escondida.

## Erro e transação

Será introduzida uma exceção de negócio específica, por exemplo `SaldoInsuficienteParaVendaException`, traduzida pelo `GlobalExceptionHandler` para `422 Unprocessable Entity`, mantendo o contrato padronizado `ApiErrorResponse`. A transação de `OperacaoService` deve encerrar sem `save`/`flush` quando a validação falhar, portanto não haverá operação parcialmente persistida.

## Banco e compatibilidade

Não é necessária migration: não haverá saldo persistido, nova tabela, coluna ou alteração das migrations V1–V5. O bloqueio e a consulta adicional usam a infraestrutura JPA já existente.

## Escopo excluído

`stock-quote-history` e a atomicidade entre cotação atual e histórico são removidos desta change. Esse comportamento será tratado futuramente em change OpenSpec própria.
