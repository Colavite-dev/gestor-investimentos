ALTER TABLE carteiras
    ADD COLUMN usuario_id BIGINT NOT NULL;

ALTER TABLE carteiras
    ADD CONSTRAINT fk_carteiras_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id);

ALTER TABLE carteiras
    DROP CONSTRAINT uk_carteiras_nome_normalizado;

ALTER TABLE carteiras
    ADD CONSTRAINT uk_carteiras_usuario_nome_normalizado
        UNIQUE (usuario_id, nome_normalizado);

CREATE INDEX idx_carteiras_usuario_id
    ON carteiras(usuario_id, id);
