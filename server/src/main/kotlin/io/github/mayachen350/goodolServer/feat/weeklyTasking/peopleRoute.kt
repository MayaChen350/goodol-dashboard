package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.includePeopleRoutes() {
    route("/people") {
        post {
            val data = call.receive<SomeoneDTO>()

            println(data)
        }
    }
}