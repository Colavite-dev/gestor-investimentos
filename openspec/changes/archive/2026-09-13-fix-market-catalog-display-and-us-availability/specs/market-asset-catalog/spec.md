## ADDED Requirements

### Requirement: US catalog eligibility and price feedback

The system SHALL expose only ordinary, tradeable US common-stock instruments from supported US venues in the Twelve Data catalog. It SHALL use the provider's country, type, currency and venue fields as the primary eligibility contract and reject composite or punctuation-bearing symbols generically, without ticker-specific blacklists. The `/stocks` directory SHALL NOT trigger one quote request per item; when it has no current price, the frontend SHALL present that the quote is obtained on tracking rather than imply a failed quote.

#### Scenario: Composite OTC symbol is excluded

- **WHEN** the provider returns a USD US `Common Stock` record with an unsupported venue or a composite symbol such as `!OTC/FLZH`
- **THEN** the catalog excludes it while retaining normal eligible symbols

#### Scenario: Directory item has no current price

- **WHEN** an eligible catalog item has no `cotacaoAtual` from `/stocks`
- **THEN** the page shows concise tracking-time quote feedback without issuing a per-item quote request

## MODIFIED Requirements

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
