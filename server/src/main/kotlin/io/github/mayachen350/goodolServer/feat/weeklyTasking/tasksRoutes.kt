package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.TasksTable
import io.github.mayachen350.goodolServer.utils.catchConflicts
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@Resource("/tasks")
private class Tasks {
    @Resource("new")
    class New(val parent: Tasks = Tasks())

    @Resource("{id}")
    class Id(val parent: Tasks = Tasks(), val id: Int) {
        @Resource("edit")
        class Edit(val parent: Id)
    }

    @Resource("{name}")
    class Name(val parent: Tasks = Tasks(), val name: String)
}

fun Route.includeTasksRoutes() {
    get<Tasks.Id> {

    }
    get("/tasks") {
        call.respond(WeeklyTaskingService.Tasks.getAllTasks().map {
            TaskDTO(it[TasksTable.name], it[TasksTable.categoryId]?.value)
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

    // Delete from task's name
    delete<Tasks.Name> {
        val name = it.name

        if (WeeklyTaskingService.Tasks.existsByName(name)) {
            WeeklyTaskingService.Tasks.deleteTaskByName(name)
            call.respond(HttpStatusCode.Gone, "Task successfully removed.")
        } else call.respond(HttpStatusCode.NotFound, "No task with that name.")
    }

    // alternative method to do this if this is preferable later:

//    route("/tasks") {
//        get("/{id}") {
//            call.parameters["id"]
//        }
//    }
}
