## Context

O backend já separa providers por port/adapter, pesquisa externamente sem persistir sugestões e persiste uma ação apenas em cadastro/resolução. `POST /operacoes` já recebe e persiste `precoUnitario`; o cálculo de posição usa operações para custo/preço médio e `Acao.cotacaoAtual` para patrimônio. A lacuna está na exposição do catálogo, no preenchimento assistido do formulário e na clareza dos dados exibidos. Veja `proposal.md` para a motivação.

A investigação de 10/09/2026 usou documentação oficial:

- brapi `/api/quote/list`: lista paginada, filtros `search` e `type`, ordenação, `page`/`limit`, metadados de total/próxima página e itens com `stock`, `name`, `close` e `logo`. O plano gratuito informa 15.000 requisições por ciclo, um ticker por chamada de cotação e atraso aproximado de 30 minutos.
- Twelve Data `/stocks`: inventário completo de referência atualizado diariamente, custo documentado de 1 crédito por requisição, com symbol, name, currency, exchange, MIC, country e type. `/symbol_search` retorna até 120 correspondências e não documenta paginação. `/quote` custa 1 crédito por símbolo. O Basic informa 8 créditos/minuto e 800/dia. Logos da Twelve Data não fazem parte desta solução.

## Goals / Non-Goals

**Goals:**

- Disponibilizar catálogo externo navegável sem criar milhares de registros locais.
- Manter consumo previsível de quota e falhas isoladas por provider.
- Oferecer cotação como sugestão editável sem mudar o fato financeiro persistido.
- Explicitar no backend e na UI a separação entre custo e valuation.

**Non-Goals:**

- Sincronizar ou persistir catálogos completos e imagens, ou enriquecer ações US com logos externos.
- Streaming, polling contínuo, refresh token de mercado ou atualização automática de todas as posições.
- Construir série histórica do patrimônio da carteira; isso exigiria reconstrução temporal própria e não é necessário para a comparação atual custo versus patrimônio.
- Alterar autenticação, regras de saldo, migrations V1–V7 ou contratos de criação de operação.

## Decisions

### 1. Novo port de catálogo e contrato paginado comum

Será criado um port dedicado ao catálogo, em vez de sobrecarregar `pesquisar`, porque busca curta e navegação ampla têm contratos, custos e metadados diferentes. `GET /acoes/catalogo` recebe `mercado`, `q` opcional, `page` zero-based na API interna e `size` limitado (padrão 20, máximo 50). A resposta terá `items`, `page`, `size`, `hasNext` e `totalElements` opcional; ausência de total no provider não será convertida em número inventado.

Alternativa rejeitada: transformar `GET /acoes` em catálogo. Isso quebraria o significado atual de ações persistidas e induziria persistência acidental.

### 2. BR usa paginação e enriquecimento nativos da brapi

O adapter usará `/api/quote/list?type=stock&page=...&limit=...`, acrescentando `search` somente quando informado. A página já contém `close` e `logo`, portanto não haverá chamadas por item. Serão aceitos somente itens de ação compatíveis com BRL; URL de logo será retornada apenas se HTTPS. A paginação externa será convertida para o contrato interno, cuidando da diferença entre índice zero interno e página do provider.

### 3. US usa `/stocks` com cache volátil e paginação local

O adapter solicitará o inventário de `Common Stock` dos Estados Unidos e filtrará novamente country/type/USD defensivamente. Como `/stocks` entrega o inventário e não documenta paginação equivalente ao caso BR, o resultado será mantido em memória por até 24 horas e paginado/filtrado localmente. O cache será limitado a esse dataset, thread-safe e descartado ao reiniciar; não será criada tabela.

Não serão chamados `/quote` para cada card. Nenhum fluxo desta change chamará `/logo` da Twelve Data, nem mesmo após seleção, resolução ou uso de uma ação US. Assim, cotação do catálogo US será nula e o frontend usará sempre seu ícone padrão para essas ações. A decisão preserva créditos e evita enriquecimento externo desnecessário; a cotação continuará pontual quando o ativo for usado. `/symbol_search` permanece adequado ao autocomplete por relevância, mas não substitui o catálogo completo porque limita resultados e não pagina.

Alternativa rejeitada: persistir `/stocks` em PostgreSQL. Isso viola o objetivo, adiciona sincronização e migration sem necessidade.

### 4. Seleção da operação reutiliza os fluxos existentes com uma chamada relevante

O frontend já carrega as ações persistidas e recebe sugestões por `(ticker, mercado)`. Ao selecionar:

