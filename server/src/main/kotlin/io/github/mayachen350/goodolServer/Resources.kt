package io.github.mayachen350.goodolServer

import io.ktor.server.application.*
import io.ktor.server.resources.*

fun Application.configureResources() {
    install(Resources)
}
