## 1. Documentação de entrega

- [x] 1.1 Atualizar `README.md` com stack, execução, migrations V1–V8, segurança, isolamento e todos os endpoints reais; verificar que não restam afirmações contraditórias sobre frontend, autenticação, dashboard ou usuários.
- [x] 1.2 Atualizar `docs/product/PRD.md` e `docs/architecture/ARCHITECTURE.md` para refletirem frontend React, JWT/BCrypt/roles, ownership e bancos; verificar aderência aos controllers, configurações e migrations atuais.
- [x] 1.3 Atualizar `docs/architecture/entity-model.md` com USUARIO, `CARTEIRA.usuario_id`, unicidade por usuário e os relacionamentos reais; verificar contra V1–V8 e entidades JPA.
- [x] 1.4 Atualizar `docs/decisions/DECISIONS.md` somente com decisões já implementadas e `docs/integrations/external-apis.md` com fluxos e endpoints externos reais; verificar que ambiguidades permanecem neutras e que não há segredos.

## 2. Coleção Postman

- [x] 2.1 Atualizar `docs/api/gestor-investimento.postman_collection.json` com Auth, Admin e os endpoints existentes de pesquisa, catálogo e resolução; verificar cobertura de todos os mappings de controllers.
- [x] 2.2 Configurar variável de coleção `token`, autenticação Bearer herdada nos requests protegidos e script pós-login seguro; validar o JSON e confirmar ausência de token ou segredo real.

## 3. Verificação da entrega

- [x] 3.1 Executar `mvn verify`, `npm test`, `npm run build` e `npm run lint`; registrar resultados e distinguir warnings não bloqueantes.
- [x] 3.2 Executar `openspec validate prepare-academic-delivery --strict`, `openspec validate --all --strict` e `git diff --check`; validar links internos, JSON Postman e ausência de referências obsoletas ou segredos.
