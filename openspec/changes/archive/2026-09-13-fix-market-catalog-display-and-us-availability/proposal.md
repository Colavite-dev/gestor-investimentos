## Why

O catálogo brasileiro recebe URLs HTTPS de logo válidas da brapi, mas a página apresenta apenas fallback. O catálogo americano é sinalizado como indisponível quando o processo do backend não recebe uma chave que já existe no `.env`, embora o provider e seus filtros retornem instrumentos reais com a configuração válida.

## What Changes

- Corrigir a apresentação de logos HTTPS válidas fornecidas pela brapi, preservando fallback somente para ausência, URL inválida ou falha real de imagem.
- Preservar o catálogo US real da Twelve Data e corrigir somente a causa confirmada de indisponibilidade, sem lista fixa, dados fake, migration ou mudança de filtros que já foram validados contra o provider real.
- Distinguir no cliente web falha de rede até o backend de respostas HTTP do backend, para que indisponibilidade de provider externo não seja apresentada como backend offline.
- Adicionar testes direcionados ao contrato de logo, catálogo/provider US e classificação de erros.

## Capabilities

### New Capabilities

- Nenhuma.

### Modified Capabilities

- `market-asset-catalog`: explicitar a entrega de logos HTTPS da brapi e a disponibilidade de instrumentos US reais quando a configuração externa válida estiver presente.
- `frontend-backend-integration`: explicitar que respostas HTTP confirmam backend alcançável e que falhas de provider permanecem distintas de falhas de rede.

## Impact

- Filtro de elegibilidade do inventário US e feedback discreto para a ausência deliberada de cotação no diretório, sem chamadas individuais de quote por card.

- Backend de catálogo BR/US e testes de adapters/controlador diretamente afetados.
- Cliente de API e página de Ações, sem redesign visual.
- Nenhuma migration, alteração de autenticação, chave versionada ou integração adicional.
