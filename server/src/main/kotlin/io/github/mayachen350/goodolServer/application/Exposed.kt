package io.github.mayachen350.goodolServer.application

import io.ktor.server.application.*
import org.flywaydb.core.internal.database.base.Database
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

interface DbConnection {
    val URL: String
    val USER: String
    val PASSWORD: String
}

object MariaDBConnection : DbConnection {
    override val URL = "r2dbc:mariadb://localhost:3306/GoodolDb"
    override val USER = "dev"
    override val PASSWORD = "password"
}

// TODO eventually maybe: find a better way to pass the db around by like injecting the database connection or smth
lateinit var database: R2dbcDatabase

fun Application.configureExposed(dbConnection: DbConnection) {
    database = R2dbcDatabase.connect(
        url = dbConnection.URL,
        user = dbConnection.USER,
        password = dbConnection.PASSWORD,
    )
}
