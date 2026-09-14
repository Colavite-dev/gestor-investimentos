## ADDED Requirements

### Requirement: Catálogo brasileiro usa a listagem paginada da brapi
O adapter brasileiro SHALL consultar a listagem oficial da brapi com `type=stock`, encaminhar busca e paginação suportadas e aceitar somente ações elegíveis do mercado brasileiro em BRL. SHALL mapear `stock`, `name`, `close` e `logo` para ticker, nome, cotação e logo quando esses valores válidos estiverem presentes.

#### Scenario: Página BR com enriquecimento nativo
- **WHEN** a brapi retorna uma página válida contendo cotação e logo
- **THEN** o sistema retorna esses dados na mesma página sem chamadas adicionais por símbolo

#### Scenario: Paginação BR
- **WHEN** a brapi informa `currentPage`, `totalPages`, `totalCount` e `hasNextPage`
- **THEN** o sistema preserva metadados equivalentes para a navegação do cliente

#### Scenario: Item não elegível
- **WHEN** a resposta contém fundo, BDR ou item incompatível com ação brasileira em BRL
- **THEN** o item não integra o catálogo de ações BR
