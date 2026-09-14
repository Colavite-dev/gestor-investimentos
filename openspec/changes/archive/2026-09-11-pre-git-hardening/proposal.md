## Why

O pente-fino pré-Git identificou inconsistências concretas de higiene do repositório, texto corrompido, documentação local e dois defeitos pontuais no frontend. Corrigi-las antes do primeiro staging reduz o risco de publicar artefatos locais, documentação enganosa ou comportamento incorreto, sem ampliar o produto nem alterar contratos.

## What Changes

- Ampliar preventivamente o `.gitignore`, preservando exemplos de ambiente e todos os arquivos versionáveis do projeto.
- Corrigir somente caracteres corrompidos em cinco specs estáveis, sem mudar requisitos ou cenários.
- Ajustar o README principal para descrever corretamente o script local, o cuidado com legado na V8 e o uso exclusivamente local do administrador demo.
- Substituir o README boilerplate do frontend por instruções curtas e específicas do Adapt Invest.
- Remover `@ts-nocheck` de `frontend/src/App.tsx` e corrigir apenas incompatibilidades TypeScript diretamente reveladas por essa remoção.
- Fazer respostas HTTP válidas do backend deixarem de indicar indisponibilidade da API, mantendo a sinalização offline para falhas reais de rede/conectividade e preservando os erros de negócio.
- Acrescentar ou ajustar testes focados e validar backend com JDK 17, frontend, OpenSpec, JSON, encoding, segredos e whitespace.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

Nenhuma. A correção do status da API apenas alinha a implementação aos requisitos estáveis de `frontend-backend-integration`; os demais itens são documentação, encoding e higiene do repositório. A change usa `skip_specs: true` e não cria delta funcional.

## Impact

- Arquivos previstos: `.gitignore`, `README.md`, `frontend/README.md`, `frontend/src/App.tsx`, `frontend/src/api/client.ts`, testes frontend focados e as cinco specs estáveis com mojibake.
- Não há alteração de backend de produção, banco, migrations V1–V8, contratos HTTP, dependências, identidade `(ticker, mercado)` ou regras acadêmicas.
- A validação final usará o JDK 17 local somente no processo atual e não alterará configuração global do Windows.
