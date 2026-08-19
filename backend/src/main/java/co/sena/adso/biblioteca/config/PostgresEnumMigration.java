package co.sena.adso.biblioteca.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

/**
 * Extiende los tipos ENUM nativos de PostgreSQL con los nuevos estados del dominio
 * (perdido/eliminado para libros, fuera_de_servicio para equipos, etc.).
 *
 * PostgreSQL no permite ALTER TYPE ... ADD VALUE dentro de una transacción, por eso
 * no puede ir en una migración Flyway. Este runner lo ejecuta de forma segura
 * (ADD VALUE IF NOT EXISTS) justo después de que Hibernate valida/actualiza el esquema.
 * En H2 (tests) se omite silenciosamente.
 */
@Component
public class PostgresEnumMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PostgresEnumMigration.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public PostgresEnumMigration(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        if (!isPostgres()) {
            return;
        }
        List<String> statements = List.of(
            "ALTER TYPE estado_libro ADD VALUE IF NOT EXISTS 'perdido'",
            "ALTER TYPE estado_libro ADD VALUE IF NOT EXISTS 'eliminado'",
            "ALTER TYPE estado_equipo ADD VALUE IF NOT EXISTS 'perdido'",
            "ALTER TYPE estado_equipo ADD VALUE IF NOT EXISTS 'eliminado'",
            "ALTER TYPE estado_equipo ADD VALUE IF NOT EXISTS 'fuera_de_servicio'"
        );
        for (String sql : statements) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                log.debug("No se pudo ejecutar: {} -> {}", sql, e.getMessage());
            }
        }
        log.info("Enum de PostgreSQL verificado/extendido con los nuevos estados.");
    }

    private boolean isPostgres() {
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            return product != null && product.toLowerCase().contains("postgres");
        } catch (Exception e) {
            return false;
        }
    }
}
