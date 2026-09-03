# Broker CVM Validation Specification

## Purpose

Validar uma corretora no cadastro oficial diário de Participantes Intermediários da CVM antes que ela seja persistida como instituição apta no sistema.

## Requirements

### Requirement: Validação no cadastro oficial de participantes intermediários
Antes de persistir uma corretora, o sistema SHALL consultar os dados cadastrais oficiais mais recentes disponíveis no dataset diário **Participantes Intermediários: Informação Cadastral** da CVM. A consulta SHALL comparar o CNPJ normalizado para 14 dígitos e SHALL permanecer independente do formato ZIP e CSV distribuído pela fonte.

#### Scenario: Participante oficial encontrado pelo CNPJ normalizado
- **WHEN** o cadastro recebe um CNPJ formatado que corresponde a um participante do dataset oficial
- **THEN** o sistema compara o identificador normalizado e usa o registro correspondente para decidir a validação

#### Scenario: Participante não encontrado
- **WHEN** nenhum registro oficial corresponde ao CNPJ normalizado
- **THEN** o sistema responde `422 Unprocessable Entity` e não persiste a corretora

### Requirement: Critério de aceitação de corretora perante a CVM
O sistema SHALL aceitar uma instituição somente quando o registro oficial correspondente estiver ativo e sua categoria de participante for Corretora ou Distribuidora de títulos e valores mobiliários. O sistema MUST rejeitar registro inativo, categoria ausente ou categoria diferente dessas duas famílias, inclusive bancos, custodiantes, depositários, cooperativas, entidades administradoras e escrituradores.

#### Scenario: Corretora ou distribuidora ativa
- **WHEN** o registro oficial possui situação ativa e categoria de Corretora ou Distribuidora de títulos e valores mobiliários
- **THEN** o sistema considera a instituição aceita perante a CVM

#### Scenario: Categoria incompatível ou registro inativo
- **WHEN** o CNPJ é encontrado, mas sua categoria não é Corretora nem Distribuidora de títulos e valores mobiliários, ou sua situação não é ativa
- **THEN** o sistema responde `422 Unprocessable Entity` e não persiste a corretora

### Requirement: Disponibilidade e atualização controlada da fonte CVM
O sistema SHALL reutilizar em memória um snapshot processado do dataset oficial durante uma janela de atualização configurável, evitando novo download e processamento completo a cada cadastro. A primeira consulta após expiração da janela SHALL atualizar o snapshot antes de avaliar o participante.

#### Scenario: Consultas dentro da janela de atualização
- **WHEN** mais de um cadastro consulta a CVM enquanto o snapshot ainda está válido
- **THEN** o sistema reutiliza os dados processados sem baixar novamente o dataset

#### Scenario: Atualização da fonte indisponível
- **WHEN** o snapshot precisa ser atualizado e a fonte CVM apresenta timeout, falha de conexão, indisponibilidade ou limitação externa
- **THEN** o sistema responde `503 Service Unavailable`, não persiste a corretora e não expõe detalhes internos da fonte

### Requirement: Conteúdo externo seguro e compatível
O sistema SHALL rejeitar conteúdo CVM que não possa ser interpretado conforme o contrato do dataset oficial, sem inferir registros ou aprovar participantes por dados parciais.

#### Scenario: Dataset incompatível
- **WHEN** o arquivo recebido não contém a estrutura necessária para identificar CNPJ, situação e categoria do participante
- **THEN** o sistema responde `502 Bad Gateway` e não persiste a corretora
