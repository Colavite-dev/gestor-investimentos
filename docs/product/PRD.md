# PRD — Adapt Invest

## Visão

Adapt Invest é uma API REST Java/Spring Boot para gestão acadêmica de corretoras, ativos e carteiras, complementada por um frontend React. O foco obrigatório é cadastro e consulta de corretoras e ações com integrações externas; carteira, operações, posições, histórico, autenticação e dashboard são evoluções já implementadas.

## Estado atual do produto

- Corretoras: cadastro por CNPJ, enriquecimento BrasilAPI/ViaCEP, elegibilidade CVM, listagem e busca por ID/CNPJ.
- Ações: cadastro, consulta, atualização de cotação, busca, catálogo e resolução para mercados BR e EUA.
- Carteiras: pertencem ao usuário autenticado; suportam operações de compra/venda, posições, preço médio e resumo por moeda.
- Histórico: cotações são persistidas e consultáveis por intervalo.
- Autenticação: JWT Bearer stateless, usuários `USER` e `ADMIN`, senhas BCrypt e endpoints administrativos protegidos.
- Frontend: React + TypeScript + Vite, com autenticação, dashboard, catálogo de ativos, corretoras, carteiras, operações e histórico.

## Regras de acesso

O usuário autenticado é a fonte de identidade para recursos financeiros. `usuarioId` não é recebido como mecanismo de autorização. Carteiras, operações, posições e resumos são isolados por proprietário; consultas de outro usuário retornam `404`. `ADMIN` possui acesso aos endpoints administrativos, mas não possui acesso implícito às carteiras de terceiros. Ativos e dados de mercado são globais.

## Integrações e qualidade

BrasilAPI, ViaCEP, CVM, brapi e Twelve Data são isoladas em providers/adapters. PostgreSQL é o banco de runtime, Flyway versiona V1–V8 e H2 é utilizado somente nos testes. Docker Compose fornece PostgreSQL local; testes normais não dependem de internet.

## Fora do escopo

Importação de notas, dividendos, imposto de renda, recomendações, execução real de ordens, Swagger/OpenAPI, retry automático e cache genérico não fazem parte do produto atual.

## Ambiguidades acadêmicas

O sistema mantém a identidade `(ticker, mercado)`, PostgreSQL em runtime e H2 em testes, sem MySQL. Essas escolhas e a execução individual não representam aprovação do professor e permanecem sujeitas à orientação acadêmica.
