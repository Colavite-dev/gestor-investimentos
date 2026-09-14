## 1. Bootstrap administrativo seguro

- [x] 1.1 Substituir username e senha versionados do bootstrap por propriedades externas `APP_DEMO_ADMIN_USERNAME` e `APP_DEMO_ADMIN_PASSWORD`, mantendo `APP_DEMO_ADMIN_ENABLED` opt-in e desabilitada por padrão; verificar que nenhum fallback administrativo é criado.
- [x] 1.2 Preservar criação idempotente, validação de papel existente e hash BCrypt para credenciais externas válidas; verificar com testes de configuração do bootstrap.
- [x] 1.3 Tratar configuração habilitada com username/password ausentes, vazios ou inválidos de forma segura, sem persistir admin e sem registrar senha; verificar cenário negativo automatizado.

## 2. Ambiente e documentação executável

- [x] 2.1 Atualizar `application.properties`, `.env.example` e `scripts/start-local.ps1` somente com as novas variáveis permitidas, sem valores secretos e sem imprimir credenciais; verificar carregamento local por inspeção e testes afetados.
- [x] 2.2 Atualizar a coleção Postman para substituir credenciais administrativas fixas por variáveis apropriadas, preservando 26 requests e o fluxo de token; validar o JSON e a contagem de requests.
- [x] 2.3 Atualizar a spec estável por meio do delta OpenSpec e verificar que a spec normativa não contém senha administrativa fixa; manter archives inalterados e classificar ocorrências remanescentes como históricas não funcionais.

## 3. Limpeza de assets confirmados

- [x] 3.1 Confirmar ausência de imports, URLs CSS e referências HTML para `frontend/public/icons.svg` e `frontend/src/assets/hero.png`, removê-los e verificar que logo, símbolo e favicon permaneçam presentes e referenciados.

## 4. Validação final

- [x] 4.1 Executar os testes de bootstrap atualizados e `./mvnw.cmd verify` com JDK 17; verificar sucesso sem depender de internet ou credenciais externas reais.
- [x] 4.2 Executar `npm test`, `npm run build` e `npm run lint` em `frontend/`; verificar sucesso sem recriar assets removidos.
- [x] 4.3 Executar `openspec validate secure-demo-admin-before-git --strict`, `openspec validate --all --strict` e `git diff --check`; verificar sucesso.
- [x] 4.4 Buscar a antiga credencial demo sem imprimi-la; verificar que quaisquer ocorrências restantes pertencem exclusivamente a archives ou fixtures históricas não funcionais e que nenhum segredo novo é candidato a commit.
