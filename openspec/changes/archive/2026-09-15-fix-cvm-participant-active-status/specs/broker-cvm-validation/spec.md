## MODIFIED Requirements

### Requirement: Critério de aceitação de corretora perante a CVM
O sistema SHALL considerar todos os registros oficiais com o mesmo CNPJ normalizado antes de decidir a aceitação. No campo `SIT` do dataset `cad_intermed.csv`, a situação semanticamente ativa SHALL corresponder ao valor normalizado `EM FUNCIONAMENTO NORMAL`. A instituição SHALL ser aceita quando existir ao menos um registro nessa situação cuja categoria pertença às famílias Corretora ou Distribuidora de títulos e valores mobiliários. O sistema MUST rejeitar o CNPJ quando não existir registro elegível, inclusive quando todos os registros estiverem `CANCELADA`, `LIQUIDAÇÃO EXTRAJUDICIAL`, possuírem situação ausente ou desconhecida, tiverem categoria ausente ou categoria diferente dessas duas famílias, como bancos, custodiantes, depositários, cooperativas, entidades administradoras e escrituradores. A decisão e o registro representativo retornado ao serviço SHALL ser determinísticos e independentes da ordem das linhas do CSV. A elegibilidade MUST NOT depender de whitelist, CNPJ ou nome específico de instituição.

#### Scenario: Corretora ou distribuidora ativa
- **WHEN** o dataset possui múltiplos registros para o mesmo CNPJ e ao menos um deles possui situação ativa e categoria de Corretora ou Distribuidora de títulos e valores mobiliários
- **THEN** o sistema considera a instituição aceita perante a CVM, independentemente da posição das linhas no CSV

#### Scenario: Categoria incompatível ou registro inativo
- **WHEN** o CNPJ possui registros, mas nenhum está ativo como Corretora ou Distribuidora de títulos e valores mobiliários
- **THEN** o sistema responde `422 Unprocessable Entity` e não persiste a corretora

#### Scenario: Registros repetidos em ordem diferente
- **WHEN** o mesmo conjunto de registros de um CNPJ é recebido em ordens diferentes no CSV
- **THEN** a decisão de aceitação e o registro representativo são os mesmos

#### Scenario: Situação operacional oficial da CVM
- **WHEN** um registro possui `SIT = EM FUNCIONAMENTO NORMAL` e categoria da família `CORRETORAS` ou `DISTRIBUIDORAS`
- **THEN** o sistema interpreta o registro como ativo e elegível

#### Scenario: Situação cancelada, em liquidação ou desconhecida
- **WHEN** um registro possui `SIT = CANCELADA`, `SIT = LIQUIDAÇÃO EXTRAJUDICIAL`, situação ausente ou qualquer situação não reconhecida
- **THEN** o sistema considera esse registro inelegível, mesmo que sua categoria pertença às famílias Corretora ou Distribuidora

#### Scenario: Regressão da XP com múltiplas categorias
- **WHEN** o CNPJ normalizado `02332886000104`, correspondente à XP INVESTIMENTOS CCTVM S.A., possui registros em múltiplas categorias, incluindo um registro `CORRETORAS` com `SIT = EM FUNCIONAMENTO NORMAL`
- **THEN** o sistema considera a instituição elegível porque existe pelo menos um registro compatível, sem depender de regra específica para a XP
