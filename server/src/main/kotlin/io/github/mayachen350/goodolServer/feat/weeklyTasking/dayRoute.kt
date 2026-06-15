package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.utils.today
import io.ktor.resources.Resource
import io.ktor.server.resources.get
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

    }

    // tasks to do today
    get<Days.Todos> {
        getTodos(LocalDate.today(), it.ofSomeoneId)
    }

    get<Days.Date.Todos> {
        getTodos(it.parent.date, it.ofSomeoneId)
    }
}