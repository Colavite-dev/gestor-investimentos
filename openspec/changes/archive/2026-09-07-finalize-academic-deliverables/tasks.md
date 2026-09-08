## 1. Base documental e README

- [x] 1.1 Reescrever README.md em UTF-8 como guia conciso do sistema atual e verificar cada seção contra controllers, DTOs, migrations, configurações e stable specs.
- [x] 1.2 Documentar no README funcionalidades obrigatórias versus evoluções/diferenciais, sem apresentar Carteiras, Operações, posições ou histórico como requisito acadêmico original.
- [x] 1.3 Documentar configuração Windows/PowerShell, `.env`, PostgreSQL Docker, Flyway V1–V6 e Hibernate `ddl-auto=validate`; verificar comandos e variáveis contra `compose.yaml`, `.env.example` e `application.properties`.
- [x] 1.4 Documentar endpoints principais, tabela de erros 400/404/409/422/502/503, testes normais e limitações confirmadas; verificar que nenhum recurso futuro seja declarado como implementado.

## 2. Integrações e decisões acadêmicas

- [x] 2.1 Criar `docs/integrations/external-apis.md` em UTF-8 para BrasilAPI, ViaCEP, CVM, brapi e Twelve Data; verificar finalidade, autenticação, variáveis, timeouts, falhas e limites somente com fatos confirmados.
- [x] 2.2 Adicionar ao README uma visão curta das integrações e link para o documento detalhado; verificar que a documentação não exponha chaves, tokens, senhas, URLs sensíveis ou corpos externos.
- [x] 2.3 Documentar corretamente os mecanismos opt-in existentes para PostgreSQL e testes reais de providers, verificando que `mvn test` e `mvn verify` não dependam de Docker ou internet.
- [x] 2.4 Atualizar DECISIONS.md para registrar decisões implementadas relevantes e as pendências acadêmicas ticker versus `(ticker, mercado)` e PostgreSQL/H2/MySQL; verificar que nenhuma seja descrita como aprovação do professor.

## 3. Modelo, coleção e documentos de contexto

- [x] 3.1 Atualizar `entity-model.md` em Mermaid com Corretora, Acao, Carteira, Operacao e CotacaoHistorica, atributos relevantes e relações reais; verificar cardinalidades contra V1–V6 e entidades JPA.
- [x] 3.2 Reorganizar a coleção Postman em Corretoras, Ações, Histórico de Cotações, Carteiras e Operações, usando apenas variáveis não secretas; validar JSON e compatibilidade dos corpos com os DTOs.
- [x] 3.3 Cobrir os 18 endpoint mappings atuais na coleção Postman e verificar método, URL e variáveis de cada request contra os controllers.
- [x] 3.4 Atualizar apenas trechos factualmente desatualizados de ARCHITECTURE.md e PRD.md; corrigir em PROFESSOR_REQUIREMENTS.md somente o encoding sem mudar o significado do enunciado.

## 4. Verificação do entregável

- [x] 4.1 Inspecionar todos os arquivos documentais alterados para UTF-8 e ausência de mojibake conhecido, preservando conteúdo não relacionado.
- [x] 4.2 Verificar links Markdown, sintaxe Mermaid, JSON Postman, ausência de credenciais e coerência com código/configuração; executar `git diff --check` e `git status --short`.
- [x] 4.3 Executar `openspec validate finalize-academic-deliverables --strict` e registrar que não há delta spec nem alteração de comportamento.
