## ADDED Requirements

### Requirement: Cotação atual é apenas sugestão de preço da operação
Ao selecionar uma ação persistida ou resolvida no formulário, o sistema SHALL tentar obter sua cotação atual e apresentá-la separadamente como referência. O preço unitário SHALL ser preenchido com essa cotação somente como sugestão e SHALL permanecer editável; o valor enviado no `POST /operacoes` SHALL ser o preço efetivamente persistido.

#### Scenario: Usuário aceita a sugestão
- **WHEN** a cotação é obtida e o usuário envia o formulário sem alterar o preço sugerido
- **THEN** a operação é persistida com esse preço unitário

#### Scenario: Usuário substitui a sugestão
- **WHEN** a cotação é 32,47 e o usuário altera o preço unitário para 30,00
- **THEN** a operação é persistida com preço unitário 30,00

#### Scenario: Usuário edita enquanto a cotação carrega
- **WHEN** o usuário informa manualmente um preço antes da resposta assíncrona
- **THEN** a resposta tardia não sobrescreve o preço manual

### Requirement: Falha de cotação não bloqueia preço manual
Quando uma ação já puder ser identificada localmente e a cotação estiver indisponível, o formulário SHALL manter a ação selecionada, informar a falha e permitir o registro com um preço manual positivo. A falha MUST NOT substituir a cotação persistida por zero ou nulo.

#### Scenario: Provider falha para ação local
- **WHEN** a atualização de cotação falha após a seleção de uma ação persistida
- **THEN** o usuário pode preencher um preço positivo e registrar a operação normalmente

#### Scenario: Novo ativo não pode ser resolvido
- **WHEN** um ativo ainda não persistido não pode ser validado pelo provider
- **THEN** o sistema não inventa nem persiste sua identidade e informa que o ativo não pôde ser selecionado
