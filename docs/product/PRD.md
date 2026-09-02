# PRD — Gestor de Investimentos

## 1. Visão do Produto

O Gestor de Investimentos é uma aplicação para gerenciamento de ativos financeiros, corretoras e carteiras de investimentos.

O sistema será desenvolvido em Java com Spring Boot e deverá integrar dados provenientes de APIs externas para validar instituições, consultar informações cadastrais e obter cotações de ativos financeiros.

O Investidor10 será utilizado como referência conceitual de experiência e organização das funcionalidades de carteira, sem intenção de reproduzir integralmente a plataforma.

---

## 2. Objetivo Principal

Permitir que o usuário gerencie informações relacionadas aos seus investimentos utilizando dados reais provenientes de serviços externos.

O sistema deverá permitir inicialmente:

- cadastrar e consultar corretoras;
- cadastrar e consultar ativos financeiros;
- obter cotações externas;
- distinguir ativos brasileiros e americanos;
- integrar informações cadastrais provenientes de APIs públicas.

Posteriormente, o sistema poderá evoluir para gerenciamento de carteiras e operações.

---

## 3. Usuário

Nesta primeira versão será considerado um usuário do sistema responsável por consultar e organizar seus investimentos.

Autenticação não faz parte inicialmente do núcleo obrigatório, podendo ser implementada como evolução futura.

---

## 4. Escopo Acadêmico Obrigatório

### Corretoras

O sistema deverá permitir:

- cadastrar corretora através de CNPJ;
- validar formato do CNPJ;
- consultar os dados cadastrais através de API externa;
- consultar endereço através do CEP;
- validar compatibilidade da instituição com o mercado financeiro;
- armazenar os dados validados;
- listar corretoras;
- buscar corretora por ID;
- buscar corretora por CNPJ;
- impedir duplicidade.

### Ativos

O sistema deverá permitir:

- cadastrar ações;
- consultar existência do ticker através de API externa;
- suportar ativos brasileiros;
- suportar ativos americanos;
- identificar o mercado do ativo;
- obter cotação atual ou mais recente;
- armazenar os dados do ativo;
- listar ativos;
- buscar ativo por ID;
- buscar ativo por ticker;
- atualizar a cotação;
- impedir duplicidade lógica do ativo.

---

## 5. Evolução de Produto

Após o núcleo acadêmico estar funcionando, o sistema poderá incluir gerenciamento de carteira inspirado conceitualmente em plataformas como Investidor10.

Possíveis funcionalidades:

- criação de carteira;
- registro de operações;
- compra;
- venda;
- quantidade de ativos;
- preço médio;
- valor investido;
- patrimônio atual;
- lucro ou prejuízo;
- rentabilidade;
- histórico de operações;
- evolução patrimonial;
- dashboard.

Essas funcionalidades não devem impedir a conclusão dos requisitos acadêmicos obrigatórios.

---

## 6. Modelo Conceitual Inicial

### Corretora

Representa uma instituição utilizada para negociação de investimentos.

### Ativo

Representa um ativo financeiro identificável através de ticker e mercado.

### Carteira

Representa um conjunto de investimentos pertencentes ao usuário.

Será implementada apenas após conclusão do núcleo obrigatório.

### Operação

Representa uma compra ou venda realizada sobre determinado ativo.

Será implementada apenas na fase de gerenciamento de carteira.

---

## 7. Identificação de Mercado

O sistema deverá buscar minimizar a necessidade de o usuário conhecer detalhes técnicos do mercado.

Sempre que possível, o sistema deverá identificar ou resolver o mercado associado ao ativo informado.

Exemplos:

PETR4 → Brasil

AAPL → Estados Unidos

A lógica utilizada para realizar essa identificação deverá permanecer isolada da camada de apresentação.

---

## 8. Integrações Externas

As integrações candidatas são:

- BrasilAPI — informações brasileiras e CNPJ;
- ViaCEP — endereço por CEP;
- brapi.dev — ativos brasileiros;
- Alpha Vantage — ativos americanos;
- Twelve Data — alternativa para ativos americanos;
- fonte pública adequada para validação de instituições financeiras.

A escolha definitiva de providers deverá considerar:

- disponibilidade;
- documentação;
- autenticação;
- limites gratuitos;
- estabilidade;
- informações retornadas.

O sistema deve demonstrar no mínimo três integrações externas reais.

---

## 9. Tratamento de Falhas

O sistema deverá prever:

- CNPJ inválido;
- CNPJ inexistente;
- CEP inválido;
- CEP inexistente;
- ticker inexistente;
- serviço externo indisponível;
- timeout;
- limite de requisições;
- resposta inválida de serviço externo;
- duplicidade;
- recurso não encontrado.

O tratamento HTTP deverá ser centralizado e padronizado.

---

## 10. Requisitos Técnicos

Backend:

- Java;
- Spring Boot;
- API REST;
- JSON.

Persistência:

- PostgreSQL como banco principal;
- H2 poderá ser utilizado em testes quando apropriado;
- Spring Data JPA;
- Flyway para migrations.

Infraestrutura:

- Docker;
- Docker Compose;
- variáveis de ambiente;
- arquivo `.env` local;
- `.env.example` sem informações secretas.

Qualidade:

- validação de entrada;
- tratamento centralizado de erros;
- testes automatizados;
- organização em camadas;
- documentação dos endpoints;
- código legível e padronizado.

---

## 11. Arquitetura

O projeto deverá possuir no mínimo:

- controller;
- service;
- repository;
- entity/model;
- dto.

Integrações externas não deverão ficar diretamente acopladas às regras de negócio.

Devem ser utilizadas abstrações que permitam isolar serviços de terceiros.

Exemplo conceitual:

Service
→ Provider/Port
→ Adapter
→ API externa

---

## 12. Banco de Dados

PostgreSQL será o banco principal da aplicação.

Flyway será responsável pelo versionamento do schema.

Alterações estruturais no banco deverão ser realizadas através de novas migrations.

Uma migration já aplicada não deverá ser modificada para representar uma nova alteração.

---

## 13. Fora do Escopo Inicial

Não fazem parte da primeira implementação:

- frontend completo;
- sincronização com corretoras reais;
- importação automática de notas de corretagem;
- autenticação;
- múltiplos usuários;
- dividendos;
- imposto de renda;
- recomendações de investimento;
- execução real de compra ou venda.

Esses itens podem ser considerados posteriormente.

---

## 14. Critério de Prioridade

A prioridade de desenvolvimento será:

1. requisitos acadêmicos obrigatórios;
2. estabilidade;
3. tratamento de falhas;
4. testes;
5. documentação;
6. diferenciais;
7. gerenciamento avançado de carteira.

---

## 15. Princípio de Desenvolvimento

O projeto seguirá Spec-Driven Development.

Mudanças relevantes deverão possuir uma especificação antes da implementação.

O código deve implementar a especificação aprovada, e não substituir a especificação.