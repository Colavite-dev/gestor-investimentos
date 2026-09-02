## Why

O cadastro atual aceita dados cadastrais informados pelo cliente e não atende ao requisito acadêmico de consultar uma fonte pública pelo CNPJ. A BrasilAPI permite enriquecer o cadastro com dados oficiais sem acoplar a regra de negócio ao contrato de um provider específico.

## What Changes

- **BREAKING**: reduzir o corpo de `POST /corretoras` para receber somente o CNPJ, tornando a fonte cadastral responsável pelos demais campos persistidos.
- Consultar `GET /api/cnpj/v1/{cnpj}` da BrasilAPI após validação local e verificação de duplicidade.
- Introduzir um port interno, um modelo cadastral independente e um adapter BrasilAPI com DTO externo próprio.
- Mapear CNPJ, razão social, nome fantasia, e-mail, telefone, CEP, endereço, município, UF e situação cadastral retornados pelo provider.
- Tratar CNPJ não encontrado, resposta inválida, timeout, indisponibilidade e rate limit sem expor detalhes internos da BrasilAPI.
- Manter `validadaNaCvm=false`; integração de CEP e validação CVM permanecem fora desta mudança.

## Capabilities

### New Capabilities

- `broker-cnpj-data`: consulta cadastral de CNPJ por abstração interna, com BrasilAPI como primeiro adapter e tratamento estável de falhas externas.

### Modified Capabilities

- `broker-management`: altera o contrato e a origem dos dados no cadastro de corretoras, preservando listagem, consultas, unicidade e estado CVM ainda não validado.

## Impact

- Contrato HTTP de `POST /corretoras`, DTO de entrada, service, mapper, exceções e documentação.
- Novos componentes em `integration/cnpj` e configuração externa de URL e timeouts.
- Testes de service/controller e testes isolados do adapter HTTP; nenhuma nova migration é necessária porque o modelo persistente não muda.
- Uso do cliente HTTP síncrono `RestClient`, já fornecido pelo Spring Web presente no projeto, sem adicionar WebFlux ou SDK externo.
