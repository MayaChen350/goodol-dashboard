package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.WeeksTable
import io.github.mayachen350.goodolServer.utils.today
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Resource("week")
private class Weeks {
    @Resource("currentWeekId")
    class CurrentWeekId(val parent: Weeks = Weeks())

    @Resource("{id}")
    class Id(val parent: Weeks = Weeks(), val id: UInt) {
        @Resource("todos")
        class Todos(val parent: Id, val ofSomeoneId: Int? = null)
    }

    @Resource("todos")
    class Todos(val parent: Weeks = Weeks(), val ofSomeoneId: Int? = null)
}

fun Route.includeWeekRoutes() {
    post<Weeks> {
        val weekData: WeekDTO = call.receive()

        if (weekData.date < LocalDate.today()) {
            call.respond(HttpStatusCode.Forbidden, "Cannot create a week in the past!!")
            return@post
        }

        WeeklyTaskingService.Weeks.createNewWeek(weekData)
        call.respond(HttpStatusCode.Created, "Successfully registered new week in the database.")
    }

    suspend fun currentWeekId(): UInt {
        val todayDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        return WeeklyTaskingService.Weeks.getByDate(todayDate)?.let { it[WeeksTable.id] }
            ?: WeeklyTaskingService.Weeks.createNewWeekFromDate(todayDate)
    }

    get<Weeks.CurrentWeekId> {
        call.respond<UInt>(currentWeekId())
    }

    suspend fun RoutingContext.getTodos(weekId: UInt, ofSomeoneId: Int?) {
        call.respond(HttpStatusCode.OK, "yes yes yes")
    }

    // tasks to do this week
    get<Weeks.Todos> {
        getTodos(currentWeekId(), it.ofSomeoneId)
    }

    get<Weeks.Id.Todos> {
        getTodos(it.parent.id, it.ofSomeoneId)


    }
}
