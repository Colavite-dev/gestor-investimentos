# Modelo persistido de entidades

O diagrama representa as tabelas e relacionamentos atuais. `Corretora` não possui associação persistida com `Acao`.

```mermaid
erDiagram
    USUARIO {
        BIGINT id PK
        VARCHAR nome
        VARCHAR username UK
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR role
        TIMESTAMPTZ created_at
    }

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
        BIGINT usuario_id FK
        VARCHAR nome
        VARCHAR nome_normalizado
        VARCHAR descricao
        TIMESTAMPTZ data_cadastro
        UNIQUE usuario_nome_normalizado
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

    USUARIO ||--o{ CARTEIRA : possui
    CARTEIRA ||--o{ OPERACAO : registra
    ACAO ||--o{ OPERACAO : referencia
    ACAO ||--o{ COTACAO_HISTORICA : possui
```

`CARTEIRA.usuario_id` é obrigatório. Seu nome normalizado é único somente dentro do mesmo usuário, conforme V8. `OPERACAO` referencia obrigatoriamente uma carteira e uma ação; `COTACAO_HISTORICA` referencia obrigatoriamente uma ação.

`ACAO` é global e sua identidade persistida é `(ticker, mercado)`. Cotações, quantidades e preços monetários usam `NUMERIC(19,8)` no schema atual.
