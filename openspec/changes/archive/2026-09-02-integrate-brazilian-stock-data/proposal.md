## Why

O cadastro transitório de ações exige que o cliente informe manualmente dados que já são disponibilizados por uma fonte de mercado confiável. A integração com a brapi.dev permite cumprir o requisito acadêmico de validar o ticker e persistir dados reais de ações brasileiras, preservando a separação entre domínio e provider externo.

## What Changes

- Introduzir resolução de dados de ações brasileiras pela API v2 da brapi.dev, usando uma chamada de cotação para validar o símbolo, resolver renomes conhecidos e obter nome, moeda, preço e horário de mercado.
- **BREAKING** Evoluir `POST /acoes` para receber somente `ticker` neste fluxo brasileiro e preencher os atributos cadastrais e de cotação exclusivamente a partir do provider.
- Persistir o ticker canônico retornado pela brapi quando o ticker solicitado tiver sido renomeado, mantendo a unicidade por `(ticker, mercado)`.
- Adicionar configuração externa opcional para `BRAPI_TOKEN`, timeouts e URL base, sem segredos em arquivos rastreados.
- Padronizar erros de ticker não reconhecido, conteúdo incompatível e indisponibilidade/rate limit do provider sem expor detalhes da brapi.
- Manter listagem e consultas existentes; não implementar provider americano nem atualização de cotação nesta mudança.

## Capabilities

### New Capabilities
- `brazilian-stock-data`: Resolução segura de dados reais de ações brasileiras por provider isolado da brapi.dev.

### Modified Capabilities
- `stock-management`: Evolução do cadastro transitório de ações para o fluxo brasileiro baseado somente em ticker e dados resolvidos externamente.

## Impact

- Código afetado: DTO e fluxo de cadastro de ações, service, controller, tratamento centralizado de erros, configuração e testes.
- Nova integração externa: `GET /api/v2/stocks/quote` da brapi.dev, isolada por port/provider e adapter com `RestClient`.
- Configuração: variáveis opcionais `BRAPI_TOKEN`, URL base e timeouts; `.env.example` receberá apenas chaves vazias se a implementação exigir.
- Banco: não há alteração estrutural esperada, pois a entidade e a tabela `acoes` já comportam os dados resolvidos.
