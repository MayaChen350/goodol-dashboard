package io.github.mayachen350.goodolServer.application

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.CannotTransformContentToTypeException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        // TODO maybe: add auto conflict handling?
        exception<CannotTransformContentToTypeException> { call, cause ->
            call.respondText(text = cause.localizedMessage, status = HttpStatusCode.BadRequest)
        }
        exception<BadRequestException> { call, cause ->
            call.respondText(text = cause.localizedMessage, status = HttpStatusCode.BadRequest)
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
}
