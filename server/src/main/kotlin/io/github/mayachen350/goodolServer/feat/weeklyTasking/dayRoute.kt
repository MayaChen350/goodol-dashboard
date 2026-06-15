package io.github.mayachen350.goodolServer.feat.weeklyTasking

import arrow.core.getOrElse
import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.utils.today
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import kotlinx.datetime.LocalDate

@Resource("day")
private class Days {
    @Resource("{date}")
    class Date(val parent: Days = Days(), val date: LocalDate) {
        @Resource("todos")
        class Todos(val parent: Date, val ofSomeoneId: ResponsibleId? = null)
    }

    @Resource("todos")
    class Todos(val parent: Days = Days(), val ofSomeoneId: ResponsibleId? = null)
}

fun Route.includeDayRoutes() {
    suspend fun RoutingContext.getTodos(date: LocalDate, responsibleId: ResponsibleId?) {
        // I'm not sure if there should be validation for a query parameter,
        // but maybe it's better than just returning an empty array with an OK response?
        val id: Int? = responsibleId?.validate()?.getOrElse {
            return call.respond(HttpStatusCode.NotFound, "Invalid responsible id.")
        }

        call.respond<List<TodoDTO>>(
            HttpStatusCode.OK,
            WeeklyTaskingService.TaskTodos.getAllThisDay(date, id)
                .map { TodoDTO.fromResultRow(it) }
        )
    }

    // tasks to do today
    get<Days.Todos> {
        getTodos(LocalDate.today(), it.ofSomeoneId)
    }

    get<Days.Date.Todos> {
        getTodos(it.parent.date, it.ofSomeoneId)
    }
}