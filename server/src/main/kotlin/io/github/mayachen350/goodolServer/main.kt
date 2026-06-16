package io.github.mayachen350.goodolServer

import io.github.mayachen350.goodolServer.application.*
import io.ktor.server.application.Application
import io.ktor.server.cio.*
import io.ktor.server.engine.*

suspend fun Application.rootModule(dbConnection: DbConnection) {
    configureExposed(dbConnection)
    configureStatusPages()
    configureResources()
    configureRequestValidation()
    configureHttp()
    configureSerialization()
    configureSessionCookies()
    configureRouting()

    setup()

    println("Server ready.")
}

fun main(args: Array<String>) {
    embeddedServer(
        factory = CIO,
        port = 3843,
        host = "0.0.0.0",
        module = {
            rootModule(MariaDBConnection)
            configureFlyway(MariaDBConnection)
        }
    ).start(wait = true)
}
