## ADDED Requirements

### Requirement: Resposta explicita custo e valuation separadamente
Cada posição SHALL expor `precoMedio`, `valorInvestido`, `cotacaoAtual`, `patrimonioAtual`, `lucroPrejuizo` e `rentabilidadePercentual`. `precoMedio` e `valorInvestido` SHALL ser derivados exclusivamente das operações; `patrimonioAtual` SHALL ser quantidade multiplicada pela cotação atual persistida; resultado e rentabilidade SHALL comparar patrimônio atual com valor investido.

#### Scenario: Duas compras e cotação superior
- **WHEN** existem compras de 10 unidades a 30 e 10 unidades a 34, e a cotação atual é 35
- **THEN** a posição informa quantidade 20, preço médio 32, valor investido 640, patrimônio atual 700 e resultado 60

#### Scenario: Cotação muda após as operações
- **WHEN** somente a cotação atual de uma ação é atualizada
- **THEN** patrimônio, resultado e rentabilidade podem mudar, mas preço médio e valor investido permanecem iguais

#### Scenario: Cotação indisponível
- **WHEN** a posição não possui cotação atual válida
- **THEN** os campos de valuation dependentes da cotação permanecem indisponíveis sem alterar custo ou preço médio

### Requirement: Resumos e gráficos não reescrevem fatos históricos
Resumos e gráficos SHALL rotular distintamente custo/investimento e valor atual e MUST NOT substituir preços unitários históricos por cotações atuais. Uma comparação gráfica entre custo e patrimônio SHALL usar os campos calculados da posição, sem refazer regras financeiras no frontend.

#### Scenario: Dashboard compara custo e valor atual
- **WHEN** o dashboard apresenta uma posição com cotação atual válida
- **THEN** a visualização distingue valor investido de patrimônio atual e apresenta resultado coerente com a diferença
