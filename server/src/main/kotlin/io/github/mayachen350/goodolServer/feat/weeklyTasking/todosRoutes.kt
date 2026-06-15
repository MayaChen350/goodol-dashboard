package io.github.mayachen350.goodolServer.feat.weeklyTasking

import arrow.core.getOrElse
import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.WeeksTable
import io.github.mayachen350.goodolServer.utils.atEndOfWeek
import io.github.mayachen350.goodolServer.utils.atStartOfWeek
import io.github.mayachen350.goodolServer.utils.today
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.ResultRow

@Resource("todos")
private class Todos {
    @Resource("assign")
    class Assign(val parent: Todos = Todos())

    @Resource("complete")
    class Complete(val parent: Todos = Todos()) {
        @Resource("{id}")
        class Id(val parent: Complete = Complete(), val id: TaskTodoId)
    }
}

fun Route.includeTodosRoutes() {
    post<Todos> {
        val taskId: Int
        val week: ResultRow
        val personId: Int?
        val dayOfWeek: kotlinx.datetime.DayOfWeek

        with(call.receive<NewTodoDTO>()) {
            dayOfWeek = weekDay
            week = WeeklyTaskingService.Weeks.getById(this.weekId.value).let {
                it ?: return@post call.respond(HttpStatusCode.Forbidden, "Invalid week id.")
            }
            val today = LocalDate.today() // cached because this function I made is misleading in the fact it's probably
            // not efficient to call it multiple times in a row

            // imagine creating a task to do for the past
            if ((week[WeeksTable.startDate] < today && week[WeeksTable.endDate] < today
                    .atEndOfWeek())
                || (week[WeeksTable.startDate] == today
                    .atStartOfWeek() && dayOfWeek < today.dayOfWeek)
            ) {
                return@post call.respond(
                    HttpStatusCode.Forbidden,
                    "Cannot create a task to do in the past!! Too laaaate"
                )
            }

            taskId = this.taskId.validate().getOrElse {
                return@post call.respond(HttpStatusCode.Forbidden, "Invalid task id.")
            }
            personId = responsibleId?.validate()?.getOrElse {
                return@post call.respond(HttpStatusCode.Forbidden, "The responsible id provided was invalid.")
            }
        }

        call.respond<TodoDTO>(
            HttpStatusCode.Created,
            WeeklyTaskingService.TaskTodos.create(
                taskId,
                week[WeeksTable.id],
                personId,
                dayOfWeek
            ).let(TodoDTO::fromResultRow)
        )
    }
    post<Todos.Assign> {
        val assignTodoData = call.receive<AssignTodoDTO>()
    }
    post<Todos.Complete.Id> {
        val id = it.id.validate().getOrElse {
            return@post call.respond(HttpStatusCode.NotFound, "Invalid TaskTodo id. Todo not found.")
        }
    }
}