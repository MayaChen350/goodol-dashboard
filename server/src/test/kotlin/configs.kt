package io.github.mayachen350.goodolServer

import io.github.mayachen350.goodolServer.application.DbConnection
import io.ktor.server.application.*
import org.flywaydb.core.Flyway

object H2DbConnection: DbConnection {
    override val URL: String = "r2dbc:h2:file:///../h2"
    override val USER: String = "root"
    override val PASSWORD: String = ""
}

fun Application.configureFlyway(dbConnection: DbConnection) {
    Flyway.configure()
        .dataSource(
            "jdbc:h2:../h2;MODE=MySQL",
            dbConnection.USER, dbConnection.PASSWORD
        )
        .driver("org.h2.Driver")
        .baselineOnMigrate(true)
        .load()
        .also { if (it.validateWithResult().invalidMigrations.any()) it.repair() }
        .migrate()
}

// suspend fun Application.configureDb() {
//    suspendTransaction {
//        SchemaUtils.create(tables = WeeklyTaskingTables)
//    }
//}