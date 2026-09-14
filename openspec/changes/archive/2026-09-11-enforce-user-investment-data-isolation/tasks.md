## 1. Inventário e migration de ownership

- [x] 1.1 Reconfirmar em modo read-only que os alvos locais continuam sendo somente as carteiras IDs 1 e 2 com sete operações e, conforme autorização já concedida, remover exclusivamente essas operações e carteiras; verificar depois que usuários, ações, cotações, banco e volume foram preservados.
- [x] 1.2 Criar somente `V8__add_carteira_ownership.sql` com `usuario_id`, FK, `NOT NULL`, unique `(usuario_id, nome_normalizado)` e índice `(usuario_id, id)`, sem editar V1–V7, e verificar o diff das migrations.
- [x] 1.3 Verificar que a V8 não possui placeholder, default, backfill ou exclusão automática e testar migration vazia e falha/rollback quando houver carteira legada sem owner.
- [x] 1.4 Atualizar o mapeamento JPA `Usuario`–`Carteira` preservando `Operacao` sem `usuario_id` redundante e verificar `ddl-auto=validate` em H2 e PostgreSQL real quando a decisão de legado permitir.

## 2. Identidade e persistência com escopo

- [x] 2.1 Introduzir resolução reutilizável do ID estável a partir de `Authentication.getName()`, sem ler identidade de DTO/request, e cobrir principal válido e inválido com testes focados.
- [x] 2.2 Substituir no `CarteiraRepository` listagem, consulta, existência, duplicidade e lock globais por queries scoped por `usuario.id`, verificando em teste repository que registros de outro owner não são retornados.
- [x] 2.3 Adicionar ao `OperacaoRepository` consulta por operação e owner da carteira, preservando a ordenação e as queries financeiras existentes após autorização, e verificar com dois owners.
- [x] 2.4 Adaptar criação de carteira para associar o usuário autenticado e tratar unicidade concorrente por owner, verificando nomes iguais entre usuários e conflito dentro da mesma conta.

## 3. Autorização dos fluxos financeiros

- [x] 3.1 Adaptar `GET /carteiras`, `GET /carteiras/{id}` e `POST /carteiras` para o ID autenticado, verificando listagem isolada, criação própria e `404` para ID alheio.
- [x] 3.2 Adaptar `POST /operacoes` para obter a carteira com lock e owner na mesma query antes de consultar sequência ou persistir, verificando que USER_B não cria compra/venda na carteira de USER_A.
- [x] 3.3 Adaptar `GET /operacoes/{id}` e `GET /carteiras/{id}/operacoes` para owner, verificando `404` indistinguível para inexistente e alheio.
- [x] 3.4 Adaptar posições e resumo para autorizar a carteira antes do cálculo e usar somente suas operações, verificando `404` para USER_B e preservação integral das fórmulas financeiras.
- [x] 3.5 Confirmar por testes que ADMIN continua acessando `/admin/users` e `/admin/metrics`, mas recebe `404` nos endpoints comuns de investimento para carteira alheia.

## 4. Fronteira global e estado frontend

- [x] 4.1 Preservar `Acao`, catálogo, cotação e histórico como dados globais e testar que dois usuários compartilham a mesma PETR3 sem compartilhar operações, quantidade, custo médio ou valuation.
- [x] 4.2 Vincular os carregamentos de Dashboard, detalhe e Operações à identidade ativa, limpar estados financeiros em logout/troca e descartar respostas obsoletas, verificando que USER_B nunca renderiza dados previamente carregados por USER_A.
- [x] 4.3 Confirmar por busca estática e teste do cliente que nenhum request de carteira/operação envia `usuarioId`, owner ou username como autorização.

## 5. Testes de segurança e validação final

- [x] 5.1 Criar teste E2E/backend com USER_A criando carteira e compra PETR3 e USER_B falhando em listar/acessar carteira, operação, posições, resumo e criação de operação, verificando os status e ausência de dados privados.
- [x] 5.2 Atualizar testes unitários e de controller existentes para owners explícitos e executar testes focados de Carteira, Operação e Posição sem reduzir a cobertura das regras de saldo, precisão, preço médio e valuation.
- [x] 5.3 Executar `mvn verify` uma vez perto do fim e os testes/build/lint frontend, registrando totais e corrigindo somente falhas causadas pela change.
- [x] 5.4 Validar PostgreSQL real sem apagar banco ou volume, confirmar schema/constraints e fluxos USER_A/USER_B; depois executar strict OpenSpec e `git diff --check`.
