## ADDED Requirements

### Requirement: Browser usa contratos de busca e resolução protegidos pelo backend
O frontend SHALL chamar `GET /acoes/pesquisar` para sugestões e `POST /acoes/resolver` para a sugestão escolhida, utilizando a base configurada da API Spring. Esses contratos SHALL transportar somente dados públicos de ação; chaves da brapi, chave da Twelve Data, tokens, URLs de provider e credenciais de banco MUST NOT ser enviados, exibidos ou armazenados no frontend.

#### Scenario: Pesquisa remota do navegador
- **WHEN** o autocomplete inicia pesquisa válida
- **THEN** o browser envia somente o termo para a API Spring e recebe sugestões normalizadas sem contato direto com provider externo

#### Scenario: Resolução retorna referência persistida
- **WHEN** o usuário confirma uma sugestão
- **THEN** o browser envia ticker e mercado à API Spring e recebe a ação persistida ou reutilizada com ID para uso em `POST /operacoes`

### Requirement: Erros de busca e resolução são apresentados sem detalhes sensíveis
O cliente de API SHALL preservar a classificação pública da API para `400`, `422`, `502` e `503` durante pesquisa e resolução. A interface SHALL aceitar `200` de pesquisa agregada mesmo quando a API tiver usado apenas resultados de provider saudável, e SHALL comunicar entrada inválida, ativo não elegível, resposta externa inválida ou indisponibilidade sem mostrar stack trace, payload externo bruto ou detalhes de credencial.

#### Scenario: Provider não disponível para autocomplete
- **WHEN** a API Spring responde `503 Service Unavailable` durante busca ou resolução
- **THEN** o frontend informa indisponibilidade temporária e não marca uma ação como selecionada
