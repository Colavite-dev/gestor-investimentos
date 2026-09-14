## Why

A Twelve Data pode retornar o mesmo ticker de ação comum dos Estados Unidos em mais de um venue legítimo. Hoje o adapter exige uma única correspondência elegível e converte `AAPL` em 422, embora o ticker e a cotação sejam válidos. É necessário selecionar um venue de maneira explícita, estável e auditável, sem escolher pela ordem da resposta externa.

## What Changes

- Definir uma prioridade determinística de venue, baseada no par `exchange` e `mic_code`, para a descoberta de ações US.
- Preferir um único venue principal explicitamente reconhecido; aceitar IEX somente como fallback quando for a única alternativa elegível restante.
- Manter ambiguidade sem desempate seguro como erro 422, com mensagem que indique ambiguidade em vez de ticker inexistente.
- Preservar a consulta de cotação e a persistência já existentes após a seleção bem-sucedida.
- Cobrir ordem invertida, venue principal versus IEX, fallback alternativo e empate não resolvível com testes determinísticos.

## Capabilities

### New Capabilities

- Nenhuma.

### Modified Capabilities

- `us-stock-data`: substituir a exigência de correspondência externa única por seleção determinística de venue para ações US, preservando rejeição segura de ambiguidades restantes.

## Impact

- Afeta `TwelveDataStockAdapter`, exceção/mensagem de ambiguidade e testes do adapter, service e controller de ações.
- `POST /acoes/resolver` poderá resolver ticker US válido com venue principal identificado; DTOs, formato REST e identidade `(ticker, mercado)` não mudam.
- Não afeta `/stocks`, catálogo, cache, timeouts, frontend, cotação, banco, migrations ou dependências.
