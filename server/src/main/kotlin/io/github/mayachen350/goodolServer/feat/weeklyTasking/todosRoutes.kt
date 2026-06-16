package io.github.mayachen350.goodolServer.feat.weeklyTasking

import arrow.core.getOrElse
import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.TasksTable
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
import io.ktor.server.resources.get
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.ResultRow

@Resource("todos")
private class Todos {
    @Resource("{id}")
    class Id(val parent: Todos = Todos(), val id: TaskTodoId)

    @Resource("assign")
    class Assign(val parent: Todos = Todos())

    @Resource("complete")
    class Complete(val parent: Todos = Todos()) {
        @Resource("{id}")
        class Id(val parent: Complete = Complete(), val id: TaskTodoId)
    }
}

fun Route.includeTodosRoutes() {
    get<Todos.Id> {
        val todo: TodoDTO? = WeeklyTaskingService.TaskTodos.getById(it.id.value)?.let(TodoDTO::fromResultRow)

        if (todo == null) {
            call.respond(HttpStatusCode.NotFound, "Todo not found")
            return@get
        }

        call.respond<TodoDTO>(todo)
    }

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

        val todoId = assignTodoData.todoId.validate().getOrElse {
            return@post call.respond(HttpStatusCode.Forbidden, "Invalid TaskTodo id. Todo not found.")
        }
        val responsibleId: Int? = assignTodoData.responsibleId?.validate()?.getOrElse {
            return@post call.respond(HttpStatusCode.Forbidden, "Invalid person id.")
        }

        WeeklyTaskingService.TaskTodos.assign(todoId, responsibleId)
        call.respond(HttpStatusCode.OK, if (responsibleId !== null) "Task assigned!" else "Task unassigned!")
    }
    post<Todos.Complete.Id> {
        val id = it.id.validate().getOrElse {
            return@post call.respond(HttpStatusCode.NotFound, "Invalid TaskTodo id. Todo not found.")
        }

        call.respond(HttpStatusCode.OK, WeeklyTaskingService.TaskTodos.toggleCompletion(id))
    }
}