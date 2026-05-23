package io.github.mayachen350.goodolServer

import io.ktor.server.engine.*
import io.ktor.server.application.*
import io.ktor.server.cio.CIO

fun main(args: Array<String>) {
    embeddedServer(
        factory = CIO,
        port = 3843,
        host = "0.0.0.0",
        module = Application::rootModule
    ).start(wait = true)
}