1. se a identidade está local, seleciona o registro e chama `PUT /acoes/{id}/atualizar-cotacao`;
2. se não está local, chama `POST /acoes/resolver` e reutiliza a cotação retornada, pois a resolução já consultou o provider;
3. mostra loading durante a chamada e, no sucesso, exibe a cotação e sugere o preço;
4. usa um identificador de requisição e um marcador de edição manual para impedir sobrescrita por resposta tardia.

Se a atualização falhar para ação local, a seleção permanece, a cotação de referência é marcada como indisponível e o preço manual continua habilitado. Se o ativo for novo e não puder ser resolvido, ele não pode ser selecionado, pois criar identidade sem validação violaria as regras existentes.

Alternativa rejeitada: novo endpoint de “preparação de operação”. Os endpoints atuais já fornecem resolução e atualização com persistência atômica de cotação/histórico; um orquestrador duplicaria semântica.

### 5. Cotação sugerida nunca substitui o preço executado

O payload de criação não muda. O frontend envia o conteúdo editável de `precoUnitario`, e o backend mantém validação positiva, precisão decimal, transação e saldo não negativo. A cotação atual fica fora de `Operacao` e não altera operações históricas.

### 6. Posição explicita campos derivados e a UI ganha comparação mínima

`PosicaoResponse` acrescentará `cotacaoAtual` e `rentabilidadePercentual`; `ResumoMoedaResponse` acrescentará rentabilidade agregada. O backend continuará sendo a única fonte das fórmulas. A rentabilidade será `(patrimonioAtual - valorInvestido) / valorInvestido * 100`, com escala/arredondamento definidos e nula quando o valuation não existir.

O dashboard manterá o gráfico de distribuição por patrimônio atual e acrescentará uma comparação compacta por posição entre `valorInvestido` e `patrimonioAtual`. Tabelas e resumo mostrarão preço médio, cotação, custo, patrimônio, resultado e rentabilidade. O gráfico de histórico de cotação não mudará de significado.

### 7. Segurança, cache e compatibilidade

O endpoint de catálogo seguirá a autenticação existente. Tokens dos providers permanecem apenas na configuração backend e nunca aparecem em DTOs/logs. Logos não serão proxyadas nem armazenadas: somente URLs HTTPS entregues pela brapi para ações BR poderão ser renderizadas, com política de referência restritiva, fallback e sem HTML injetado. Ações US usarão exclusivamente o ícone padrão empacotado no frontend. Erros seguem o tratamento atual de provider e não disparam retry ilimitado.

Nenhuma dependência nova é necessária: cache TTL simples pode ser implementado com tipos concorrentes/JDK e relógio injetável para testes. Nenhuma migration V8 é necessária porque todos os novos valores são transitórios ou derivados.

## Risks / Trade-offs

- [Planos, quotas e campos dos providers podem mudar] → isolar parsing no adapter, testar contratos e documentar que limites devem ser revisados antes de publicação.
- [Inventário US pode ser grande em memória] → filtrar no provider e defensivamente no backend, guardar somente campos necessários e limitar cache a uma entrada com TTL.
- [Primeira carga US pode ter maior latência] → apresentar loading, timeout e erro recuperável; chamadas seguintes usam cache.
- [Cotação BR pode ser atrasada conforme o plano] → rotular como cotação atual do provider, conservar timestamp e não apresentá-la como preço de execução garantido.
- [Direitos de exibição variam por plano da Twelve Data] → manter o uso acadêmico/local aprovado e revisar licença/plano antes de disponibilização pública.
- [URL brapi de logo pode rastrear ou falhar] → aceitar somente HTTPS, não enviar credenciais, usar `referrerPolicy` restritiva no frontend e fallback imediato; nenhuma URL externa de logo será usada para ações US.
- [Atualizar cotação ao selecionar ação cria histórico] → esse efeito já pertence ao endpoint explícito de atualização e melhora o valuation; não executar atualização em renderização/polling.

## Migration Plan

1. Implementar e testar adapters/port/endpoint de catálogo sem mudar os endpoints atuais.
2. Acrescentar campos derivados compatíveis às respostas de posição/resumo.
3. Integrar catálogo e sugestão de preço no frontend com fallbacks.
4. Executar testes focados, Maven verify, build/lint e validação manual opt-in dos providers.
5. Rollback consiste em remover endpoint/UI/campos adicionais; não há alteração de schema ou dados a desfazer.
