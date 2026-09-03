# Modelo simplificado de entidades

O diagrama representa o modelo persistente atual. `Acao` é um cadastro mestre identificado logicamente por `(ticker, mercado)`; não há relacionamento obrigatório com `Corretora`.

```mermaid
erDiagram
    CORRETORA {
        BIGINT id PK
        VARCHAR cnpj UK
        VARCHAR razao_social
        VARCHAR nome_fantasia
        VARCHAR email
        VARCHAR telefone
        VARCHAR cep
        VARCHAR logradouro
        VARCHAR numero
        VARCHAR complemento
        VARCHAR bairro
        VARCHAR cidade
        CHAR uf
        VARCHAR situacao_cadastral
        BOOLEAN validada_na_cvm
        TIMESTAMP data_cadastro
    }

    ACAO {
        BIGINT id PK
        VARCHAR ticker
        VARCHAR nome_empresa
        ENUM mercado "BRASIL | ESTADOS_UNIDOS"
        ENUM moeda "BRL | USD"
        DECIMAL cotacao_atual
        TIMESTAMP data_hora_cotacao
        UNIQUE ticker_mercado "ticker + mercado"
    }
```

`CORRETORA` e `ACAO` não possuem aresta no diagrama porque o modelo atual não persiste associação entre elas.
