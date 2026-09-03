## Context

O projeto utiliza Spring Boot, JPA, PostgreSQL e Flyway, com H2 no perfil de testes. Corretoras já seguem Controller → Service → Repository, DTOs separados, mapper e handler de exceções centralizado. Esta change cria o cadastro mestre de ativos previsto no PRD; veja `proposal.md` para a motivação e a delta spec para o contrato.

## Goals / Non-Goals

**Goals:**

- Persistir ações de Brasil e Estados Unidos com identidade composta por ticker e mercado.
- Definir uma API de cadastro útil antes de existirem providers reais, sem falsificar integração externa.
- Manter a evolução para resolução de ticker e cotação isolada do domínio e dos contratos de leitura.

**Non-Goals:**

- Implementar provider BR/EUA, `TickerResolver`, `QuoteProvider`, atualização de cotação, relação com corretora, operação, carteira, preço médio, patrimônio, autenticação ou dashboard.
- Modelar FIIs, ETFs, criptomoedas, renda fixa, fundos ou outros mercados.

## Decisions

### Ativo mestre sem corretora relacionada

`Acao` representará o instrumento negociado, não a relação entre um usuário, uma corretora e uma compra. Por isso, `corretoraRelacionada` não será incluída: uma mesma ação pode ser negociada por várias corretoras, e essa associação pertence à futura Operação.

Alternativa considerada: vínculo opcional com Corretora. Foi descartada porque induziria duplicação do mesmo ativo e confundiria cadastro mestre com posição.

### Identidade composta, enums e normalização

O modelo terá `Mercado` (`BRASIL`, `ESTADOS_UNIDOS`) e `Moeda` (`BRL`, `USD`) como enums persistidos por nome. O service normalizará ticker removendo espaços externos e convertendo para maiúsculas; a moeda será derivada exclusivamente do mercado. O banco reforçará unicidade de `(ticker, mercado)`.

Alternativa considerada: ticker globalmente único e moeda enviada pelo request. Foram descartadas por colisão possível entre mercados e por permitir combinações incoerentes.

### Contrato manual transitório de criação

Até as próximas changes de resolução e cotação, `POST /acoes` receberá `ticker`, `nomeEmpresa`, `mercado`, `cotacaoAtual` e `dataHoraCotacao`. O cliente fornece explicitamente esses valores de bootstrap; `moeda` é somente resposta derivada. Quando providers reais existirem, uma change própria poderá evoluir o comando para resolver dados principalmente a partir do ticker, preservando a identidade e os endpoints de leitura.

Alternativa considerada: armazenar cotação e empresa nulas. Foi descartada porque o requisito acadêmico exige armazenar dados do ativo e cotação, e criaria registros incompletos sem fonte oficial.

### Busca obrigatória por ticker sem perda de determinismo

`GET /acoes/ticker/{ticker}` será mantido. Um query parameter opcional `mercado` selecionará a identidade completa. Quando omitido, a busca retorna somente se o ticker existir em exatamente um mercado; duas correspondências retornam 400, sem escolher um mercado por heurística.

Alternativa considerada: sempre assumir Brasil ou devolver uma lista. Foi descartada porque ambas mudariam silenciosamente o significado do endpoint obrigatório.

### Migração incremental e compatível com testes

Uma nova migration `V2__create_acoes.sql` criará `acoes` com `id`, `ticker`, `nome_empresa`, `mercado`, `moeda`, `cotacao_atual` (`NUMERIC(19,4)`), `data_hora_cotacao` (`TIMESTAMP WITH TIME ZONE`) e constraint única `(ticker, mercado)`. Check constraints restringirão os valores de mercado e moeda aos enums iniciais. A sintaxe será compatível com PostgreSQL e H2 em modo PostgreSQL.

## Risks / Trade-offs

- [Dados manuais podem estar desatualizados] → o contrato os declara transitórios; a atualização real será entregue somente com provider confiável.
- [Ticker idêntico em mercados distintos] → unicidade composta e erro explícito de ambiguidade sem `mercado`.
- [Novos mercados exigirão alteração de enum/check] → uma change futura criará nova migration, sem modificar V2.
- [Precisão de cotação futura pode exceder quatro casas] → `NUMERIC(19,4)` é adequado ao escopo inicial; uma evolução exigirá migration explícita se o provider demandar maior precisão.

## Migration Plan

1. Adicionar `V2__create_acoes.sql`, sem modificar `V1__create_corretoras.sql`.
2. Validar a migration com H2 na suíte automatizada e com PostgreSQL na validação consolidada da implementação.
3. O rollback da aplicação não exige exclusão automática da tabela; qualquer reversão estrutural futura será uma migration deliberada.
