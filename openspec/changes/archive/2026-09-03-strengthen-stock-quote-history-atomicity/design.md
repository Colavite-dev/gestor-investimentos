## Context

See proposal.md for motivation. Hoje `AcaoService.cadastrar` abre transação antes da chamada ao provider e permite construir o serviço sem `CotacaoHistoricaService`; nesse caminho, a ação é gravada sem o histórico inicial. Na atualização, a chamada externa ocorre fora de `AcaoQuoteUpdatePersistenceService.atualizar`, que já reúne a alteração da ação e `historico.registrar` em uma transação, mas não há teste de integração que comprove rollback quando a gravação histórica falha.

## Goals / Non-Goals

**Goals:**

- Tornar o histórico obrigatório nos dois fluxos que aceitam uma cotação externa válida.
- Delimitar uma transação curta que persista conjuntamente a cotação atual e sua observação histórica, sem incluir a chamada ao provider.
- Comprovar a atomicidade contra o banco: em falha de gravação histórica, a ação observada depois da chamada permanece sem a alteração parcial.
- Preservar o endpoint e explicitar seus filtros inclusivos e sua ordenação `dataHoraCotacao ASC, id ASC`.

**Non-Goals:**

- Não introduzir migration, mudar `NUMERIC(19,4)`/`NUMERIC(19,8)`, alterar V2/V5 ou normalizar precisão.
- Não definir resolução de concorrência para respostas externas fora de ordem, nem criar lock, versionamento ou nova API.
- Não alterar providers, contratos HTTP, carteira, operações ou cotações históricas já persistidas.

## Decisions

### Persistência obrigatória e transacional em componente separado

`AcaoService` continuará orquestrando a consulta e validação do provider fora de transação de escrita. Um componente de persistência separado será responsável por iniciar a transação curta tanto para cadastro quanto para atualização, persistindo a ação e chamando o registro histórico obrigatório dentro da mesma fronteira. A dependência de `CotacaoHistoricaService` será obrigatória; será removido o construtor alternativo que a recebe como `null` e a condição que a ignora.

Alternativa descartada: manter a transação em `AcaoService.cadastrar`. Ela inclui a chamada externa e torna a fronteira mais longa que o necessário. Alternativa descartada: registrar o histórico após finalizar a transação da ação. Ela permite o estado parcial que esta change corrige.

### Propagação de falha e rollback verificável

Falhas de persistência do histórico devem propagar como falhas da operação, permitindo o rollback da transação que contém a ação. Os testes de atomicidade usarão contexto Spring e repositório real para observar o estado após a chamada; a falha será injetada somente na camada que grava histórico, não substituindo a transação nem a persistência da ação. O teste verificará que não existe ação recém-cadastrada parcial e, na atualização, que valor e timestamp originais continuam no banco e nenhuma observação nova foi criada.

Alternativa descartada: testes unitários que apenas verificam chamadas de mocks. Eles não demonstram commit/rollback da unidade de persistência.

### Consulta determinística sem mudança de API

O endpoint manterá `GET /acoes/{acaoId}/historico-cotacoes` e filtros ISO-8601 opcionais. Sem filtros retorna toda a coleção; apenas `de` ou apenas `ate` aplica o respectivo limite inclusivo; ambos aplicam intervalo inclusivo. `de > ate` e timestamp malformado retornam `400`; ação ausente retorna `404`. Em qualquer caminho a ordenação é `dataHoraCotacao ASC, id ASC`, inclusive para timestamps iguais.

### Concorrência e precisão como riscos registrados

Atualizações concorrentes podem receber timestamps 10:05 e 10:04 e persistir em ordem inversa, regredindo a cotação atual. A atomicidade não resolve ordenação causal; como uma regra de rejeição/precedência amplia o contrato de atualização e exige decisão de produto, ela será registrada como futura change separada. A divergência entre `acoes.cotacao_atual NUMERIC(19,4)` e `cotacoes_historicas.cotacao NUMERIC(19,8)` também fica explicitamente fora do escopo para uma futura change de precisão/schema.

## Risks / Trade-offs

- [Falha do histórico tornar indisponível uma atualização antes aceita] → é o comportamento de consistência deliberado; a resposta não deve afirmar sucesso sem ambos os registros.
- [Teste de rollback depender de transação de teste] → executar o cenário em contexto Spring sem transação englobando o teste e consultar o banco após a falha.
- [Duplicidade concorrente no cadastro] → manter a proteção existente por restrição e tradução de exceção dentro da fronteira de persistência.
- [Cotação antiga vencer a mais nova em concorrência] → documentar e tratar numa change futura, sem esconder o risco nesta correção.

## Migration Plan

Não há migration nem alteração de dados. Em deploy, a nova consistência passa a valer para novos cadastros e atualizações. Se houver rollback da aplicação, não existe schema para reverter; observações já gravadas permanecem imutáveis.
