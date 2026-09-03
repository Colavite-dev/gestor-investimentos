## Why

As ações cadastradas armazenam a cotação recebida no momento do cadastro, mas não dispõem da atualização obrigatória de preço e data/hora. Como o mercado já está persistido, o sistema pode reutilizar o provider correto de modo determinístico e sem redescobrir o ativo.

## What Changes

- Adicionar `PUT /acoes/{id}/atualizar-cotacao`, sem body, para atualizar cotação e data/hora de uma ação persistida.
- Reutilizar `StockDataProviderSelector` e o mercado persistido para selecionar brapi ou Twelve Data.
- Evoluir minimamente a abstração de provider para consultar somente dados de cotação, preservando o fluxo de cadastro e evitando descoberta desnecessária no mercado dos Estados Unidos.
- Validar compatibilidade de ticker, moeda, preço e timestamp antes de persistir a atualização.
- Mapear falhas externas e dados incompatíveis para os contratos HTTP já estabelecidos.

## Capabilities

### New Capabilities

- Nenhuma.

### Modified Capabilities

- `stock-management`: Disponibilizar a atualização síncrona de cotação por ID, preservando a identidade e os dados cadastrais do ativo.

## Impact

- Afeta controller, serviço, entidade de ação, abstração/implementações dos providers de ações, tratamento de erros, README e testes.
- Não exige migration nem mudança de schema: `acoes` já contém preço e data/hora da cotação.
