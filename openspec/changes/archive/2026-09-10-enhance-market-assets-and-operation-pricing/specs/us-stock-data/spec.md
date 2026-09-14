## ADDED Requirements

### Requirement: Catálogo americano usa dados de referência da Twelve Data
O adapter americano SHALL usar `/stocks` como fonte do inventário, filtrando `country=United States`, `type=Common Stock` e moeda USD. SHALL mapear symbol, name, currency, exchange e MIC quando disponíveis, sem tratar o banco local como catálogo.

#### Scenario: Catálogo US válido
- **WHEN** a Twelve Data retorna seu inventário de ações
- **THEN** somente Common Stocks dos Estados Unidos em USD são disponibilizadas e paginadas pela aplicação

#### Scenario: Mesmo ticker em exchanges incompatíveis
- **WHEN** metadados não permitem identificar de forma segura uma ação US elegível
- **THEN** o item ambíguo ou incompatível é descartado em vez de receber identidade inventada

### Requirement: Ações US não usam serviço externo de logo
O sistema MUST NOT chamar `/logo` da Twelve Data nem outro serviço externo de logo para ações US em qualquer fluxo desta change, incluindo catálogo, pesquisa, seleção, resolução, uso e atualização. O catálogo MUST omitir `logoUrl`, e o frontend SHALL usar somente seu ícone padrão. O sistema também MUST NOT chamar `/quote` individualmente para cada item do catálogo; o inventário de referência MAY ser mantido em cache volátil, e cotações SHALL continuar sendo consultadas pontualmente nos fluxos de uso ou atualização do ativo.

#### Scenario: Página US exibida
- **WHEN** uma página contém vários ativos americanos
- **THEN** ela é produzida a partir do inventário de referência sem chamada de cotação por item e sem qualquer chamada de logo

#### Scenario: Ação US selecionada ou utilizada
- **WHEN** uma ação US é pesquisada, selecionada, resolvida, utilizada ou atualizada
- **THEN** a integração pode usar os endpoints de dados e cotação previstos, mas nunca consulta `/logo`

#### Scenario: Cache expira
- **WHEN** o cache volátil do inventário expira
- **THEN** uma consulta posterior pode recarregar `/stocks`, sem persistir o catálogo em tabelas da aplicação
