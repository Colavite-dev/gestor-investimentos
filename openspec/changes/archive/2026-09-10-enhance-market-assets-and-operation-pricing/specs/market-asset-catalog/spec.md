## Purpose

Define um catálogo externo consultável de ações brasileiras e americanas sem importar em massa instrumentos para a persistência da aplicação.

## ADDED Requirements

### Requirement: Catálogo externo autenticado e paginado
O sistema SHALL expor `GET /acoes/catalogo` somente a usuários autenticados, exigindo `mercado`, aceitando `q`, `page` e `size`, e retornando uma página de ativos obtida do provider do mercado solicitado. Cada item SHALL identificar `ticker`, `nomeEmpresa`, `mercado`, `moeda` e `exchange` quando disponível, além de `cotacaoAtual` opcional. `logoUrl` SHALL ser opcional e permitido somente para ação BR quando fornecido pela brapi.

#### Scenario: Página de catálogo solicitada
- **WHEN** um usuário autenticado solicita uma página válida para um mercado suportado
- **THEN** o sistema retorna apenas itens desse mercado e metadados suficientes para avançar ou encerrar a paginação

#### Scenario: Busca no catálogo
- **WHEN** o usuário fornece `q`
- **THEN** o sistema filtra por ticker ou nome conforme a capacidade do provider sem consultar o banco como fonte do catálogo

#### Scenario: Acesso sem autenticação
- **WHEN** um cliente sem autenticação solicita o catálogo
- **THEN** o sistema responde `401`

### Requirement: Consulta sem persistência automática
O sistema MUST NOT criar ou atualizar entidades `Acao` apenas porque um ativo foi listado, filtrado, paginado ou visualizado no catálogo. A persistência SHALL ocorrer somente pelos fluxos explícitos já previstos de cadastro, resolução, uso ou atualização de um ativo.

#### Scenario: Usuário navega por várias páginas
- **WHEN** o usuário visualiza páginas do catálogo sem selecionar um ativo para uso
- **THEN** nenhuma ação do catálogo é persistida no PostgreSQL

### Requirement: Metadados opcionais e fallback
O catálogo SHALL tratar cotação e exchange como metadados opcionais. Para ação BR, `logoUrl` só SHALL ser exposta quando vier da brapi e usar HTTPS; sua ausência ou falha de carregamento MUST NOT impedir a apresentação do ativo. Para ação US, `logoUrl` MUST ser omitida e nenhuma consulta externa de logo SHALL ocorrer.

#### Scenario: brapi não fornece logo válido
- **WHEN** uma ação BR não contém uma URL HTTPS de logo válida da brapi
- **THEN** a resposta omite `logoUrl` e a interface apresenta fallback visual sem fazer download ou persistência binária

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
