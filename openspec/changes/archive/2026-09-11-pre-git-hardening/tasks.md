## 1. Higiene do repositório

- [x] 1.1 Ampliar `.gitignore` para ambientes, builds, IDEs, logs, temporários, caches e bancos locais, preservando `.env.example`, código, migrations, specs e documentação; verificar o diff das regras.
- [x] 1.2 Executar `git check-ignore` para confirmar que `.env`, backups e artefatos gerados são ignorados e que `.env.example` e `frontend/.env.example` permanecem rastreáveis.

## 2. Encoding e documentação

- [x] 2.1 Corrigir exclusivamente o mojibake nos cinco arquivos estáveis indicados e verificar por busca dirigida que nenhum marcador de texto corrompido permanece neles, sem alterar estrutura ou significado normativo.
- [x] 2.2 Atualizar `README.md` com a descrição verdadeira das categorias carregadas por `start-local.ps1`, a cautela não destrutiva da V8 e o uso exclusivamente local do admin demo; verificar que nenhum valor secreto foi incluído.
- [x] 2.3 Substituir `frontend/README.md` pelo guia curto do Adapt Invest com stack, backend local e comandos npm solicitados, verificando que não duplica os contratos da documentação raiz.

## 3. Correções frontend focadas

- [x] 3.1 Remover `@ts-nocheck` de `frontend/src/App.tsx`, corrigir somente eventuais erros TypeScript diretamente resultantes e verificar que rotas, guards e árvore visual permanecem funcionalmente iguais.
- [x] 3.2 Ajustar `frontend/src/api/client.ts` para marcar offline somente quando não houver resposta HTTP por falha de rede/conectividade, preservando `ApiError`, mensagens de negócio e logout controlado em `401`.
- [x] 3.3 Adicionar ou ajustar teste focado para resposta HTTP não-2xx, sucesso e rejeição de rede, verificando que apenas a última produz status offline e que o erro HTTP original continua disponível.

## 4. Validação final

- [x] 4.1 Configurar `JAVA_HOME` e `PATH` somente no processo atual para o JDK 17 indicado, confirmar `java -version` e executar `mvnw.cmd verify` com sucesso.
- [x] 4.2 Executar `npm test`, `npm run build` e `npm run lint`, registrando testes, erros e warnings sem corrigir itens fora de escopo.
- [x] 4.3 Validar `pre-git-hardening` e todas as specs com OpenSpec estrito, validar o JSON do Postman e repetir a busca de mojibake e de possíveis segredos sem imprimir valores.
- [x] 4.4 Executar `git diff --check` e revisar o diff final para confirmar que backend de produção, banco, migrations V1–V8, contratos HTTP, regras acadêmicas e demais itens excluídos não foram alterados, sem fazer staging, commit ou push.
