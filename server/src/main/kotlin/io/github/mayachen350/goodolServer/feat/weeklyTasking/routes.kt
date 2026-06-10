package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.ktor.server.routing.*

fun Route.includeWeeklyTaskingRoutes() {
    route("/weeklyTasking") {
        includeCategoriesRoutes()
        includeTasksRoutes()
        includePeopleRoutes()
        includeWeekRoutes()
    }
}