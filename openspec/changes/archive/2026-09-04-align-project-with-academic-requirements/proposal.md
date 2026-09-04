## Why

A auditoria do enunciado acadêmico encontrou o núcleo obrigatório funcionalmente atendido, mas identificou duas redações ambíguas que não devem ser resolvidas por mudança automática: a unicidade literal de ticker e a menção conjunta a H2, MySQL e PostgreSQL. A change cria uma trilha controlada para registrar a decisão humana e confirmar que nenhum requisito obrigatório foi introduzido ou removido sem aprovação.

## What Changes

- Registrar a avaliação técnica da identidade lógica atual de ativo como `(ticker, mercado)` em confronto com a redação literal de unicidade por ticker.
- Registrar a avaliação da escolha PostgreSQL como banco principal e H2 exclusivamente para testes, em confronto com a redação que cita H2, MySQL e PostgreSQL.
- Exigir confirmação do professor antes de qualquer mudança de unicidade, banco, schema, endpoint ou provider motivada por essas ambiguidades.
- Confirmar que as lacunas documentais encontradas — README, modelo de entidades e coleção Postman desatualizados — permanecem destinadas à Fase 7 e não serão implementadas nesta change.

## Capabilities

### New Capabilities

Nenhuma. A change não introduz comportamento de produto.

### Modified Capabilities

Nenhuma. A auditoria não encontrou mudança funcional obrigatória aprovada para as stable specs.

## Impact

Não há alteração prevista em código de produção, testes, migrations, endpoints, dependências ou stable specs. O eventual APPLY depende de decisão humana e se limita a registrar a interpretação aprovada no local documental apropriado, sem antecipar os entregáveis da Fase 7.
