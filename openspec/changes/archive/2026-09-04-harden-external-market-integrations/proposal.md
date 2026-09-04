## Why

As integrações de mercado ainda permitem que a brapi troque silenciosamente a identidade de um ticker, classificam alguns erros semanticamente equivalentes da Twelve Data de forma divergente e dependem da última linha do CSV da CVM para CNPJs repetidos. Esses comportamentos podem persistir um ativo diferente do solicitado, retornar um status HTTP enganoso ou alterar a decisão de validação conforme a ordem do dataset.

## What Changes

- Validar de forma estrita a identidade retornada pela brapi: o resultado deverá confirmar o ticker solicitado; respostas que indiquem renomeação, alias ou outro símbolo não serão aceitas automaticamente.
- Classificar respostas e falhas da brapi por semântica, preservando `422` para ticker não encontrado, `502` para conteúdo externo inválido/incompatível e `503` para indisponibilidade, limite ou autenticação/configuração externa.
- Unificar a classificação da Twelve Data para erros HTTP e erros estruturados no corpo, sem expor credencial nem conteúdo remoto sensível.
- Tornar determinística a decisão CVM para múltiplas linhas do mesmo CNPJ, priorizando a existência de registro ativo elegível de Corretora ou Distribuidora e eliminando dependência da ordem do CSV.
- Ampliar testes isolados dos adapters e de mapeamento HTTP; testes reais continuarão opt-in e independentes de `mvn verify`.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `brazilian-stock-data`: endurecer a confirmação da identidade do ticker e a classificação semântica de falhas da brapi.
- `us-stock-data`: tornar consistente a classificação semântica de respostas de erro da Twelve Data.
- `broker-cvm-validation`: definir decisão determinística de aceitação para múltiplos registros CVM do mesmo CNPJ.

## Impact

- Código afetado: adapters e DTOs internos de brapi, Twelve Data e CVM; possivelmente exceções existentes e seus testes, sem vazamento de DTO externo para controllers ou domínio.
- API pública: não haverá endpoint novo nem mudança de payload de sucesso; alguns casos externos ambíguos passarão a falhar com o status centralizado apropriado em vez de serem aceitos ou classificados de modo inconsistente.
- Configuração: serão preservados os timeouts explícitos e as credenciais por variáveis de ambiente; não haverá migration, retry automático, novo provider ou chamada externa em transação de escrita.
