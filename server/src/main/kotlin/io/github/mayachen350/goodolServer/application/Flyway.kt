package io.github.mayachen350.goodolServer.application

import io.ktor.server.application.Application
import org.flywaydb.core.Flyway

fun Application.configureFlyway() {
    Flyway.configure()
        .dataSource(
            DbConnection.URL.replace("r2dbc", "jdbc"),
            DbConnection.USER, DbConnection.PASSWORD
        )
//        .baselineOnMigrate(true) // Used when migrating an existing database for the first time
        .load()
        .also { if (it.validateWithResult().invalidMigrations.any()) it.repair() }
        .migrate()
}