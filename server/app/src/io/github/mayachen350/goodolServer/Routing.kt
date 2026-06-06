package io.github.mayachen350.goodolServer

import io.github.mayachen350.goodolServer.feat.weeklyTasking.includeWeeklyTaskingRoutes
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*

fun Application.configureRouting() {
    routing {
        get("/") { call.respond("OK") } // testing for server response endpoint
        swaggerUI(path = "swagger") {
            /*
             Documentation source configuration goes here.

             This can be from file (documentation.yaml), or it can be served dynamically from your sources using the
             `describe {}` API on routes.  When `openApi` enabled in Gradle, these calls will be automatically injected
             based on your code and comments.
             */
            info = OpenApiInfo("Goodol Dashboard API", "0.1.0")
            source = OpenApiDocSource.Routing(ContentType.Application.Json) {
                routingRoot.descendants()
            }
        }

//        staticResources("/static", "static")

        includeWeeklyTaskingRoutes()
    }
}