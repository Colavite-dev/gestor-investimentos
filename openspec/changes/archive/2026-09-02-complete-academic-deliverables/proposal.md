## Why

Os requisitos acadêmicos funcionais estão concluídos, mas ainda faltam entregáveis exigidos para demonstrar e apresentar o sistema: uma coleção executável de chamadas da API e um diagrama simplificado das entidades. Formalizar esses artefatos agora torna a entrega reproduzível e verificável sem alterar o comportamento da aplicação.

## What Changes

- Criar uma coleção Postman ou Insomnia com os endpoints atuais de corretoras e ações, incluindo exemplos de sucesso, validação, duplicidade e falhas externas.
- Criar um diagrama simplificado das entidades persistidas, seus campos, identidade lógica e ausência de relacionamento obrigatório entre Ação e Corretora.
- Documentar no README como importar a coleção e visualizar o diagrama, sem incluir credenciais ou dados secretos.

## Capabilities

### New Capabilities

- `academic-deliverables`: Artefatos reproduzíveis para demonstração e entrega acadêmica da API e do modelo persistente.

### Modified Capabilities

Nenhuma. O comportamento da API e do modelo de domínio não será alterado.

## Impact

- Novos arquivos de documentação e exemplos em `docs/` ou diretório equivalente.
- Atualização pontual do `README.md`.
- Nenhuma alteração em Java, banco, migrations, dependências, configuração, secrets ou contratos HTTP.
