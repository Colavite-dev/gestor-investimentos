package com.colavite.gestor_investimento.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarteiraOwnershipMigrationTest {
    @Test
    void migratesEmptyDatabaseWithoutLegacyPlaceholderOrBackfill() throws Exception {
        String url = databaseUrl("empty");
        Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load().migrate();

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            try (var columns = statement.executeQuery("SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='CARTEIRAS' AND COLUMN_NAME='USUARIO_ID'")) {
                assertThat(columns.next()).isTrue();
                assertThat(columns.getString(1)).isEqualTo("NO");
            }
            try (var history = statement.executeQuery("SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"version\"='8' AND \"success\"=TRUE")) {
                assertThat(history.next()).isTrue();
                assertThat(history.getInt(1)).isEqualTo(1);
            }
        }
    }

    @Test
    void refusesLegacyWalletWithoutOwnerAndPreservesItsData() throws Exception {
        String url = databaseUrl("legacy");
        Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").target("7").load().migrate();
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO carteiras (nome, nome_normalizado, descricao, data_cadastro) VALUES ('Legado', 'LEGADO', NULL, CURRENT_TIMESTAMP)");
        }

        assertThatThrownBy(() -> Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load().migrate())
                .isInstanceOf(FlywayException.class);

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT COUNT(*) FROM carteiras WHERE nome_normalizado='LEGADO'")) {
            assertThat(rows.next()).isTrue();
            assertThat(rows.getInt(1)).isEqualTo(1);
        }
    }

    private String databaseUrl(String name) {
        return "jdbc:h2:mem:ownership_" + name + "_" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    }
}
