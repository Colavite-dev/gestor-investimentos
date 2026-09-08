# Modelo persistido de entidades

O diagrama representa as tabelas e relacionamentos atuais. `Corretora` não possui associação persistida com `Acao`.

```mermaid
erDiagram
    CORRETORA {
        BIGINT id PK
        VARCHAR cnpj UK
        VARCHAR razao_social
        VARCHAR nome_fantasia
        VARCHAR cep
        VARCHAR cidade
        CHAR uf
        BOOLEAN validada_na_cvm
        TIMESTAMPTZ data_cadastro
    }

    ACAO {
        BIGINT id PK
        VARCHAR ticker
        VARCHAR nome_empresa
        VARCHAR mercado
        VARCHAR moeda
        DECIMAL cotacao_atual
        TIMESTAMPTZ data_hora_cotacao
        UNIQUE ticker_mercado
    }

    CARTEIRA {
        BIGINT id PK
        VARCHAR nome
        VARCHAR nome_normalizado UK
        VARCHAR descricao
        TIMESTAMPTZ data_cadastro
    }

    OPERACAO {
        BIGINT id PK
        BIGINT carteira_id FK
        BIGINT acao_id FK
        VARCHAR tipo
        DECIMAL quantidade
        DECIMAL preco_unitario
        TIMESTAMPTZ data_operacao
    }

    COTACAO_HISTORICA {
        BIGINT id PK
        BIGINT acao_id FK
        DECIMAL cotacao
        TIMESTAMPTZ data_hora_cotacao
        TIMESTAMPTZ data_registro
    }

    CARTEIRA ||--o{ OPERACAO : registra
    ACAO ||--o{ OPERACAO : referencia
    ACAO ||--o{ COTACAO_HISTORICA : possui
```

`OPERACAO` é obrigatoriamente ligada a uma `CARTEIRA` e uma `ACAO`. `COTACAO_HISTORICA` é obrigatoriamente ligada a uma `ACAO`. As cotações, quantidades e preços monetários usam precisão `NUMERIC(19,8)` no schema atual.
