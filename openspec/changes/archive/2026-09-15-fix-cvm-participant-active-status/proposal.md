## Why

O cadastro de corretoras interpreta como ativa somente a situação literal `ATIVO`, mas o campo `SIT` do dataset oficial `cad_intermed.csv` representa participantes operacionais com `EM FUNCIONAMENTO NORMAL`. Como consequência, instituições oficialmente registradas, como a XP Investimentos, são rejeitadas apesar de possuírem um registro compatível de corretora.

## What Changes

- Interpretar `EM FUNCIONAMENTO NORMAL` como a única situação semanticamente ativa no contrato atual do dataset de participantes intermediários da CVM.
- Manter elegíveis somente registros das famílias `CORRETORAS` ou `DISTRIBUIDORAS` e continuar rejeitando `CANCELADA`, `LIQUIDAÇÃO EXTRAJUDICIAL` e situações desconhecidas.
- Preservar a avaliação de todos os registros associados ao mesmo CNPJ e a seleção determinística de um registro elegível quando existir.
- Atualizar fixtures e mocks que representam o campo `SIT` para usar o vocabulário real do CSV.
- Adicionar regressão para o CNPJ `02.332.886/0001-04`, com múltiplas categorias, e fortalecer o teste real opt-in para confirmar a elegibilidade da XP.
- Não adicionar whitelist, hardcode por CNPJ ou exceção específica por instituição.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `broker-cvm-validation`: alinhar o significado de situação ativa ao valor oficial `EM FUNCIONAMENTO NORMAL`, mantendo as restrições de categoria, a avaliação de múltiplos registros e a rejeição segura de situações não reconhecidas.

## Impact

- Produção: somente o predicado compartilhado de elegibilidade CVM.
- Testes: fixtures unitárias e de integração relacionadas à CVM, incluindo regressão da XP e teste real opt-in.
- API pública: nenhum endpoint, payload ou status HTTP será alterado; instituições realmente elegíveis deixarão de receber o falso negativo `422`.
- Persistência e infraestrutura: nenhuma migration, alteração de banco, novo provider ou mudança de configuração.
