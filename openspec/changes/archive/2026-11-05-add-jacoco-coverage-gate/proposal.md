## Why

As aulas 13 e 14 já estão atendidas no projeto: ele possui autenticação JWT/BCrypt, papéis `USER` e `ADMIN`, recursos administrativos e isolamento dos dados de investimento, cobertos por testes existentes. A lacuna da aula 15 é que o Maven ainda não mede nem exige cobertura; adicionar esse controle torna a qualidade da suíte visível e impede regressões silenciosas.

## What Changes

- Configurar JaCoCo no ciclo `verify` do Maven para gerar relatórios de cobertura e reprovar a compilação quando os limites mínimos de linhas ou ramificações não forem atingidos.
- Definir limites iniciais de 80% para linhas e 55% para ramificações, conforme referência da aula 15, e validar a suíte atual contra eles antes de concluir a implementação.
- Documentar como executar a verificação e localizar o relatório, sem alterar contratos da API ou políticas de acesso já especificadas.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `academic-deliverables`: exigir verificação reproduzível de cobertura de linhas e ramificações no build Maven.

## Impact

- Backend/build: `pom.xml` e relatórios Maven em `target/site/jacoco`.
- Documentação/spec: especificação `academic-deliverables` e instruções de uso no README.
- Não há alteração planejada em endpoints, autenticação, autorização, migrations ou dados existentes.
