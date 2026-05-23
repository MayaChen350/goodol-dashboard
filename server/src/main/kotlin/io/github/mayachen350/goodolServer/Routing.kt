package io.github.mayachen350.goodolServer

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.ucasoft.ktor.simpleCache.cacheOutput
import io.github.mayachen350.goodolServer.application.UserSession
import io.github.mayachen350.goodolServer.feat.weeklyTasking.includeWeeklyTaskingRoutes
import io.ktor.http.HttpStatusCode
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import io.ktor.server.http.content.*
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.sessions.*
import io.ktor.server.sessions.get

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
        get<Articles> { articles ->
            // Get all articles ...
            call.respond(articles)
        }
        get<Articles.New> {
            // Show a page with fields for creating a new article ...
            call.respondText("Create a new article")
        }
        post<Articles> {
            // Save an article ...
            call.respondText("An article is saved", status = HttpStatusCode.Created)
        }
        get<Articles.Id> { article ->
            // Show an article with id ${article.id} ...
            call.respondText("An article with id ${article.id}", status = HttpStatusCode.OK)
        }
        get<Articles.Id.Edit> { article ->
            // Show a page with fields for editing an article ...
            call.respondText("Edit an article with id ${article.parent.id}", status = HttpStatusCode.OK)
        }
        put<Articles.Id> { article ->
            // Update an article ...
            call.respondText("An article with id ${article.id} updated", status = HttpStatusCode.OK)
        }
        delete<Articles.Id> { article ->
            // Delete an article ...
            call.respondText("An article with id ${article.id} deleted", status = HttpStatusCode.OK)
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
        get("/session/increment") {
            val session = call.sessions.get<UserSession>() ?: UserSession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }

        includeWeeklyTaskingRoutes()
    }
}