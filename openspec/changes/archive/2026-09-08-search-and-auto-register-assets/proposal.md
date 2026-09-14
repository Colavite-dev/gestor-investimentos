## Why

O lançamento de uma operação hoje depende de a ação já existir no cadastro mestre local. Isso interrompe a demonstração e o uso normal: a pessoa precisa sair da operação, cadastrar o ativo em outra tela e voltar. A mudança permite descobrir ativos suportados da B3 e dos Estados Unidos no próprio fluxo, sem expor credenciais nem duplicar regras financeiras no navegador.

## What Changes

- Adicionar uma pesquisa remota, somente de leitura, de sugestões de ações brasileiras e americanas, mediada pelo backend e pelos providers já existentes, com agregação tolerante a falha parcial.
- Adicionar uma resolução explícita de uma sugestão selecionada: reutilizar a ação persistida para `(ticker, mercado)` quando ela existir; caso contrário, validar os dados externos e persistir o ativo e sua observação inicial de cotação antes de retornar a ação com ID.
- Estender o contrato de ações com DTOs internos de sugestão e resolução, mantendo `POST /operacoes` e `OperacaoRequest.acaoId` inalterados.
- Evoluir o combobox de ação da nova operação para autocomplete remoto com mínimo de caracteres, debounce, estados de carregamento/erro/nenhum resultado, navegação por teclado e seleção obrigatória de uma sugestão resolvida.
- Preservar a identidade `(ticker, mercado)`, os mercados Brasil/Estados Unidos, as moedas BRL/USD e a classificação centralizada de falhas externas (`422`, `502`, `503`), sem deixar a falha de um provider esconder sugestões válidas do outro.
- Não criar migrations, novos providers, chamadas externas no frontend, conversão cambial, retry automático ou persistência durante pesquisa.

## Capabilities

### New Capabilities

_Nenhuma._

### Modified Capabilities

- `stock-management`: pesquisa e resolução explícita de ativos para reutilização ou cadastro automático sob seleção do usuário.
- `brazilian-stock-data`: descoberta de sugestões elegíveis da B3 pela brapi sem escrita e resolução brasileira compatível com as regras de identidade já aceitas.
- `us-stock-data`: descoberta de sugestões elegíveis da Twelve Data sem escrita e resolução americana com a classificação semântica já existente.
- `frontend-application`: autocomplete remoto de ação no lançamento de operação, sem texto livre como referência de ativo.
- `frontend-backend-integration`: consumo browser → API Spring para sugestões e resolução de ações, preservando mensagens públicas controladas e sem segredos.

## Impact

- Backend: `AcaoController`, `AcaoService`, o port/selector de dados de ações, adapters brapi/Twelve Data, DTOs de ação e testes de controller/service/provider.
- Frontend: cliente de API, tipos e o combobox do modal de nova operação; a operação continuará a enviar exclusivamente o ID retornado da ação resolvida.
- Banco: a tabela `acoes`, o histórico de cotação e a unicidade existente `uk_acoes_ticker_mercado` serão reutilizados; não há mudança de schema planejada.
- Segurança e disponibilidade: browser continuará chamando apenas o Spring; tokens dos providers permanecem no backend. Pesquisa e resolução seguirão os timeouts e a classificação de falhas já estabelecidos.
