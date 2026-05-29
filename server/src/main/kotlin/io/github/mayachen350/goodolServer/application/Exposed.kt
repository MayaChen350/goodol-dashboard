package io.github.mayachen350.goodolServer.application

import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

object DbConnection{
    const val URL = "r2dbc:mariadb://localhost:3306/GoodolDb"
    const val USER = "dev"
    const val PASSWORD = "password"
}

suspend fun Application.configureExposed() {
    val database = R2dbcDatabase.connect(
        url = DbConnection.URL,
        user = DbConnection.USER,
        password = DbConnection.PASSWORD,
    )
}
