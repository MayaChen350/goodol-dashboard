package io.github.mayachen350.goodolServer

import io.github.mayachen350.goodolServer.application.DbConnection
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import java.util.UUID

class H2DbConnection(val randomUUID: UUID): DbConnection {
    override val URL: String = "r2dbc:h2:mem:///${randomUUID};DB_CLOSE_DELAY=-1;MODE=MySQL"
    override val USER: String = "root"
    override val PASSWORD: String = ""
}

fun Application.configureFlyway(dbConnection: H2DbConnection) {
    Flyway.configure()
        .dataSource(
            "jdbc:h2:mem:${dbConnection.randomUUID};DB_CLOSE_DELAY=-1;MODE=MySQL",
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