package com.colavite.gestor_investimento.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorretoraOwnershipMigrationTest {
    @Test
    void migraBancoVazioComOwnershipObrigatorioEUnicidadeComposta() throws Exception {
        String url = databaseUrl("empty");
        Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load().migrate();

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            try (var columns = statement.executeQuery("SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='CORRETORAS' AND COLUMN_NAME='USUARIO_ID'")) {
                assertThat(columns.next()).isTrue();
                assertThat(columns.getString(1)).isEqualTo("NO");
            }
            try (var history = statement.executeQuery("SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"version\"='9' AND \"success\"=TRUE")) {
                assertThat(history.next()).isTrue();
                assertThat(history.getInt(1)).isEqualTo(1);
            }
        }
    }

    @Test
    void recusaCorretoraLegadaEPreservaLinhaESchemaAnterior() throws Exception {
        String url = databaseUrl("legacy");
        Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").target("8").load().migrate();
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO corretoras (cnpj, razao_social, cep, logradouro, numero, bairro, cidade, uf, situacao_cadastral, validada_na_cvm, data_cadastro) VALUES ('11222333000181', 'Legado', '01001000', 'Praca', '1', 'Se', 'Sao Paulo', 'SP', 'ATIVA', FALSE, CURRENT_TIMESTAMP)");
        }

        assertThatThrownBy(() -> Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load().migrate())
                .isInstanceOf(FlywayException.class);

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM corretoras WHERE cnpj='11222333000181'")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getInt(1)).isEqualTo(1);
            }
            try (var columns = statement.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='CORRETORAS' AND COLUMN_NAME='USUARIO_ID'")) {
                assertThat(columns.next()).isTrue();
                assertThat(columns.getInt(1)).isEqualTo(0);
            }
        }
    }

    private String databaseUrl(String name) {
        return "jdbc:h2:mem:broker_ownership_" + name + "_" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    }
}
