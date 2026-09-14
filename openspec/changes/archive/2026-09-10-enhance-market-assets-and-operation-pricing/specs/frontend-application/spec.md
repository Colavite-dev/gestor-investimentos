## ADDED Requirements

### Requirement: Tela de Ações apresenta catálogo externo
A rota autenticada `/acoes` SHALL permitir alternar ou distinguir ativos persistidos e catálogo de mercado, filtrar por Brasil ou Estados Unidos, pesquisar e navegar progressivamente. Cada item SHALL exibir ticker, empresa, mercado, moeda, exchange quando disponível e cotação quando fornecida sem enriquecimento excessivo. Ações BR SHALL exibir a logo HTTPS da brapi quando válida e fallback quando ausente ou quebrada; ações US SHALL exibir sempre o ícone padrão do frontend.

#### Scenario: Logo ausente ou quebrada
- **WHEN** uma ação BR não possui logo utilizável ou o carregamento da imagem falha
- **THEN** a interface mostra um fallback estável com identificação textual do ativo

#### Scenario: Ação US exibida
- **WHEN** uma ação US é apresentada em qualquer fluxo desta change
- **THEN** a interface usa o ícone padrão local e não solicita logo externo

#### Scenario: Provider indisponível
- **WHEN** o catálogo externo falha
- **THEN** a tela apresenta erro recuperável e preserva o acesso à lista local sem criar ativos

### Requirement: Formulário de operação apresenta cotação e preço editável
O modal SHALL mostrar um estado de carregamento da cotação após a seleção, o rótulo “Cotação atual do mercado”, o preço unitário editável e o total calculado com o preço digitado. A moeda SHALL corresponder ao ativo selecionado.

#### Scenario: Cotação obtida
- **WHEN** a cotação atual é carregada
- **THEN** ela é exibida como referência e preenche o preço unitário sem bloquear edição posterior

#### Scenario: Cotação indisponível
- **WHEN** não é possível obter a cotação de uma ação selecionável
- **THEN** a interface informa a indisponibilidade e permite preço manual válido

### Requirement: Carteira distingue custo de valor de mercado
O dashboard e o detalhe da carteira SHALL exibir rótulos claros para valor investido, preço médio, cotação atual, patrimônio atual, resultado e rentabilidade quando aplicáveis. A distribuição por ativo SHALL continuar baseada no patrimônio atual, e uma comparação compacta SHALL distinguir custo e patrimônio sem alterar o tema visual existente.

#### Scenario: Usuário consulta uma posição valorizada
- **WHEN** patrimônio atual excede o valor investido
- **THEN** tabela, resumo e gráfico apresentam valores coerentes e identificam o resultado positivo sem tratar a cotação como preço da operação
