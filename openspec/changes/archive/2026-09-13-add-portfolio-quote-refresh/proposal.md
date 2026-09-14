## Why

As cotações usadas pelo resumo e pelas posições são persistidas e podem ficar desatualizadas. Hoje a pessoa precisa atualizar cada ativo individualmente, mesmo quando a carteira contém posições abertas em BRL e USD. Isso deixa o valuation com valor atual igual ao custo mesmo quando o mercado já mudou.

## What Changes

- Adicionar uma atualização explícita, centralizada por carteira, em `PUT /carteiras/{id}/atualizar-cotacoes`.
- Atualizar uma vez cada ativo com posição líquida aberta e quantidade positiva, selecionando brapi para `BRASIL` e Twelve Data para `ESTADOS_UNIDOS` por meio da lógica existente de atualização unitária.
- Retornar o resultado agregado da execução, incluindo quantidades atualizadas, falhas e tickers com falha sem detalhes internos.
- Adicionar na tela de detalhe da carteira o botão **Atualizar cotações**, seus estados de carregamento, sucesso, sucesso parcial e erro, e o recarregamento dos dados dependentes após uma execução com ao menos um sucesso.
- Preservar o histórico de cotações e todas as regras atuais de operações, custo, preço médio, cálculo financeiro, autenticação e isolamento por usuário.

## Capabilities

### Modified Capabilities

- `portfolio-management`: expor a ação explícita e autorizada de atualização em lote das cotações de posições abertas de uma carteira.
- `stock-management`: reutilizar a atualização de cotação existente por ativo como a única forma de consultar provider e persistir cada resultado da ação em lote.
- `frontend-application`: oferecer o comando de atualização no detalhe da carteira e apresentar seu resultado sem redesign.

## Impact

- Backend: controller e serviço de carteira, DTO de resultado e testes de autorização, seleção/deduplicação e falha parcial.
- Frontend: módulo de API, tipos, detalhe da carteira e testes dos estados de interação e recarga.
- Sem migration, alteração de banco, alteração de providers, mudança de fórmulas ou alteração de endpoints GET.
