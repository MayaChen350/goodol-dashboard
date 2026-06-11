package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.WeeksTable
import io.github.mayachen350.goodolServer.utils.today
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

fun Route.includeWeekRoutes() {
    route("/week") {
        post {
            val weekData: WeekDTO = call.receive()

            if (weekData.date < LocalDate.today()) {
                call.respond(HttpStatusCode.Forbidden, "Cannot create a week in the past!!")
                return@post
            }

            WeeklyTaskingService.Weeks.createNewWeek(weekData)
            call.respond(HttpStatusCode.Created, "Successfully registered new week in the database.")
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