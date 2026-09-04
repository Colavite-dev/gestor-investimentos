ALTER TABLE acoes
    ALTER COLUMN cotacao_atual TYPE NUMERIC(19,8);

ALTER TABLE acoes
    ADD CONSTRAINT ck_acoes_cotacao_atual_positiva CHECK (cotacao_atual > 0);
