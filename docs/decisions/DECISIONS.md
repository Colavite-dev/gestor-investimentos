# Decisões do Projeto

## DEC-001 — Desenvolvimento individual

O projeto será desenvolvido por uma única pessoa, apesar de o enunciado original mencionar grupo.

## DEC-002 — Banco principal

PostgreSQL será utilizado como banco principal.

## DEC-003 — Banco em testes

H2 poderá ser utilizado em testes quando trouxer simplicidade sem mascarar comportamentos específicos do PostgreSQL.

## DEC-004 — Versionamento do banco

Flyway será utilizado para migrations.

## DEC-005 — Infraestrutura local

PostgreSQL será executado através de Docker.

Inicialmente a aplicação Spring Boot poderá executar diretamente no ambiente local.

## DEC-006 — Referência de produto

O Investidor10 será utilizado como inspiração conceitual para experiência de gerenciamento de investimentos e carteira.

Não existe objetivo de copiar integralmente a plataforma.

## DEC-007 — Isolamento de APIs externas

Serviços de terceiros deverão ser acessados através de abstrações internas.

As regras de negócio não devem depender diretamente de SDKs, DTOs ou formatos específicos das APIs externas.

## DEC-008 — Spec-Driven Development

Mudanças relevantes deverão ser planejadas e especificadas antes da implementação.

OpenSpec será utilizado para organizar esse fluxo.

## DEC-009 — Assistente de desenvolvimento

Codex será utilizado como principal agente de programação.

O Codex deverá respeitar PRD, arquitetura, decisões e especificações OpenSpec.

## DEC-010 — Interpretação dos bancos exigidos

O requisito original menciona H2, MySQL e PostgreSQL de forma ambígua.

Para este projeto foi decidido utilizar:

- PostgreSQL como banco relacional principal;
- H2 opcionalmente em testes;
- MySQL não será utilizado.

A decisão evita manter compatibilidade artificial com três bancos e concentra o desenvolvimento no PostgreSQL.

## DEC-011 — Instituição financeira não validada

Uma instituição que não atender ao critério definido de validação para atuação no mercado financeiro não deverá ser cadastrada como corretora válida.

A requisição deverá ser rejeitada com erro de negócio apropriado.

O critério e a fonte externa utilizados para essa validação serão definidos na especificação da funcionalidade de cadastro de corretora.

## DEC-012 — Identificação do mercado

O sistema deverá buscar identificar o mercado do ativo a partir do ticker e dos dados obtidos pelos providers externos.

A experiência desejada evita exigir que o usuário conheça previamente detalhes técnicos do mercado.

A estratégia exata de resolução de mercado será definida na especificação de cadastro de ativos.

## DEC-013 — Identidade lógica do ativo

A identidade lógica de um ativo será composta inicialmente por:

ticker + mercado.

O mesmo ativo não poderá ser cadastrado mais de uma vez para o mesmo mercado.

Essa decisão permite distinguir tickers iguais que eventualmente existam em mercados diferentes.

## DEC-014 — Escopo inicial dos ativos financeiros

O núcleo acadêmico inicial será implementado para ações brasileiras e americanas.

Embora alguns documentos utilizem o termo genérico "ativo", isso não implica suporte inicial a todas as classes de ativos financeiros.

Generalizações para ETFs, FIIs, BDRs, criptomoedas, renda fixa ou outros instrumentos deverão ser tratadas como evoluções futuras e especificadas separadamente.

