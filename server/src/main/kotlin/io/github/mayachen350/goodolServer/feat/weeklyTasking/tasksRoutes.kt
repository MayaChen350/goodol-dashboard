package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.TasksTable
import io.github.mayachen350.goodolServer.utils.catchConflicts
import io.ktor.http.HttpStatusCode
import io.ktor.resources.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*

@Resource("/tasks")
private class Tasks {
    @Resource("new")
    class New(val parent: Tasks = Tasks())

    @Resource("{id}")
    class Id(val parent: Tasks = Tasks(), val id: Long) {
        @Resource("edit")
        class Edit(val parent: Id)
    }
}

fun Route.includeTasksRoutes() {
    get<Tasks.Id> {

    }
    get("/tasks") {
        call.respond(WeeklyTaskingService.Tasks.getAllTasks().map {
            TaskDTO(it[TasksTable.name], it[TasksTable.categoryId].value)
        })
    }
    post("/tasks") {
        with(call.receive<TaskDTO>()) {
            catchConflicts {
                WeeklyTaskingService.Tasks.createNewTask(this)
                call.respond(HttpStatusCode.Created, "New task created!")
            }
        }
    }

    // alternative method to do this if this is preferable later:

//    route("/tasks") {
//        get("/{id}") {
//            call.parameters["id"]
//        }
//    }
}
