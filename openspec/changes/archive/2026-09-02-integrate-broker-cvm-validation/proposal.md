## Why

O cadastro de corretoras já confirma os dados cadastrais na BrasilAPI e valida o CEP no ViaCEP, mas ainda não comprova que a instituição é uma participante intermediária apta perante a CVM. Essa verificação é necessária antes de persistir uma corretora como válida para o sistema.

## What Changes

- Adiciona validação de participante intermediário da CVM entre a validação de CEP e a persistência de uma corretora.
- Usa o dataset oficial diário **Participantes Intermediários: Informação Cadastral** da CVM, distribuído como ZIP com arquivos CSV; não pressupõe um endpoint REST por CNPJ.
- Normaliza o CNPJ para comparação e aceita somente registros ativos cuja categoria seja compatível com o escopo de corretoras.
- Persiste a corretora somente após resultado positivo da CVM, definindo `validadaNaCvm=true`; instituição inexistente, inativa ou de categoria incompatível é rejeitada sem persistência.
- Isola o download, a leitura de ZIP/CSV e a atualização do dataset em adapter atrás de um port interno, com reutilização de um snapshot em memória para evitar download e reprocessamento a cada cadastro.
- Expõe falhas controladas para rejeição cadastral (422), conteúdo externo incompatível (502) e indisponibilidade técnica da fonte (503), sem vazar detalhes do provider.

## Capabilities

### New Capabilities

- `broker-cvm-validation`: validação de corretora contra o cadastro oficial diário de Participantes Intermediários da CVM.

### Modified Capabilities

- `broker-management`: o cadastro passa a exigir aprovação da CVM antes de persistir e retorna `validadaNaCvm=true` para uma corretora criada com sucesso.

## Impact

- Componentes da aplicação de Corretora: service, ports, adapter CVM, configuração HTTP/cache, exceções e tratamento HTTP.
- Testes unitários e de controller usarão dublês do port CVM; uma verificação real, se criada, será opt-in e fora da suíte normal.
- A tabela e o mapeamento JPA existentes já possuem `validadaNaCvm`; não há alteração de schema ou migration prevista.
