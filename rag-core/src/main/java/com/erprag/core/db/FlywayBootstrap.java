package com.erprag.core.db;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

public final class FlywayBootstrap {

    private FlywayBootstrap() {
    }

    public static void migrate(DataSource dataSource) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
