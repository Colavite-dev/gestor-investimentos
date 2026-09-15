ALTER TABLE corretoras
    ADD COLUMN usuario_id BIGINT NOT NULL;

ALTER TABLE corretoras
    ADD CONSTRAINT fk_corretoras_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id);

ALTER TABLE corretoras
    DROP CONSTRAINT uk_corretoras_cnpj;

ALTER TABLE corretoras
    ADD CONSTRAINT uk_corretoras_usuario_cnpj
        UNIQUE (usuario_id, cnpj);

CREATE INDEX idx_corretoras_usuario_id
    ON corretoras(usuario_id, id);
