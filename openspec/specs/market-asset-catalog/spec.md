# market-asset-catalog Specification

## Purpose

Define um catálogo externo consultável de ações brasileiras e americanas sem importar em massa instrumentos para a persistência da aplicação.

## Requirements

### Requirement: Catálogo externo autenticado e paginado

O sistema SHALL expor `GET /acoes/catalogo` somente a usuários autenticados, exigindo `mercado`, aceitando `q`, `page` e `size`, e retornando uma página de ativos obtida do provider do mercado solicitado. Cada item SHALL identificar `ticker`, `nomeEmpresa`, `mercado`, `moeda` e `exchange` quando disponível, além de `cotacaoAtual` opcional. `logoUrl` SHALL ser opcional e permitido somente para ação BR quando fornecido pela brapi com URL HTTPS válida; a URL válida SHALL ser preservada pelo contrato até o navegador. Para Estados Unidos, quando a configuração externa válida da Twelve Data estiver disponível no processo do backend, o catálogo SHALL retornar instrumentos reais elegíveis do provider, sem lista fixa ou dados fabricados.

#### Scenario: Página de catálogo solicitada
- **WHEN** um usuário autenticado solicita uma página válida para um mercado suportado
- **THEN** o sistema retorna apenas itens desse mercado e metadados suficientes para avançar ou encerrar a paginação

#### Scenario: Busca no catálogo
- **WHEN** o usuário fornece `q`
- **THEN** o sistema filtra por ticker ou nome conforme a capacidade do provider sem consultar o banco como fonte do catálogo

#### Scenario: Acesso sem autenticação
- **WHEN** um cliente sem autenticação solicita o catálogo
- **THEN** o sistema responde `401`

#### Scenario: Logo HTTPS da brapi é entregue
- **WHEN** a brapi fornece logo HTTPS válida para um ativo BR do catálogo
- **THEN** o item da resposta contém a mesma URL HTTPS utilizável sem persistir logo na entidade `Acao`

#### Scenario: Catálogo US configurado consulta provider real
- **WHEN** um usuário autenticado solicita catálogo US e a chave da Twelve Data está disponível no processo do backend
- **THEN** a resposta contém instrumentos reais elegíveis retornados pelo provider e não uma lista fixa local

### Requirement: US catalog eligibility and price feedback

The system SHALL expose only ordinary, tradeable US common-stock instruments from supported US venues in the Twelve Data catalog. It SHALL use the provider's country, type, currency and venue fields as the primary eligibility contract and reject composite or punctuation-bearing symbols generically, without ticker-specific blacklists. The `/stocks` directory SHALL NOT trigger one quote request per item; when it has no current price, the frontend SHALL present that the quote is obtained on tracking rather than imply a failed quote.

#### Scenario: Composite OTC symbol is excluded
- **WHEN** the provider returns a USD US `Common Stock` record with an unsupported venue or a composite symbol such as `!OTC/FLZH`
- **THEN** the catalog excludes it while retaining normal eligible symbols

#### Scenario: Directory item has no current price
- **WHEN** an eligible catalog item has no `cotacaoAtual` from `/stocks`
- **THEN** the page shows concise tracking-time quote feedback without issuing a per-item quote request

### Requirement: Consulta sem persistência automática

O sistema MUST NOT criar ou atualizar entidades `Acao` apenas porque um ativo foi listado, filtrado, paginado ou visualizado no catálogo. A persistência SHALL ocorrer somente pelos fluxos explícitos já previstos de cadastro, resolução, uso ou atualização de um ativo.

#### Scenario: Usuário navega por várias páginas
- **WHEN** o usuário visualiza páginas do catálogo sem selecionar um ativo para uso
- **THEN** nenhuma ação do catálogo é persistida no PostgreSQL

### Requirement: Metadados opcionais e fallback

O catálogo SHALL tratar cotação e exchange como metadados opcionais. Para ação BR, `logoUrl` só SHALL ser exposta quando vier da brapi e usar HTTPS; sua ausência ou falha de carregamento MUST NOT impedir a apresentação do ativo. Para ação US, `logoUrl` MUST ser omitida e nenhuma consulta externa de logo SHALL ocorrer.

#### Scenario: brapi não fornece logo válido
- **WHEN** uma ação BR não contém uma URL HTTPS de logo válida da brapi
- **THEN** a resposta omite `logoUrl` e a interface apresenta fallback visual sem download ou persistência binária

#### Scenario: Ação US no catálogo
- **WHEN** qualquer ação US é retornada pelo catálogo
- **THEN** a resposta omite `logoUrl` e a interface usa o fallback padrão sem consultar serviço externo de logo

#### Scenario: Catálogo sem cotação por item
- **WHEN** obter cotação individual para todos os itens excederia a estratégia de consumo aprovada
- **THEN** os itens são retornados sem `cotacaoAtual` e continuam utilizáveis para pesquisa e seleção

### Requirement: Falhas e limites do provider

O sistema SHALL limitar `size`, aplicar timeout e traduzir indisponibilidade, quota excedida ou resposta externa inválida para o padrão de erro existente, sem expor chaves ou payloads sensíveis. O cliente MUST NOT repetir automaticamente chamadas sem limite.

#### Scenario: Provider indisponível
- **WHEN** o provider do mercado falha ou recusa a chamada por quota
- **THEN** o sistema informa indisponibilidade de forma segura e não altera ações persistidas
