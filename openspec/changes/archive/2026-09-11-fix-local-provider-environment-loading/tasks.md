## 1. Propagação de ambiente local

- [x] 1.1 Incluir `BRAPI_TOKEN` e `TWELVE_DATA_API_KEY` na allowlist existente de `scripts/start-local.ps1` e verificar que o script continua usando o mesmo parser, injeção temporária e restauração.
- [x] 1.2 Confirmar que `.env.example` já documenta somente os nomes esperados das duas variáveis, sem alterar valores reais nem criar chaves novas.

## 2. Validação

- [x] 2.1 Iniciar o backend por `scripts/start-local.ps1` e verificar, sem expor segredos, que uma resolução BR autenticada e uma pesquisa AAPL não falham por credencial ausente.
- [x] 2.2 Consultar o catálogo US pelo backend iniciado pelo script e distinguir a presença da chave de uma eventual falha de `/stocks` por timeout.
- [x] 2.3 Executar `mvn verify` e `openspec validate fix-local-provider-environment-loading --strict` e registrar os resultados sem sincronizar ou arquivar a change.
