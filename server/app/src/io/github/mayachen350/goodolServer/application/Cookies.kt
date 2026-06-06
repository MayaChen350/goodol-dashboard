package io.github.mayachen350.goodolServer.application

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

@Serializable
data class UserSession(val count: Int = 0)

fun Application.configureSessionCookies() {

    install(Sessions) {
        cookie<io.github.mayachen350.goodolServer.application.UserSession>("session") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
}