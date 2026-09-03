## Context

Os requisitos funcionais e as integrações externas já estão implementados e documentados no README. O repositório ainda não possui coleção de requests nem diagrama de entidades, embora ambos sejam entregáveis acadêmicos explícitos.

## Goals / Non-Goals

**Goals:**

- Criar artefatos portáveis e versionáveis para demonstração local.
- Refletir exatamente os contratos atuais, incluindo os dois mercados e a atualização de cotação.
- Manter todos os valores sensíveis como variáveis configuráveis e vazias por padrão.

**Non-Goals:**

- Alterar endpoints, DTOs, regras de negócio, banco ou código Java.
- Criar frontend, autenticação, publicação externa ou apresentação em slides.
- Adicionar testes automatizados de negócio apenas para validar documentação.

## Decisions

### Formato da coleção

Será usada uma coleção Postman v2.1 em `docs/api/gestor-investimento.postman_collection.json`, formato amplamente importável e já adequado a requests HTTP/JSON. A variável `baseUrl` terá valor local padrão não sensível; IDs e payloads de exemplo serão claramente identificados como valores de demonstração.

Alternativas consideradas: Insomnia export (menos universal para avaliadores) e OpenAPI/Swagger (mais amplo que o requisito e não existente no projeto). A coleção Postman atende diretamente ao entregável sem adicionar dependências.

### Organização dos requests

As pastas serão `Corretoras` e `Ações`. Cada request terá método, caminho, headers e body compatíveis com o README. Requests que dependem de APIs externas continuarão sendo exemplos manuais, sem execução automática ou credenciais embutidas.

### Formato do diagrama

Será usado Mermaid em `docs/architecture/entity-model.md`, pois é texto versionável, renderiza no GitHub e não exige ferramenta binária. O diagrama mostrará `Corretora` e `Acao`, campos essenciais, enums de mercado/moeda, chave primária e a unicidade `(ticker, mercado)`, sem aresta entre as entidades.

### Segurança

Nenhum arquivo conterá `DB_PASSWORD`, `BRAPI_TOKEN` ou `TWELVE_DATA_API_KEY` reais. Variáveis da coleção serão placeholders ou valores não sensíveis. A revisão incluirá busca textual por padrões de segredo antes de concluir.

## Risks / Trade-offs

- [Contratos podem evoluir] → incluir apenas requests alinhados ao README atual e revisar a coleção em mudanças futuras de API.
- [Visualizador pode não suportar Mermaid] → manter o arquivo em Markdown com descrição textual suficiente para leitura sem renderização.
- [Exemplos podem ser confundidos com dados reais] → usar placeholders e nomes explícitos de demonstração, sem executar ou persistir dados automaticamente.

## Migration Plan

Não há migration de banco nem migração de dados. A aplicação continua inalterada; os artefatos são adicionados e o README passa a referenciá-los. Rollback consiste em remover os arquivos de documentação e os links correspondentes.
