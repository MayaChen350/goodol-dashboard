package io.github.mayachen350.goodolServer.application

import arrow.core.left
import io.github.mayachen350.goodolServer.utils.DatabaseConflictError
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.CannotTransformContentToTypeException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import org.jetbrains.exposed.v1.r2dbc.ExposedR2dbcException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<CannotTransformContentToTypeException> { call, cause ->
            call.respondText(text = cause.localizedMessage, status = HttpStatusCode.BadRequest)
        }
        exception<BadRequestException> { call, cause ->
            call.respondText(text = cause.localizedMessage, status = HttpStatusCode.BadRequest)
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
        exception<ExposedR2dbcException> { call, e ->
            if (e.message.startsWith("Duplicate entry") || e.message.startsWith("Unique index")) {
                call.respond(HttpStatusCode.Conflict, e.message)
                DatabaseConflictError.left()
            } else call.respondText(text = "500: $e", status = HttpStatusCode.InternalServerError)
        }
    }
}