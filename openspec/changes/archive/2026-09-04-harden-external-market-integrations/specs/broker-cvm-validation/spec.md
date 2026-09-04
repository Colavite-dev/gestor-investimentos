## MODIFIED Requirements

### Requirement: Critério de aceitação de corretora perante a CVM
O sistema SHALL considerar todos os registros oficiais com o mesmo CNPJ normalizado antes de decidir a aceitação. A instituição SHALL ser aceita quando existir ao menos um registro ativo cuja categoria seja Corretora ou Distribuidora de títulos e valores mobiliários. O sistema MUST rejeitar o CNPJ quando não existir registro elegível, inclusive quando todos os registros forem inativos, tiverem categoria ausente ou categoria diferente dessas duas famílias, como bancos, custodiantes, depositários, cooperativas, entidades administradoras e escrituradores. A decisão e o registro representativo retornado ao serviço SHALL ser determinísticos e independentes da ordem das linhas do CSV.

#### Scenario: Corretora ou distribuidora ativa
- **WHEN** o dataset possui múltiplos registros para o mesmo CNPJ e ao menos um deles possui situação ativa e categoria de Corretora ou Distribuidora de títulos e valores mobiliários
- **THEN** o sistema considera a instituição aceita perante a CVM, independentemente da posição das linhas no CSV

#### Scenario: Categoria incompatível ou registro inativo
- **WHEN** o CNPJ possui registros, mas nenhum está ativo como Corretora ou Distribuidora de títulos e valores mobiliários
- **THEN** o sistema responde `422 Unprocessable Entity` e não persiste a corretora

#### Scenario: Registros repetidos em ordem diferente
- **WHEN** o mesmo conjunto de registros de um CNPJ é recebido em ordens diferentes no CSV
- **THEN** a decisão de aceitação e o registro representativo são os mesmos
