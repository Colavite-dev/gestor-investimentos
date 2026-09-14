## Context

Veja `proposal.md` para a motivação. `TwelveDataStockAdapter` atualmente filtra ticker exato, ação comum dos Estados Unidos e USD, mas rejeita qualquer lista com tamanho diferente de um. A investigação real de AAPL mostrou os pares válidos `NASDAQ`/`XNGS` e `IEX`/`IEXG`; a cotação de AAPL respondeu normalmente. A persistência, a identidade `(ticker, mercado)` e a consulta de cotação já são independentes da descoberta depois que a ação existe.

## Goals / Non-Goals

**Goals:**

- Transformar uma lista de matches elegíveis em uma seleção determinística e independente da ordenação da API.
- Permitir venues principais conhecidos e um fallback IEX estritamente limitado, preservando erro seguro quando não houver um vencedor único.
- Diferenciar mensagem de ambiguidade de mensagem de ticker inexistente sem mudar status, DTOs ou contratos REST.
- Validar o comportamento no adapter e manter a resolução idempotente para uma ação já persistida.

**Non-Goals:**

- Alterar `/stocks`, `StockCatalogProvider`, cache de catálogo, timeouts ou a disponibilidade do catálogo americano.
- Adicionar exchange ou MIC à entidade, DTOs, identidade lógica, banco ou migrations.
- Alterar `/quote`, o parser da cotação, frontend, autenticação, providers brasileiros ou dependências.

## Decisions

### 1. Prioridade por pares completos de exchange e MIC

A seleção será feita após os filtros existentes de ticker, país, tipo e moeda. Ela classificará cada match por um par conhecido, em vez de usar o índice no payload:

1. venues principais: `NASDAQ`/`XNGS`, `NYSE`/`XNYS` e `NYSE American`/`XASE`;
2. fallback alternativo: `IEX`/`IEXG`;
3. demais pares: sem prioridade reconhecida.

Se houver exatamente um match principal, ele vence. Na ausência de principal, um único IEX vence. Dois matches na maior prioridade aplicável, ou somente matches sem prioridade reconhecida, resultam em ambiguidade. A validação requer os dois valores do par para evitar considerar principal uma resposta parcialmente ou incoerentemente identificada.

Alternativas consideradas:

- Primeiro resultado retornado: rejeitado porque a ordem da Twelve Data não é contrato estável.
- Persistir venue/MIC e alterar a identidade do ativo: rejeitado porque expande modelo e contratos além da necessidade; a identidade aprovada permanece `(ticker, mercado)`.
- Aceitar qualquer resultado elegível: rejeitado porque voltaria a tornar a escolha arbitrária.

### 2. Erro dedicado de ambiguidade de venue

Uma ambiguidade restante será representada por uma exceção de domínio específica, mapeada pelo handler central para 422 com mensagem sanitizada. Ticker inexistente e instrumento incompatível continuarão usando sua classificação atual. Isto preserva o status REST, mas evita a afirmação incorreta de que um ticker válido não existe.

### 3. Cotação continua após descoberta escolhida

Após seleção do instrumento, `consultar` seguirá executando uma única chamada `/quote` com o ticker normalizado e aplicará o parser/validação existentes. `consultarCotacao` de ação US persistida não executará discovery e não será modificado.

### 4. Testes são determinísticos e externos permanecem opt-in

Mocks do adapter cobrirão combinações de venues e ordem invertida. Testes de service/controller confirmarão resolução e idempotência sem internet. O teste real existente pode ser atualizado apenas se continuar opt-in e nunca exibir a chave.

## Risks / Trade-offs

- [A Twelve Data pode introduzir novos venues principais] → pares desconhecidos permanecem ambíguos de modo seguro até decisão explícita.
- [Dois registros repetidos do mesmo venue principal podem aparecer] → a regra conserva 422, evitando deduplicação silenciosa de dados externos potencialmente inconsistentes.
- [IEX como único resultado pode representar cobertura alternativa] → é aceito somente quando nenhum venue principal existe; a cotação continua sujeita às validações atuais.
- [Mensagem mais específica pode alterar assertions de clientes] → status e payload permanecem iguais; testes cobrirão a nova mensagem controlada.

## Migration Plan

1. Introduzir a classificação de venue e a exceção sanitizada no adapter/handler.
2. Ajustar testes unitários e de controller/service para os cenários de seleção.
3. Executar a suíte Maven e validação OpenSpec.

Não há migration, alteração de dados ou rollout de frontend. Rollback consiste em reverter somente os arquivos Java/testes desta change.
