package io.github.mayachen350

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.ucasoft.ktor.simpleCache.cacheOutput
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import io.ktor.resources.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import kotlinx.serialization.Serializable
import io.ktor.server.http.content.*
import io.ktor.server.sessions.*

fun Application.configureRouting() {
    routing {
        staticResources("/static", "static")
        cacheOutput(2.seconds) {
            get("/short") {
                call.respond(Random.nextInt().toString())
            }
        }
        cacheOutput {
            get("/default") {
                call.respond(Random.nextInt().toString())
            }
        }
        get<Articles> { article ->
            // Get all articles ...
            call.respond("List of articles sorted starting from ${article.sort}")
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }
    }
}