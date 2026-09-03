## Why

O cadastro de corretoras já obtém o endereço pela fonte oficial de CNPJ, mas ainda não verifica se o CEP cadastral existe nem consegue complementar lacunas seguras do endereço. A consulta ViaCEP atende ao requisito acadêmico de integração externa de CEP sem substituir a origem empresarial dos dados.

## What Changes

- Consultar ViaCEP após a obtenção válida dos dados cadastrais de CNPJ, usando o CEP normalizado retornado por essa fonte.
- Introduzir uma abstração interna de consulta de CEP, com ViaCEP como adapter inicial e configuração externa de URL e timeouts, sem credenciais.
- Validar a existência do CEP e reconciliar o endereço: preservar campos não vazios da fonte de CNPJ; preencher somente logradouro, bairro, município e UF ausentes com dados ViaCEP; nunca inventar número; manter o CEP da fonte cadastral após normalização.
- Tratar CEP inexistente, resposta externa inválida e falhas técnicas em contratos HTTP estáveis, sem retry, cache ou exposição de detalhes do provider.
- Manter o request de `POST /corretoras` contendo apenas CNPJ e preservar `validadaNaCvm=false`.

## Capabilities

### New Capabilities

- `broker-cep-data`: validação e enriquecimento seguro de endereço por CEP através de uma abstração interna e ViaCEP como provider inicial.

### Modified Capabilities

- `broker-management`: o cadastro passa a validar e enriquecer o endereço recebido da fonte de CNPJ antes de persistir a corretora, sem alterar o contrato de entrada.

## Impact

- Service de corretoras, tratamento centralizado de erros, configuração HTTP, README e testes automatizados.
- Novos componentes isolados em `integration/cep`, sem dependências novas, migration ou alteração de entidade/JPA.
- Consulta externa adicional por cadastro não duplicado, depois da consulta CNPJ bem-sucedida.
