## Context

`start-local.ps1` já possui um único parser restrito do `.env`: ele lê apenas chaves de uma allowlist, registra o valor anterior no ambiente do processo, injeta os valores durante a execução do Spring Boot e os restaura no `finally`. `BRAPI_TOKEN` e `TWELVE_DATA_API_KEY` estão documentadas em `.env.example`, mas não pertencem à allowlist atual.

## Goals / Non-Goals

**Goals:**

- Fazer as duas credenciais de provider seguirem exatamente o mesmo ciclo temporário das credenciais já aceitas pelo script.
- Demonstrar que o processo iniciado pelo script consegue consumir a integração BR autenticada e a integração americana sem expor segredo.

**Non-Goals:**

- Alterar adapters, propriedades Spring, contratos REST, catálogo, cache, timeout, parser de quote, persistência, frontend ou banco.
- Criar um segundo parser de `.env`, registrar valores de credenciais ou tornar as chaves obrigatórias no script.

## Decisions

### Estender a allowlist existente

Adicionar somente `BRAPI_TOKEN` e `TWELVE_DATA_API_KEY` à coleção `$allowedKeys`.

Alternativa considerada: carregar todas as chaves do `.env`. Rejeitada porque reduziria o controle explícito do ambiente propagado ao processo e alteraria o modelo de segurança já adotado pelo script.

### Reutilizar injeção e restauração atuais

As novas chaves serão percorridas pelos mesmos loops que guardam valores originais, definem variáveis de processo e restauram o ambiente no `finally`.

Alternativa considerada: definir variáveis diretamente na linha que inicia Maven. Rejeitada porque duplicaria o fluxo, dificultaria a restauração e aumentaria o risco de expor credenciais.

## Risks / Trade-offs

- [Credencial ausente ou inválida no `.env`] → O script mantém o comportamento de propagar ausência; os adapters continuam classificando autenticação/indisponibilidade conforme a implementação existente.
- [Falha conhecida de `/stocks` da Twelve Data] → A validação distingue a presença da chave no processo do timeout externo; não altera timeout nem catálogo nesta change.
