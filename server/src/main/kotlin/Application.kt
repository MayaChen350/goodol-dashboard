package io.github.mayachen350

import io.ktor.server.application.Application

fun Application.rootModule() {
    configureExposed()
    configureStatusPages()
    configureResources()
    configureRequestValidation()
    configureHttp()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
