## Context

O estado auditado já implementa os contratos acadêmicos e funcionais aceitos. Os problemas desta change estão distribuídos entre higiene do repositório, documentação, encoding de specs estáveis e dois pontos isolados do cliente React. Por isso, a implementação precisa ser pequena, verificável por diff e incapaz de alterar backend, banco, migrations ou contratos HTTP.

## Goals / Non-Goals

**Goals:**

- Impedir que arquivos locais, credenciais, caches e artefatos gerados sejam incluídos acidentalmente no primeiro staging.
- Restaurar a codificação portuguesa das cinco specs indicadas sem modificar seu conteúdo normativo.
- Tornar as instruções locais verdadeiras e suficientes, sem duplicar a documentação raiz.
- Recuperar a checagem TypeScript de `App.tsx` sem mudar rotas ou renderização.
- Fazer o indicador de conectividade representar alcance real do backend, preservando integralmente `ApiError`, mensagens e tratamento de `401`.
- Validar o backend com o JDK 17 indicado pelo usuário, apenas por variáveis do processo atual.

**Non-Goals:**

- Alterar política de senha, credencial demo, rate limiting, concorrência de cotação/histórico, CORS, open-in-view, bundle, lint, ZIP da CVM ou qualquer comportamento dependente do professor.
- Alterar backend de produção, migrations V1–V8, schema, API, providers, dependências ou identidade `(ticker, mercado)`.
- Fazer staging, commit, push, pull, rebase ou reset.

## Decisions

### 1. O `.gitignore` será ampliado por categorias explícitas

As regras de ambiente usarão `.env` e `.env.*`, seguidas de `!.env.example`, para preservar os templates versionáveis tanto na raiz quanto no frontend. Serão mantidas as exceções existentes de `target` e `build` dentro de árvores de source/test, e adicionados padrões explícitos para IDEs, logs, temporários, caches, bancos locais e artefatos gerados.

Regras excessivamente amplas para extensões usadas pelo projeto não serão adicionadas. Em particular, código, Markdown, JSON, SQL de migration, assets de frontend e diretórios OpenSpec continuarão rastreáveis. Após a edição, `git check-ignore` confirmará destinos sensíveis e exemplos permitidos.

### 2. A correção de mojibake será estritamente textual

Cada ocorrência corrompida nos cinco arquivos será convertida para o caractere português pretendido. Títulos, sentenças normativas, palavras-chave SHALL/MUST, endpoints, números, estrutura Markdown e cenários permanecerão idênticos em significado e ordem. O diff será revisado para garantir que somente sequências de encoding mudaram, e uma busca dirigida por marcadores como `Ã`, `Â`, `â€` e `�` deverá retornar zero nesses arquivos.

### 3. A documentação local será curta e operacional

O README raiz explicará as quatro categorias de variáveis aceitas pelo script sem enumerar valores. A nota da V8 informará que carteira legada sem owner requer tratamento controlado, sem oferecer comando destrutivo. O admin demo será descrito como opção exclusivamente local, desabilitada por padrão e inadequada a ambiente público.

O README do frontend conterá apenas stack, pré-requisito do backend local e comandos npm. A documentação de contratos, banco e integrações continuará centralizada no README raiz e em `docs/`.

### 4. `App.tsx` voltará ao type-check normal sem refatoração preventiva

Primeiro será removida somente a diretiva `@ts-nocheck`. O build revelará incompatibilidades reais; somente essas incompatibilidades poderão ser corrigidas, mantendo imports, árvore de rotas, guards e elementos renderizados funcionalmente equivalentes. Nenhuma reorganização estética acompanhará a correção.

### 5. Uma resposta HTTP prova conectividade, mesmo quando representa erro

O cliente marcará `online=true` sempre que `fetch` produzir um objeto `Response`. Status 400, 401, 403, 404, 409, 422 e também respostas HTTP controladas 5xx continuarão gerando `ApiError` e mensagens existentes, mas não serão classificados como falha de rede. `online=false` ficará restrito ao caminho em que `fetch` rejeita por conectividade ou condição equivalente sem resposta HTTP.

O tratamento atual de `401` com token continuará acionando o listener de sessão. Testes focados verificarão uma resposta de negócio não-2xx, uma resposta bem-sucedida e uma rejeição de rede, além de preservar a mensagem retornada pelo backend.

### 6. Validação Java usará ambiente efêmero de processo

No APPLY, `JAVA_HOME` será apontado para `C:\Users\Cliente\AppData\Local\Programs\Eclipse Adoptium\jdk-17.0.20.101-hotspot` e o respectivo `bin` será prefixado ao `PATH` somente no processo do comando. `java -version` deverá confirmar Java 17 antes de `mvnw.cmd verify`. Nenhuma variável global ou configuração do Windows será alterada.

## Risks / Trade-offs

- **[Regra de ignore esconde arquivo necessário]** → testar arquivos sensíveis e exemplos com `git check-ignore`, além de revisar o status expandido.
- **[Correção de encoding muda requisito]** → limitar o diff a caracteres corrompidos e comparar estrutura, endpoints e palavras normativas antes/depois.
- **[Mudança do indicador mascara backend realmente inacessível]** → manter `online=false` exclusivamente quando não existe resposta HTTP e cobrir esse caminho por teste.
- **[Remoção de `@ts-nocheck` revela erro fora do escopo]** → corrigir somente o erro diretamente bloqueador em `App.tsx`; qualquer necessidade de alteração comportamental será reportada como bloqueio.
- **[JDK 17 indicado não está utilizável]** → interromper a validação, registrar o erro e não substituir por mudança global ou instalação.

## Migration Plan

1. Aplicar as mudanças por grupos pequenos: ignore, encoding/documentação e frontend/testes.
2. Rodar validações focadas após cada grupo e revisar o diff para impedir expansão de escopo.
3. Executar a bateria final solicitada, incluindo JDK 17, OpenSpec estrito, JSON, mojibake, segredos e `git diff --check`.
4. Manter a change ativa após APPLY e aguardar autorização separada para sync/archive.

Não há migration de dados ou deploy. Antes de commit, rollback consiste em reverter somente os arquivos desta change após revisão explícita; nenhuma operação destrutiva será executada automaticamente.
