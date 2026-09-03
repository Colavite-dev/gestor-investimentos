## Why

O histórico de cotações já existe, mas o cadastro de ação pode omiti-lo silenciosamente em alguns usos de `AcaoService`, e não há evidência de rollback quando a gravação histórica falha durante uma atualização. Isso permite que a cotação atual e sua trilha auditável deixem de representar o mesmo fato externo.

## What Changes

- Tornar obrigatória a gravação da observação histórica inicial no cadastro de ação com dados externos válidos.
- Garantir que a atualização da cotação atual e a gravação da mesma cotação no histórico sejam uma unidade transacional curta, com rollback integral quando qualquer persistência falhar.
- Adicionar cobertura de integração que prove consistência, rollback e coerência de valor/timestamp no cadastro e na atualização.
- Completar a cobertura do endpoint de histórico para filtros opcionais, limites inclusivos, erros de intervalo/formato e ordenação determinística por timestamp e ID.
- Documentar como risco futuro a concorrência de atualizações recebidas fora de ordem e a diferença de precisão entre cotação atual e histórico; esta change não altera schema, migrations nem a política de precisão.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `stock-quote-history`: tornar o vínculo entre cotação atual e observação histórica atomicamente obrigatório e explicitar a cobertura de consulta determinística.

## Impact

Afeta `AcaoService`, `AcaoQuoteUpdatePersistenceService`, `CotacaoHistoricaService`, seus repositórios e testes de serviço/integração/controller. Os endpoints existentes e providers externos são preservados; chamadas ao provider continuam fora da transação curta de persistência. Não haverá migrations, mudanças em `V2`/`V5`, novas APIs externas nem mudanças de precisão numérica.
