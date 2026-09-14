## 1. Catálogo externo e providers

- [x] 1.1 Criar o contrato paginado do catálogo, DTOs opcionais de cotação/exchange e logo restrita a BR, além do port dedicado, verificando serialização, limites de página e identidade `(ticker, mercado)` em testes unitários.
- [x] 1.2 Implementar o catálogo BR sobre `/api/quote/list` com `type=stock`, busca, paginação, cotação e URL HTTPS de logo, verificando mapeamento, filtros, paginação e erros com servidor HTTP simulado.
- [x] 1.3 Implementar o catálogo US sobre `/stocks`, filtragem de Common Stock/United States/USD, cache volátil de 24 horas e paginação local, verificando cache, expiração, concorrência, ambiguidade e indisponibilidade com testes determinísticos.
- [x] 1.4 Garantir que o catálogo US não chama `/quote` por item e que nenhum fluxo US da change chama `/logo`, inclusive pesquisa, seleção, resolução, uso e atualização, além de confirmar que nenhum catálogo persiste ações por meio das interações dos mocks e repositórios.

## 2. API e cálculos de carteira

- [x] 2.1 Implementar `GET /acoes/catalogo` autenticado com validação de `mercado`, `q`, `page` e `size`, verificando respostas BR/US, limites, `401` e tradução segura de falhas em testes de controller/service.
- [x] 2.2 Acrescentar `cotacaoAtual` e `rentabilidadePercentual` à posição e rentabilidade agregada ao resumo, verificando as fórmulas, escala, null de valuation e o exemplo 10×30 + 10×34 com cotação 35.
- [x] 2.3 Reforçar em testes que atualização de cotação altera somente valuation/histórico e não muda preço unitário, custo ou preço médio das operações.
- [x] 2.4 Confirmar que endpoints existentes, autenticação, roles, saldo não negativo e tratamento de erros permanecem compatíveis por meio da suíte backend focada.

## 3. Catálogo e operação no frontend

- [x] 3.1 Adicionar tipos e cliente do catálogo/novos campos de posição, verificando compilação TypeScript e preservação automática do Bearer pelo cliente atual.
- [x] 3.2 Evoluir `/acoes` para distinguir catálogo externo e ativos locais, com mercado, busca, paginação progressiva, loading, erro recuperável, logo brapi/fallback para BR e ícone padrão local obrigatório para US, verificando manualmente que navegar não cria registros nem solicita logo US.
- [x] 3.3 Adaptar a seleção de operação para atualizar cotação de ativo local ou reutilizar a cotação da resolução de ativo novo, verificando que há no máximo uma chamada de mercado relevante por seleção.
- [x] 3.4 Exibir “Cotação atual do mercado”, preencher o preço sugerido sem retirar sua edição, proteger edição manual de respostas tardias e permitir preço manual em falha, verificando total e moeda BRL/USD no modal.
- [x] 3.5 Ajustar tabelas de operações para formatar pela moeda do ativo sem alterar o preço persistido, verificando compras e vendas BR/US.
- [x] 3.6 Ajustar dashboard e detalhe de carteira para mostrar custo, preço médio, cotação, patrimônio, resultado e rentabilidade, além da comparação compacta custo versus valor atual, verificando coerência com os DTOs sem fórmulas de domínio duplicadas.

## 4. Validação e entrega do APPLY

- [x] 4.1 Executar testes backend focados de adapters, catálogo, ações, operações e posições e corrigir somente falhas causadas pela change.
- [x] 4.2 Executar `mvn verify` uma vez próximo ao fim e confirmar que toda a suíte passa com Java 17 e sem testes normais dependentes da internet.
- [x] 4.3 Executar build, TypeScript e lint pelos scripts existentes do frontend e confirmar ausência de erros bloqueantes.
- [x] 4.4 Realizar validação opt-in dos providers reais quando as chaves estiverem disponíveis, cobrindo uma página BR e o inventário/pesquisa US sem imprimir chaves nem fazer carga excessiva.
- [x] 4.5 Validar manualmente catálogo, fallback, cotação sugerida/editável, falha com preço manual e separação visual custo/valuation para USER, incluindo proteção de rotas.
- [x] 4.6 Executar `openspec validate enhance-market-assets-and-operation-pricing --strict` e `git diff --check`, comparar a implementação com todos os deltas e confirmar que V1–V7 e o schema não foram alterados.
