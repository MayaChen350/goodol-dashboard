package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.WeeksTable
import io.github.mayachen350.goodolServer.utils.today
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.*
import kotlin.time.Clock

fun Route.includeWeekRoutes() {
    route("/week") {
        post {
            val weekData: WeekDTO = call.receive()

            if (weekData.date < LocalDate.today()) {
                call.respond(HttpStatusCode.Forbidden, "Cannot create a week in the past!!")
                return@post
            }

            try {
                WeeklyTaskingService.Weeks.createNewWeek(weekData)
                call.respond(HttpStatusCode.Created, "Successfully registered new week in the database.")
            } catch (e: Exception) {
                if (e.message != null && (e.message!!.contains("Duplicate entry") || e.message!!.startsWith("Unique index")))
                    call.respond(HttpStatusCode.Conflict, e.message.toString())
                else throw e
            }
        }

        get("/currentWeekId") {
            val todayDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

            val weekId: UInt? = WeeklyTaskingService.Weeks.getByDate(todayDate)?.let { it[WeeksTable.id] }

            call.respond<UInt>(
                weekId ?: WeeklyTaskingService.Weeks.createNewWeekFromDate(todayDate)
            )
        }
    }
}