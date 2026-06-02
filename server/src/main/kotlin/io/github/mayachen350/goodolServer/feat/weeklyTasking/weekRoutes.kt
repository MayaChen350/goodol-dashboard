package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mariadb.r2dbc.message.server.ErrorPacket

fun Route.includeWeekRoutes() {
    route("/week") {
        post {
            val weekData: WeekDTO = call.receive()

            try {
                WeeklyTaskingService.createNewWeek(weekData)
                call.respond(HttpStatusCode.Created, "Successfully registered new week in the database.")
            } catch (e: Exception) {
                if (e.message != null && (e.message!!.contains("Duplicate entry") || e.message!!.startsWith("Unique index")))
                    call.respond(HttpStatusCode.Conflict, e.message.toString())
                else throw e
            }
        }
    }
}