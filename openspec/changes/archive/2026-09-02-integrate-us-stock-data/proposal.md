## Why

O cadastro de ações resolve apenas ativos brasileiros pela brapi, embora o sistema já modele o mercado dos Estados Unidos e USD. É necessário integrar uma fonte externa apropriada para cadastrar ações americanas com dados reais, preservando a identidade por ticker e mercado.

## What Changes

- Adicionar resolução de ações americanas pela Twelve Data, com validação de que o instrumento é uma ação comum negociada nos Estados Unidos antes da persistência.
- Evoluir o cadastro de `POST /acoes` para receber `ticker` e `mercado`, usando o mercado como seletor explícito do provider e mantendo os demais dados exclusivamente sob responsabilidade da fonte externa.
- **BREAKING**: o cadastro brasileiro deixa de aceitar body somente com `ticker`; passa a exigir `mercado: "BRASIL"` para selecionar a brapi de modo determinístico.
- Reutilizar a abstração interna de dados de ações e introduzir uma seleção de provider por mercado, sem acoplar a camada de serviço aos adapters brapi ou Twelve Data.
- Configurar a chave da Twelve Data exclusivamente por `TWELVE_DATA_API_KEY`, sem segredos em arquivos rastreados, respostas ou logs.

## Capabilities

### New Capabilities

- `us-stock-data`: Resolução e validação de ações comuns dos Estados Unidos pela Twelve Data, com dados externos consistentes, autenticação segura e falhas controladas.

### Modified Capabilities

- `stock-management`: Evoluir o contrato de cadastro para selecionar explicitamente o mercado e encaminhar a resolução ao provider correspondente.

## Impact

- Afeta o request de `POST /acoes`, o serviço de ações, a abstração de dados de mercado, tratamento centralizado de erros, configuração e testes.
- Adiciona adapter HTTP da Twelve Data com consultas de descoberta e cotação; a integração brapi permanece responsável apenas pelo mercado Brasil.
- Não requer migration, alteração de entidade ou mudança no schema: os enums e a tabela atuais já suportam `ESTADOS_UNIDOS` e `USD`.
