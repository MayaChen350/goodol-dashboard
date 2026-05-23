package io.github.mayachen350.goodolServer

import io.ktor.server.application.Application
import kotlinx.coroutines.runBlocking

fun Application.rootModule() = runBlocking {
    configureExposed()
    configureStatusPages()
    configureResources()
    configureRequestValidation()
    configureHttp()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
